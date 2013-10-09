/**
 * 
 */
package com.spapageo.ethmmyrss.api;


import java.sql.Timestamp;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
/**
 * @author Doom
 *
 */
public class TimestampAdapter extends XmlAdapter<String, Timestamp> {

    private final DateTimeFormatter rssform = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");
    
    @Override
    public String marshal(Timestamp v) throws Exception {
        return rssform.print(v.getTime());
    }

    @Override
    public Timestamp unmarshal(String v) throws Exception {
        return new Timestamp(rssform.parseDateTime(v).toDate().getTime());
    }

}