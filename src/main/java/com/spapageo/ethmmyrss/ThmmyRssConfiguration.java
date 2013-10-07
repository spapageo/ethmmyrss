package com.spapageo.ethmmyrss;

import java.util.HashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;

/**
 * @author Doom
 *
 */
public class ThmmyRssConfiguration extends Configuration {
	@JsonProperty
	private boolean standalone = true;
	
    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();
	
    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();
    
    @Valid
    @NotNull
    @JsonProperty
	private FetcherConfiguration fetcher = new FetcherConfiguration();
    
    @NotNull
    @Size(min = 1)
    private HashMap<Integer,String> lesson_ids;
    
    public FetcherConfiguration getFetcher() {
		return fetcher;
	}
    
    public HashMap<Integer,String> getLesson_ids() {
		return lesson_ids;
	}
    
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return httpClient;
    }
    
    public DatabaseConfiguration getDatabase() {
		return database;
	}
    
    public boolean getStandalone(){
    	return standalone;
    }
}
