/**
 * 
 */
package com.spapageo.ethmmyrss.health;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.client.Fetcher;
import com.yammer.metrics.core.HealthCheck;

/**
 * @author Doom
 *
 */
public class FetcherHealthCheck  extends HealthCheck{

	private Fetcher f;
	private ThmmyRssConfiguration config;
	public FetcherHealthCheck(ThmmyRssConfiguration config,Fetcher f){
		super("fetcher");
		this.f = f;
		this.config = config;
	}
	@Override
	protected Result check() throws Exception {
		String jssesionid = f.login(config.getFetcher().getUsername(), config.getFetcher().getPassword(), config.getFetcher().getEthhmy_login());
		if(jssesionid.isEmpty()){
			return Result.unhealthy("Site login failed");
		}
		if(!f.lessonLogin(config.getFetcher().getEthmmy_url(), 36, jssesionid)){
			return Result.unhealthy("Site login failed");
		}else{
			if(f.lessonAnnouncements(config.getFetcher().getEthmmy_url(), 36, jssesionid).isEmpty()){
				return Result.unhealthy("Got zero announcements");
			}else{
				return Result.healthy("All is good");
			}
			
		}
	}

}
