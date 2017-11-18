package com.spapageo.ethmmyrss.api.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Rss {

    @SuppressWarnings("all")
    private String version = "2.0";
    private Channel channel;

    @SuppressWarnings("all")
    Rss() {
        //Needed my raxb
    }

    public Rss(Channel channel) {
        this.channel = channel;
    }

    @XmlElement
    public Channel getChannel() {
        return channel;
    }

    @XmlAttribute
    public String getVersion() {
        return version;
    }
}
