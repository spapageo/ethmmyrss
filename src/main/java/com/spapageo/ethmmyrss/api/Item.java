/**
 * 
 */
package com.spapageo.ethmmyrss.api;

import java.sql.Timestamp;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.hash.Hashing;

/**
 * @author Doom
 *
 */
@XmlRootElement
@XmlType(propOrder={"title", "link","date","description"})
public class Item {
	@NotEmpty
	@NotNull
	private String link;
	@NotEmpty
	@NotNull
	private String title;
	@NotEmpty
	@NotNull
	private Timestamp date;
	@NotEmpty
	@NotNull
	private String description;
	@NotEmpty
	@NotNull
	private String hash;
	@NotEmpty
	@NotNull
	@Min(0)
	private int lessonId;
	
	public Item(){}
	
	public Item(String title,Timestamp date,String description,int lessonId){
		this.title = title;
		this.date = date;
		this.description = description;
		this.lessonId = lessonId;
		this.link = "http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.login.do?method=execute&PRMID=" + lessonId;
		this.hash = Hashing.sha256().newHasher().putString(title)
									.putString(date.toString()).putString(description).putInt(lessonId)
									.hash().toString();
	}
	
	public Item(String title,Timestamp date,String description,int lessonId,String hash){
		this.title = title;
		this.date = date;
		this.description = description;
		this.lessonId = lessonId;
		this.hash = hash;
		this.link = "http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.login.do?method=execute&PRMID=" + lessonId;
	}
	@XmlElement
	public String getTitle() {
		return title;
	}
	@XmlElement(name = "pubDate")
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	public Timestamp getDate() {
		return date;
	}
	@XmlElement
	public String getDescription() {
		return description;
	}
	@XmlTransient
	public String getHash() {
		return hash;
	}
	@XmlTransient
	public int getLessonId() {
		return lessonId;
	}
	@XmlElement
	public String getLink(){
		return link;
	}
	
	public void setLink(String link){
		this.link = link;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setLessonId(int lessonId) {
		this.lessonId = lessonId;
	}
}
