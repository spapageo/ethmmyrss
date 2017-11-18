package com.spapageo.thmmyrss.client;

import com.spapageo.ethmmyrss.ThmmyRssApplication;
import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.client.AnnouncementFetcher;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;

import static org.junit.Assert.*;

public class AnnouncementFetcherTest {
    @ClassRule
    public static final DropwizardAppRule<ThmmyRssConfiguration> RULE = //
            new DropwizardAppRule<>(ThmmyRssApplication.class, ResourceHelpers.resourceFilePath("thmmyrss.yml"));


    private AnnouncementFetcher announcementFetcher;
    private ThmmyRssConfiguration config;

    @Before
    public void setup() throws Exception {
        ThmmyRssApplication thmmyRssApplication = RULE.getApplication();
        Client client = thmmyRssApplication.getClient();
        config = RULE.getConfiguration();
        announcementFetcher = new AnnouncementFetcher(client, RULE.getConfiguration());
    }

    @Test
    public void login() {
        assertTrue("Check corrert login", announcementFetcher.login(config.getFetcher().getUsername(),
                config.getFetcher().getPassword()).isPresent());
        assertTrue("Check wrong login", !announcementFetcher.login("a", "a").isPresent());
    }

    @Test
    public void lessonLogin() {
        String jsession = announcementFetcher.login(config.getFetcher().getUsername(),
                config.getFetcher().getPassword()).orElse(null);
        assertNotNull(jsession);
        assertTrue("Check correct lesson login", announcementFetcher.lessonLogin(36, jsession));
        assertFalse("Check wrong lesson login", announcementFetcher.lessonLogin(0, jsession));
    }

    @Test
    public void lessonAnnouncements() {
        String jsession = announcementFetcher.login(config.getFetcher().getUsername(),
                config.getFetcher().getPassword()).orElse(null);
        assertTrue(announcementFetcher.lessonLogin(36, jsession));
        if (config.getFetcher().getUsername().equals("guest")) {
            assertTrue(announcementFetcher.lessonAnnouncements(config.getFetcher().getEthmmyUrl(), 36, jsession).isEmpty());
        } else {
            assertFalse(announcementFetcher.lessonAnnouncements(config.getFetcher().getEthmmyUrl(), 36, jsession).isEmpty());
        }
    }
}
