/**
 *
 */
package com.spapageo.ethmmyrss.client;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.Item;
import com.spapageo.ethmmyrss.jdbi.RssDAO;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Fetcher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fetcher.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd MMM yyyy hh:mm aa")
            .withLocale(new Locale("EL"));
    private static final String LOGOUT_URL_PART = "logout.do";

    private final Client client;
    private final ThmmyRssConfiguration config;
    private final RssDAO rssDAO;


    public Fetcher(Client client, ThmmyRssConfiguration config, RssDAO rssDAO) {
        this.client = client;
        this.config = config;
        this.rssDAO = rssDAO;
    }

    public Optional<String> login(String username, String password, String loginUrl) {

        Form params = new Form();
        params.add("username", username);
        params.add("password", password);

        ClientResponse response = post(loginUrl, params);

        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code " + response.getStatus() + " can't login :(");
            return Optional.empty();
        }

        String htmlContent = response.getEntity(String.class);


        if (!htmlContent.contains(LOGOUT_URL_PART)) {
            LOGGER.warn("Unsuccessfull login. Wrong credentials");
            return Optional.empty();
        }
        String jsession = null;
        try {
            jsession = response.getHeaders().getFirst("Set-Cookie").split(";")[0].split("=")[1];
            LOGGER.info("Successfull login. Session id is: {}", jsession);
        } catch (Exception e) {
            LOGGER.warn("Failed to get jsessionid.", e);
        }
        return Optional.ofNullable(jsession);
    }

    private ClientResponse post(String loginUrl, Form params) {
        return client.resource(loginUrl).accept(MediaType.TEXT_HTML)
                .post(ClientResponse.class, params);
    }

    public boolean lessonLogin(String ethmmyUrl, int lessonId, String jsessionId) {
        //http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.data.do?method=jspregister&PRMID=149
        String registerResource = ethmmyUrl + "cms.course.data.do;jsessionid=" + jsessionId + "?method=jspregister&PRMID=" + lessonId;
        ClientResponse response = get(registerResource);
        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code {} can't register to the lesson with id: {}", response.getStatus(), lessonId);
            return false;
        }

        //http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.login.do;jsessionid=9AC9CF991DD087375184D0FE4762336F?method=execute&PRMID=64
        String loginResource = ethmmyUrl + "cms.course.login.do;jsessionid=" + jsessionId + "?method=execute&PRMID=" + lessonId;
        response = get(loginResource);

        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code {} can't login to the lesson with id: {}", response.getStatus(), lessonId);
            return false;
        }
        String htmlContent = response.getEntity(String.class);
        if (isLoggedOutOrContainsErrorMessage(htmlContent)) {
            LOGGER.warn("Something went wrong at the lesson login.");
            return false;
        }
        LOGGER.info("Successfull lesson login. Lesson id is: {}", lessonId);
        return true;
    }

    private ClientResponse get(String registerResource) {
        return client.resource(registerResource).accept(MediaType.TEXT_HTML).get(ClientResponse.class);
    }

    public List<Item> lessonAnnouncements(String ethmmyUrl, int lessonId, String jsessionId) {
        //http://alexander.ee.auth.gr:8083/eTHMMY/cms.announcement.data.do;jsessionid=546875641B6BE1229A818EA20A3F4CB3?method=jsplist&PRMID=77
        String resourse = ethmmyUrl + "cms.announcement.data.do;jsessionid=" + jsessionId + "?method=jsplist&PRMID=" + lessonId;

        ClientResponse response = get(resourse);

        List<Item> ret = new ArrayList<>();

        if (response.getStatus() != 200) {
            LOGGER.warn("Got status code {} can't get the announcements with id: {}", response.getStatus(), lessonId);
            return ret;
        }
        String htmlContent = response.getEntity(String.class);
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

    private Item constructItemFromElement(int lessonId, Element e) {

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

        return new Item(title, tstamp, announcement, lessonId);
    }

    @Override
    public void run() {
        LOGGER.info("Starting to update the annoucements");
        Optional<String> jsession = this.login(this.config.getFetcher().getUsername(),
                this.config.getFetcher().getPassword(),
                this.config.getFetcher().getEthhmy_login());
        if (jsession.isPresent()) {
            for (int id : config.getLesson_ids().keySet()) {
                try {
                    if (lessonLogin(config.getFetcher().getEthmmy_url(), id, jsession.get())) {
                        List<Item> announc = this.lessonAnnouncements(config.getFetcher().getEthmmy_url(), id, jsession.get());
                        if (announc.size() > 10) {
                            announc = announc.subList(0, 10);
                        }
                        rssDAO.insertItems(announc);
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    LOGGER.error("Error while processing lessond " + id, e);
                }
            }

        }
        LOGGER.info("Done");
    }

    private boolean isLoggedOutOrContainsErrorMessage(String htmlContent) {
        return !htmlContent.contains(LOGOUT_URL_PART) || htmlContent.contains("etErrorMsg");
    }
}
