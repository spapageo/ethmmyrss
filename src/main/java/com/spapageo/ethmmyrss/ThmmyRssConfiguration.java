package com.spapageo.ethmmyrss;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.Map;

public class ThmmyRssConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    @JsonProperty(value = "fetcher")
    private FetcherConfiguration fetcher = new FetcherConfiguration();

    @NotNull
    @Size(min = 1)
    private Map<Integer, String> lessonIds = Collections.emptyMap();

    public FetcherConfiguration getFetcher() {
        return fetcher;
    }

    public Map<Integer, String> getLessonIds() {
        return lessonIds;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return httpClient;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }
}
