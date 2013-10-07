/**
 * 
 */
package com.spapageo.ethmmyrss.api;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Doom
 *
 */
@XmlRootElement
public class Rss {
	public Rss(){}
	
	public Rss(Channel ch){
		this.channel = ch;
	}
	public Channel getChannel() {
		return channel;
	}
	
	@XmlElement
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	

	@XmlAttribute
	public String version = "2.0";
	
	@NotNull
	private Channel channel;

	
}
