/**
 * 
 */
package com.spapageo.ethmmyrss;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.skife.jdbi.v2.DBI;
import org.slf4j.LoggerFactory;

import com.spapageo.ethmmyrss.client.Fetcher;
import com.spapageo.ethmmyrss.health.FetcherHealthCheck;
import com.spapageo.ethmmyrss.jdbi.RssDAO;
import com.spapageo.ethmmyrss.resourses.FeedResource;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.jdbi.bundles.DBIExceptionsBundle;


/**
 * @author Doom
 *
 */
public class ThmmyRssService extends Service<ThmmyRssConfiguration> {

	private Fetcher fetcher;
	private DBIFactory factory;
	private DBI jdbi;
	private RssDAO dao;
	private Client client;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
	    
		//check if we are running on heroku
	    if(System.getenv("DATABASE_URL") != null){
	    	URI dbUri = new URI(System.getenv("DATABASE_URL"));
	    	String username = dbUri.getUserInfo().split(":")[0];
	    	String password = dbUri.getUserInfo().split(":")[1];
	    	String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

	    	System.setProperty("dw.http.port", System.getenv("PORT"));
	    	System.setProperty("dw.http.adminPort", System.getenv("PORT"));
	    	
	    	System.setProperty("dw.database.user", username);
	    	System.setProperty("dw.database.password", password);
	    	System.setProperty("dw.database.url", dbUrl);
	    	System.setProperty("dw.standalone","false");
	    }
		new ThmmyRssService().run(args);
	}

	@Override
	public void initialize(Bootstrap<ThmmyRssConfiguration> bootstrap) {
		bootstrap.setName("Thmmy Rss");
		bootstrap.addCommand(new UpdateDbCommand(this, "updatedb", "Update the rss database."));
		bootstrap.addBundle(new DBIExceptionsBundle());
		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
	}

	@Override
	public void run(ThmmyRssConfiguration config, Environment env)
			throws Exception {
	    client = new JerseyClientBuilder().using(config.getJerseyClientConfiguration()).using(env).withProperty("PROPERTY_CHUNKED_ENCODING_SIZE", 0).build();
	    factory = new DBIFactory();
	    jdbi = factory.build(env, config.getDatabase(), "mysql");
	    dao = jdbi.onDemand(RssDAO.class);
	    try{
	    	//dao.droptable();
	    	dao.createItemsTable();
	    }catch(Exception e){
	    	LoggerFactory.getLogger(getClass()).warn("Table probably already exists. Trying to continue",e);
	    }
	    this.fetcher = new Fetcher(client,config,dao);
	    if(config.getStandalone()){
		    final ScheduledExecutorService exec = env.managedScheduledExecutorService("Ethmmy Parser Executor Service 1", 2);
		    exec.scheduleWithFixedDelay(fetcher, 0 , 30, TimeUnit.MINUTES);
	    }
	    env.addHealthCheck(new FetcherHealthCheck(config, fetcher));
	    env.addResource(new FeedResource(dao,config));
	}

	public Fetcher getFetcher() {
		return fetcher;
	}

	public DBIFactory getFactory() {
		return factory;
	}

	public DBI getJdbi() {
		return jdbi;
	}

	public RssDAO getDao() {
		return dao;
	}

	public Client getClient() {
		return client;
	}

}
