package com.spapageo.ethmmyrss.api.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(propOrder = {"title", "link", "description", "announcements"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Channel {
    @XmlElement
    private String description;
    @XmlElement
    private String link;
    @XmlElement
    private String title;
    @XmlElement(name = "item")
    private List<Announcement> announcements;

    @SuppressWarnings("all")
    Channel() {
        //Needed my raxb
    }

    public Channel(String description, String link, String title, List<Announcement> announcements) {
        this.description = description;
        this.link = link;
        this.title = title;
        this.announcements = announcements;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }
}
