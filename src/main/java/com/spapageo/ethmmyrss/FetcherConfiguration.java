package com.spapageo.ethmmyrss;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetcherConfiguration {
	
	@NotNull
	@NotBlank
	@JsonProperty
	private String ethmmy_url;
	
	@NotNull
	@NotBlank
	@JsonProperty
	private String ethhmy_login;
	
	@NotNull
	@NotBlank
	@JsonProperty
	private String username;
	
	@NotNull
	@NotBlank
	@JsonProperty
	private String password;
	
	public String getEthmmy_url() {
		return ethmmy_url;
	}
	
	public String getEthhmy_login() {
		return ethhmy_login;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
