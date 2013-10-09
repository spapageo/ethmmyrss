/**
 * 
 */
package com.spapageo.ethmmyrss.client;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.Item;
import com.spapageo.ethmmyrss.jdbi.RssDAO;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;

/**
 * @author Doom
 *
 */
public class Fetcher implements Runnable{
	private final Client client;
	private final ThmmyRssConfiguration config;
	private final String empty = new String();
	private final RssDAO rssDAO;

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final DateTimeFormatter form = DateTimeFormat.forPattern("dd MMM yyyy hh:mm aa").withLocale(new Locale("EL"));
	private final DateTimeFormatter rssform = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");

	public Fetcher(Client client,ThmmyRssConfiguration config,RssDAO rssDAO) {
		this.client = client;
		this.config = config;
		this.rssDAO = rssDAO;
	}

	public String login(String username,String password,String login_url){

		Form params = new Form();
		params.add("username", username);
		params.add("password", password);

		ClientResponse response = client.resource(login_url).accept(MediaType.TEXT_HTML)
				.post(ClientResponse.class, params);

		if(response.getStatus() != 200){
			LOGGER.warn("Got status code " + response.getStatus() + " can't login :(");
			return empty;
		}

		String html_content = response.getEntity(String.class);


		if(html_content.contains("logout.do")){
			String jsession = empty;
			try{
				jsession = response.getHeaders().getFirst("Set-Cookie").split(";")[0].split("=")[1];
				LOGGER.info("Successfull login. Session id is: " + jsession);
			} catch (Exception e){
				LOGGER.warn("Failed to get jsessionid.");
				LOGGER.debug(e.toString());
			}
			return jsession;
		}else{
			LOGGER.warn("Unsuccessfull login. Wrong credentials");
			return empty;
		}
	}

	public boolean lessonLogin(String ethmmyUrl,int lessonId, String jsessionId){
		//http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.login.do;jsessionid=9AC9CF991DD087375184D0FE4762336F?method=execute&PRMID=64
		String resourse = ethmmyUrl + "cms.course.login.do;jsessionid=" + jsessionId + "?method=execute&PRMID=" + lessonId;

		ClientResponse response = client.resource(resourse).accept(MediaType.TEXT_HTML).get(ClientResponse.class);

		if(response.getStatus() != 200){
			LOGGER.warn("Got status code " + response.getStatus() + " can't login to the lesson with id: " + lessonId);
			return false;
		}
		String html_content = response.getEntity(String.class);
		if(!html_content.contains("logout.do") || html_content.contains("etErrorMsg")){
			LOGGER.warn("Something went wrong at the lesson login.");
			return false;
		}
		LOGGER.info("Successfull lesson login. Lesson id is: " + lessonId);
		return true;
	}

	public List<Item> lessonAnnouncements(String ethmmyUrl,int lessonId, String jsessionId){
		//http://alexander.ee.auth.gr:8083/eTHMMY/cms.announcement.data.do;jsessionid=546875641B6BE1229A818EA20A3F4CB3?method=jsplist&PRMID=77
		String resourse = ethmmyUrl + "cms.announcement.data.do;jsessionid=" + jsessionId + "?method=jsplist&PRMID=" + lessonId;

		ClientResponse response = client.resource(resourse).accept(MediaType.TEXT_HTML).get(ClientResponse.class);

		List<Item> ret = new ArrayList<Item>();

		if(response.getStatus() != 200){
			LOGGER.warn("Got status code " + response.getStatus() + " can't get the announcements with id: " + lessonId);
			return ret;
		}
		String html_content = response.getEntity(String.class);
		if(!html_content.contains("logout.do") || html_content.contains("etErrorMsg")){
			LOGGER.warn("Something went wrong while getting the announcements. Id: " + lessonId);
			return ret;
		}

		Document doc = Jsoup.parse(html_content);
		Elements el = doc.select("img[src=images/spacer.gif] ~ table");

		String title;
		String date;
		String announcement;
		Timestamp tstamp;
		for(Element e : el){

			title = e.child(0).child(0).child(1).child(0).text();

			date = e.child(0).child(0).child(1).child(1).child(0).text();

			DateTime dateTime = form.parseDateTime(date);
			//date = rssform.print(dateTime);
			tstamp = new Timestamp(dateTime.toDate().getTime());
			
			// Delete the date to get only the announcement body
			e.child(0).child(0).child(1).child(1).child(0).remove();
			//e.child(0).child(0).child(1).child(1).child(2).remove();

			Element ann = e.child(0).child(0).child(1).child(1);
			for(Element elem : ann.select("[href]")){
				elem.attr("href",elem.absUrl("href"));
			}
			announcement = e.child(0).child(0).child(1).child(1).html();

			ret.add(new Item(title, tstamp, announcement,lessonId));
		}
		LOGGER.info("Successfully got " + el.size() + " announcements from lesson: " + lessonId);

		return ret;
	}

	@Override
	public void run() {
		LOGGER.info("Start");
		try{
			String jsession = this.login( this.config.getFetcher().getUsername(),
					this.config.getFetcher().getPassword(),
					this.config.getFetcher().getEthhmy_login());
			if(!jsession.isEmpty()){
				for(int id : config.getLesson_ids().keySet()){
					try{
						if(lessonLogin(config.getFetcher().getEthmmy_url(), id, jsession)){
							List<Item> announc = this.lessonAnnouncements(config.getFetcher().getEthmmy_url(), id, jsession);
							if(announc.size() > 10){
								announc = announc.subList(0, 10);
							}
							rssDAO.insertItems(announc);
						}
					}catch(Exception e){
						if (e instanceof BatchUpdateException){
							logException(((BatchUpdateException) e).getNextException());
						}else if(e instanceof DBIException){
							logException((DBIException) e);
						}
					}
					Thread.sleep(1000);
				}

			}
		}catch(Exception e){
			LOGGER.error("Site login failed. Probably IO exception", e);
		}
		LOGGER.info("Done");
	}
	
    protected void logException(DBIException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof SQLException) {
            for (Throwable throwable : (SQLException)cause) {
                LOGGER.error(throwable.getMessage(), throwable);
            }
        } else {
            LOGGER.error(cause.getMessage(), exception);
        }
    }
    
    protected void logException(SQLException exception) {
        final String message = exception.getMessage();
        for (Throwable throwable : exception) {
            LOGGER.error(message, throwable);
        }
    }
    
    
}
