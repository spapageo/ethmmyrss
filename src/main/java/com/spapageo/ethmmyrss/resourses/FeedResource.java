package com.spapageo.ethmmyrss.resourses;

import com.codahale.metrics.annotation.Timed;
import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.domain.Announcement;
import com.spapageo.ethmmyrss.api.domain.Channel;
import com.spapageo.ethmmyrss.api.domain.Rss;
import com.spapageo.ethmmyrss.api.service.AnnouncementService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/feed/{id}")
@Produces(MediaType.APPLICATION_XML)
public class FeedResource {
    private AnnouncementService announcementService;
    private ThmmyRssConfiguration config;

    public FeedResource(AnnouncementService announcementService, ThmmyRssConfiguration config) {
        this.announcementService = announcementService;
        this.config = config;
    }

    @GET
    @Timed
    public Rss getLessonFeed(@PathParam("id") int id) {
        if (!config.getLessonIds().containsKey(id)) {
            throw new WebApplicationException(404);
        }
        List<Announcement> l = announcementService.getLessonAnnouncements(id);
        return new Rss(new Channel("Ανακοινώσεις", "http://alexander.ee.auth.gr:8083/eTHMMY/",
                config.getLessonIds().get(id), l));
    }
}