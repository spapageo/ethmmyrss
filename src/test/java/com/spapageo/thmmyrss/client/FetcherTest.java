package com.spapageo.thmmyrss.client;


import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.client.Fetcher;
import com.spapageo.ethmmyrss.jdbi.RssDAO;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FetcherTest {
    private Fetcher f;
    private ThmmyRssConfiguration config;

    @Before
    public void setup() throws Exception {
        System.setProperty("dw.database.url", "postgresql://test");
        String configFile = getClass().getResource("/thmmyrss.yml").getFile();
        config = ConfigurationFactory.forClass(ThmmyRssConfiguration.class, new Validator()).build(new File(configFile));
        RssDAO dao = new DBI("jdbc:h2:mem:test;MVCC=TRUE").onDemand(RssDAO.class);
        Environment env = new Environment("Test env", config, new ObjectMapperFactory(), new Validator());
        f = new Fetcher(new JerseyClientBuilder().using(env).using(config.getJerseyClientConfiguration())
                .withProperty("PROPERTY_CHUNKED_ENCODING_SIZE", 0).build()
                , config, dao);
    }

    @Test
    public void login() {
        assertTrue("Check corrert login", f.login(config.getFetcher().getUsername(),
                config.getFetcher().getPassword(), config.getFetcher().getEthhmy_login()).isPresent());
        assertTrue("Check wrong login", !f.login("a", "a", config.getFetcher().getEthhmy_login()).isPresent());
    }

    @Test
    public void lessonLogin() {
        String jsession = f.login(config.getFetcher().getUsername(),
                config.getFetcher().getPassword(), config.getFetcher().getEthhmy_login()).orElse(null);
        assertNotNull(jsession);
        assertTrue("Check correct lesson login", f.lessonLogin(config.getFetcher().getEthmmy_url(), 36, jsession));
        assertFalse("Check wrong lesson login", f.lessonLogin(config.getFetcher().getEthmmy_url(), 0, jsession));
    }

    @Test
    public void lessonAnnouncements() {
        String jsession = f.login(config.getFetcher().getUsername(),
                config.getFetcher().getPassword(), config.getFetcher().getEthhmy_login()).orElse(null);
        assertTrue(f.lessonLogin(config.getFetcher().getEthmmy_url(), 36, jsession));
        if (config.getFetcher().getUsername().equals("guest")) {
            assertTrue(f.lessonAnnouncements(config.getFetcher().getEthmmy_url(), 36, jsession).isEmpty());
        } else {
            assertFalse(f.lessonAnnouncements(config.getFetcher().getEthmmy_url(), 36, jsession).isEmpty());
        }
    }

    @After
    public void cleanup() {
        System.clearProperty("dw.database.url");
    }

}
