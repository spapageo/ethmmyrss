package com.spapageo.ethmmyrss.resourses;

import com.codahale.metrics.annotation.Timed;
import com.spapageo.ethmmyrss.ThmmyRssConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
    private ThmmyRssConfiguration config;

    public IndexResource(ThmmyRssConfiguration config) {
        this.config = config;
    }

    @GET
    @Timed
    public IndexView getIndex() {
        return new IndexView(config.getLessonIds());
    }
}