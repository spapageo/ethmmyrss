package com.spapageo.ethmmyrss.api;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.validator.constraints.NotEmpty;

@XmlRootElement
@XmlType(propOrder={"title", "link","description","item"})
public class Channel {
	@NotNull
	@NotEmpty
	private String description;
	@NotNull
	@NotEmpty
	private String link;
	@NotNull
	@NotEmpty
	private String title;
	@NotNull
	private List<Item> item;
	
	public Channel(){}
	
	public Channel(String description,String link,String title,List<Item> l){
		this.description = description;
		this.link = link;
		this.title = title;
		this.item = l;
	}
	
	public String getDescription() {
		return description;
	}
	@XmlElement
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public String getLink() {
		return link;
	}
	@XmlElement
	public void setLink(String link) {
		this.link = link;
	}
	
	
	public String getTitle() {
		return title;
	}
	@XmlElement
	public void setTitle(String title) {
		this.title = title;
	}
	
	public List<Item> getItem() {
		return item;
	}

	@XmlElement
	public void setItem(List<Item> item) {
		this.item = item;
	}
}
