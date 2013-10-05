package com.spapageo.thmmyrss.api;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.Test;

public class FeedTest {

	@Test
	public void test() throws Exception {
		String target = new String(Files.readAllBytes(Paths.get(getClass().getResource("/fixtures/feed.xml").toURI())),
									Charset.forName("UTF-8"));
		JAXBContext con = JAXBContext.newInstance(Rss.class);
		Marshaller mars = con.createMarshaller();
		mars.setProperty("jaxb.formatted.output", false);
		mars.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		StringWriter sw = new StringWriter();
		List<Item> l = new ArrayList<>();
		l.add(new Item("title","pubDate","description",1));
		mars.marshal(new Rss(new Channel("description","http://alexander.ee.auth.gr:8083/eTHMMY/","title",l)), sw);
		assertTrue(target.equals(sw.toString()));
	}

}
