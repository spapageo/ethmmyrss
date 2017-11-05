/**
 *
 */
package com.spapageo.ethmmyrss.resourses;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.Channel;
import com.spapageo.ethmmyrss.api.Item;
import com.spapageo.ethmmyrss.api.Rss;
import com.spapageo.ethmmyrss.jdbi.RssDAO;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/feed/{id}")
@Produces(MediaType.APPLICATION_XML)
public class FeedResource {
    private RssDAO dao;
    private ThmmyRssConfiguration config;

    public FeedResource(RssDAO dao, ThmmyRssConfiguration config) {
        this.dao = dao;
        this.config = config;
    }

    @GET
    @Timed
    public Rss getLessonFeed(@PathParam("id") int id) {
        if (!config.getLesson_ids().containsKey(id)) {
            throw new WebApplicationException(404);
        }
        List<Item> l = dao.getItemsForId(id);
        return new Rss(new Channel("Ανακοινώσεις", "http://alexander.ee.auth.gr:8083/eTHMMY/",
                config.getLesson_ids().get(id), l));
    }
}