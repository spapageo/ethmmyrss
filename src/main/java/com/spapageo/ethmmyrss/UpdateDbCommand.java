/**
 * 
 */
package com.spapageo.ethmmyrss;

import net.sourceforge.argparse4j.inf.Namespace;

import com.spapageo.ethmmyrss.client.Fetcher;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.EnvironmentCommand;
import com.yammer.dropwizard.config.Environment;

/**
 * @author Doom
 *
 */
public class UpdateDbCommand extends EnvironmentCommand<ThmmyRssConfiguration>{

	private ThmmyRssService service;
	
	protected UpdateDbCommand(Service<ThmmyRssConfiguration> serv,
			String name, String description) {
		super(serv, name, description);
		this.service = (ThmmyRssService)serv; 
	}

	@Override
	protected void run(Environment env, Namespace arg1,
			ThmmyRssConfiguration config) throws Exception { 

	    new Fetcher(service.getClient(), config, service.getDao()).run();
	}
}
