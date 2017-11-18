package com.spapageo.ethmmyrss.client;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.domain.Announcement;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnouncementFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementFetcher.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd MMM yyyy hh:mm aa")
            .withLocale(new Locale("EL"));
    private static final String LOGOUT_URL_PART = "logout.do";
    private static final String ERROR_MSG = "etErrorMsg";
    private static final Pattern HEADER_PATTERN = Pattern.compile(".*JSESSIONID=(.*?);.*");
    private final Client client;
    private final String eThmmyUrl;
    private final String eThmmyLoginUrl;

    public AnnouncementFetcher(Client client, ThmmyRssConfiguration config) {
        this.client = client;
        this.eThmmyUrl = config.getFetcher().getEthmmyUrl();
        this.eThmmyLoginUrl = config.getFetcher().getEthmmyLogin();
    }

    public Optional<String> login(String username, String password) {

        Form params = new Form().param("username", username).param("password", password);

        Response response = post(eThmmyLoginUrl, params);

        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code " + response.getStatus() + " can't login :(");
            return Optional.empty();
        }

        String htmlContent = response.readEntity(String.class);


        if (!htmlContent.contains(LOGOUT_URL_PART)) {
            LOGGER.warn("Unsuccessfull login. Wrong credentials");
            return Optional.empty();
        }
        Optional<String> jSessionCookie = Optional.ofNullable(response.getHeaders().getFirst("Set-Cookie")) //
                .map(Object::toString) //
                .map(HEADER_PATTERN::matcher) //
                .filter(Matcher::matches) //
                .map(matcher -> matcher.group(1)) //
                .filter(cookie -> !StringUtils.isBlank(cookie)); //

        if (jSessionCookie.isPresent()) {
            LOGGER.info("Successful login. Session id is: {}", jSessionCookie.get());
        } else {
            LOGGER.warn("Failed to get jsessionid.");
        }

        return jSessionCookie;
    }

    private Response post(String loginUrl, Form params) {
        return client.target(loginUrl).request(MediaType.TEXT_HTML)
                .post(Entity.entity(params, MediaType.APPLICATION_FORM_URLENCODED), Response.class);
    }

    public boolean lessonLogin(int lessonId, String jsessionId) {

        //http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.data.do?method=jspregister&PRMID=149
        String registerResource = eThmmyUrl + "cms.course.data.do;jsessionid=" + jsessionId +
                "?method=jspregister&PRMID=" + lessonId;
        Response response = get(registerResource);
        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code {} can't register to the lesson with id: {}", response.getStatus(), lessonId);
            return false;
        }

        //http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.login.do;jsessionid=9AC9CF991DD087375184D0FE4762336F?method=execute&PRMID=64
        String loginResource = eThmmyUrl + "cms.course.login.do;jsessionid=" + jsessionId + "?method=execute&PRMID="
                + lessonId;
        response = get(loginResource);

        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code {} can't login to the lesson with id: {}", response.getStatus(), lessonId);
            return false;
        }
        String htmlContent = response.readEntity(String.class);
        if (isLoggedOutOrContainsErrorMessage(htmlContent)) {
            LOGGER.warn("Something went wrong at the lesson login.");
            return false;
        }
        LOGGER.info("Successfull lesson login. Lesson id is: {}", lessonId);
        return true;
    }

    private Response get(String registerResource) {
        return client.target(registerResource).request(MediaType.TEXT_HTML).get(Response.class);
    }

    public List<Announcement> lessonAnnouncements(String ethmmyUrl, int lessonId, String jsessionId) {
        //http://alexander.ee.auth.gr:8083/eTHMMY/cms.announcement.data.do;jsessionid=546875641B6BE1229A818EA20A3F4CB3?method=jsplist&PRMID=77
        String resourse = ethmmyUrl + "cms.announcement.data.do;jsessionid=" + jsessionId + "?method=jsplist&PRMID=" + lessonId;

        Response response = get(resourse);

        List<Announcement> ret = new ArrayList<>();

        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code {} can't get the announcements with id: {}", response.getStatus(), lessonId);
            return ret;
        }
        String htmlContent = response.readEntity(String.class);
        if (isLoggedOutOrContainsErrorMessage(htmlContent)) {
            LOGGER.warn("Something went wrong while getting the announcements. Id: {}", lessonId);
            return ret;
        }

        Document doc = Jsoup.parse(htmlContent);
        Elements elements = doc.select("img[src=images/spacer.gif] ~ table");

        for (Element element : elements) {
            ret.add(constructItemFromElement(lessonId, element));
        }
        LOGGER.info("Successfully got {} announcements from lesson: {}", elements.size(), lessonId);

        return ret;
    }

    private Announcement constructItemFromElement(int lessonId, Element e) {

        String title = e.child(0).child(0).child(1).child(0).text();
        String date = e.child(0).child(0).child(1).child(1).child(0).text();
        DateTime dateTime = DATE_TIME_FORMATTER.parseDateTime(date);
        Timestamp tstamp = new Timestamp(dateTime.toDate().getTime());

        // Delete the date to get only the announcement body
        e.child(0).child(0).child(1).child(1).child(0).remove();

        Element ann = e.child(0).child(0).child(1).child(1);
        for (Element elem : ann.select("[href]")) {
            elem.attr("href", elem.absUrl("href"));
        }
        String announcement = e.child(0).child(0).child(1).child(1).html();

        return new Announcement(title, tstamp, announcement, lessonId);
    }

    private boolean isLoggedOutOrContainsErrorMessage(String htmlContent) {
        return !htmlContent.contains(LOGOUT_URL_PART) || htmlContent.contains(ERROR_MSG);
    }
}
