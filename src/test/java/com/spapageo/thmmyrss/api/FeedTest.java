package com.spapageo.thmmyrss.api;

import com.spapageo.ethmmyrss.api.Channel;
import com.spapageo.ethmmyrss.api.Item;
import com.spapageo.ethmmyrss.api.Rss;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FeedTest {

    private Marshaller marshaller;

    @Before
    public void setUp() throws Exception {
        JAXBContext con = JAXBContext.newInstance(Rss.class);
        marshaller = con.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", false);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    }

    @Test
    public void testRssFeedSerialization() throws Exception {
        String target = new String(Files.readAllBytes(Paths.get(getClass().getResource("/fixtures/feed.xml").toURI())),
                Charset.forName("UTF-8"));

        StringWriter sw = new StringWriter();
        List<Item> items = Collections.singletonList(new Item("title", new Timestamp(0), "description", 1));
        marshaller.marshal(new Rss(new Channel("description", "http://alexander.ee.auth.gr:8083/eTHMMY/", "title", items)), sw);
        assertEquals(target, sw.toString());
    }

}
