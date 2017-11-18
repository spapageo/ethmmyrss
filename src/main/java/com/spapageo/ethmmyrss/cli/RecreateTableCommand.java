package com.spapageo.ethmmyrss.cli;

import com.spapageo.ethmmyrss.ThmmyRssApplication;
import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

public class RecreateTableCommand extends EnvironmentCommand<ThmmyRssConfiguration> {
    private ThmmyRssApplication application;

    public RecreateTableCommand(ThmmyRssApplication application,
                                String name, String description) {
        super(application, name, description);
        this.application = application;
    }

    @Override
    protected void run(Environment env, Namespace namespace,
                       ThmmyRssConfiguration config) throws Exception {
        application.getAnnouncementDAO().dropAnnouncementsTable();
        application.getAnnouncementDAO().createAnnouncementsTable();
    }
}
