package com.spapageo.ethmmyrss;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

public class FetcherConfiguration {

    @NotBlank
    @JsonProperty
    private String ethmmyUrl;

    @NotBlank
    @JsonProperty
    private String ethmmyLogin;

    @NotBlank
    @JsonProperty
    private String username;

    @NotBlank
    @JsonProperty
    private String password;

    public String getEthmmyUrl() {
        return ethmmyUrl;
    }

    public String getEthmmyLogin() {
        return ethmmyLogin;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
