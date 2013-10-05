/**
 * 
 */
package com.spapageo.thmmyrss.resourses;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.spapageo.thmmyrss.ThmmyRssConfiguration;
import com.spapageo.thmmyrss.api.Channel;
import com.spapageo.thmmyrss.api.Item;
import com.spapageo.thmmyrss.api.Rss;
import com.spapageo.thmmyrss.jdbi.RssDAO;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

/**
 * @author Doom
 *
 */
@Path("/feed/{id}")
@Produces(MediaType.APPLICATION_XML)
public class FeedResource {
	private RssDAO dao;
	private ThmmyRssConfiguration config;
	
    public FeedResource(RssDAO dao,ThmmyRssConfiguration config) {
    	this.dao = dao;
    	this.config = config;
    }

    @GET
    @Timed
    @CacheControl(maxAge = 30,maxAgeUnit = TimeUnit.MINUTES)
    public Rss sayHello(@PathParam("id") int id) {
    	if(!config.getLesson_ids().containsKey(id)){
    		throw new WebApplicationException(404);
    	}
		Rss r = new Rss();
		List<Item> l = dao.getItemsForId(id);
		r.setChannel(new Channel("Ανακοινώσεις","http://alexander.ee.auth.gr:8083/eTHMMY/",config.getLesson_ids().get(id),l));
    	return r;
    }
}