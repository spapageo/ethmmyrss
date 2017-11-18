package com.spapageo.ethmmyrss;

import com.spapageo.ethmmyrss.core.service.AnnouncementService;
import com.spapageo.ethmmyrss.core.service.AnnouncementServiceImpl;
import com.spapageo.ethmmyrss.client.AnnouncementFetcher;
import com.spapageo.ethmmyrss.cli.RecreateTableCommand;
import com.spapageo.ethmmyrss.cli.UpdateAnnouncementsCommand;
import com.spapageo.ethmmyrss.health.AnnouncementFetcherHealthCheck;
import com.spapageo.ethmmyrss.jdbi.AnnouncementDAO;
import com.spapageo.ethmmyrss.resourses.FeedResource;
import com.spapageo.ethmmyrss.resourses.IndexResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jdbi.bundles.DBIExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.net.URI;

public class ThmmyRssApplication extends Application<ThmmyRssConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThmmyRssApplication.class);
    private AnnouncementDAO announcementDAO;
    private Client client;
    private AnnouncementService announcementService;

    public static void main(String[] args) throws Exception {

        //overwrite config with environment variables
        if (System.getenv("DATABASE_URL") != null) {
            URI dbUri = new URI(System.getenv("DATABASE_URL"));
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

            System.setProperty("dw.database.user", username);
            System.setProperty("dw.database.password", password);
            System.setProperty("dw.database.url", dbUrl);
        }

        if (System.getenv("PORT") != null) {
            System.setProperty("dw.server.applicationConnectors[0].port", System.getenv("PORT"));
        }

        if (System.getenv("USERNAME") != null) {
            System.setProperty("dw.fetcher.username", System.getenv("USERNAME"));
        }

        if (System.getenv("PASSWORD") != null) {
            System.setProperty("dw.fetcher.password", System.getenv("PASSWORD"));
        }

        new ThmmyRssApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ThmmyRssConfiguration> bootstrap) {
        bootstrap.addCommand(new RecreateTableCommand(this, "recreatetable", "Recreate the database table."));
        bootstrap.addCommand(new UpdateAnnouncementsCommand(this, "updatedb", "Update the announcement database"));
        bootstrap.addBundle(new DBIExceptionsBundle());
        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets"));
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(ThmmyRssConfiguration configuration, Environment env)
            throws Exception {
        client = new JerseyClientBuilder(env).using(configuration.getJerseyClientConfiguration()).using(env)
                .withProperty("PROPERTY_CHUNKED_ENCODING_SIZE", 0).build("jersey-client");
        DBIFactory factory = new DBIFactory();
        DBI jdbi = factory.build(env, configuration.getDatabase(), "announcement-db");
        announcementDAO = jdbi.onDemand(AnnouncementDAO.class);

        tryCreateAnnouncementTable();
        AnnouncementFetcher announcementFetcher = new AnnouncementFetcher(client, configuration);
        env.healthChecks().register("announcement-fetcher-health", new AnnouncementFetcherHealthCheck(configuration, announcementFetcher));
        this.announcementService = new AnnouncementServiceImpl(announcementDAO, announcementFetcher, configuration);
        env.jersey().register(new FeedResource(announcementService, configuration));
        env.jersey().register(new IndexResource(configuration));
    }

    private void tryCreateAnnouncementTable() {
        try {
            announcementDAO.createAnnouncementsTable();
        } catch (Exception e) {
            LOGGER.warn("Table probably already exists. Trying to continue");
        }
    }

    public AnnouncementDAO getAnnouncementDAO() {
        return announcementDAO;
    }

    public Client getClient() {
        return client;
    }

    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }
}
