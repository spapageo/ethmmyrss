package com.spapageo.ethmmyrss.api.domain;

import com.google.common.hash.Hashing;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Timestamp;

import static java.nio.charset.StandardCharsets.UTF_8;

@XmlType(propOrder = {"title", "link", "date", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Announcement {
    @XmlElement
    private String link;
    @XmlElement
    private String title;
    @XmlElement(name = "pubDate")
    @XmlJavaTypeAdapter(TimestampAdapter.class)
    private Timestamp date;
    @XmlElement
    private String description;
    @XmlTransient
    private String hash;
    @XmlTransient
    private int lessonId;

    @SuppressWarnings("all")
    Announcement() {
        //Needed my raxb
    }

    public Announcement(String title, Timestamp date, String description, int lessonId) {
        this(title, date, description, lessonId, Hashing.sha256().newHasher().putString(title, UTF_8)
                .putString(date.toString(), UTF_8).putString(description, UTF_8).putInt(lessonId)
                .hash().toString());
    }

    public Announcement(String title, Timestamp date, String description, int lessonId, String hash) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.lessonId = lessonId;
        this.hash = hash;
        this.link = "http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.login.do?method=execute&PRMID=" + lessonId;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getHash() {
        return hash;
    }

    public int getLessonId() {
        return lessonId;
    }

    public String getLink() {
        return link;
    }
}
