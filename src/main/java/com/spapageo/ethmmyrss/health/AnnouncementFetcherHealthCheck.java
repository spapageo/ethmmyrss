package com.spapageo.ethmmyrss.health;

import com.codahale.metrics.health.HealthCheck;
import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.client.AnnouncementFetcher;

public class AnnouncementFetcherHealthCheck extends HealthCheck {

    private AnnouncementFetcher announcementFetcher;
    private ThmmyRssConfiguration config;

    public AnnouncementFetcherHealthCheck(ThmmyRssConfiguration config, AnnouncementFetcher announcementFetcher) {
        this.announcementFetcher = announcementFetcher;
        this.config = config;
    }

    @Override
    protected Result check() throws Exception {
        String jssesionid = announcementFetcher.login(config.getFetcher().getUsername(), config.getFetcher().getPassword())
                .orElseThrow(IllegalStateException::new);
        if (jssesionid.isEmpty()) {
            return Result.unhealthy("Site login failed");
        }
        if (!announcementFetcher.lessonLogin(36, jssesionid)) {
            return Result.unhealthy("Site login failed");
        } else {
            if (!config.getFetcher().getUsername().equals("guest")) {
                if (announcementFetcher.lessonAnnouncements(config.getFetcher().getEthmmyUrl(), 36, jssesionid).isEmpty()) {
                    return Result.unhealthy("Got zero announcements");
                }
            }
            return Result.healthy("All is good");
        }
    }

}
