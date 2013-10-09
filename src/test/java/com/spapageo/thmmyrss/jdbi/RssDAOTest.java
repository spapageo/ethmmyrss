package com.spapageo.thmmyrss.jdbi;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.StringMapper;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.Item;
import com.spapageo.ethmmyrss.jdbi.RssDAO;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;

public class RssDAOTest {

	private RssDAO dao;
	DBI dbi;
	Handle h;
	
	@Before
	public void setUp() throws Exception {
		String configFile = getClass().getResource("/thmmyrss.yml").getFile();
		ThmmyRssConfiguration config = ConfigurationFactory.forClass(ThmmyRssConfiguration.class, new Validator()).build(new File(configFile));
		Environment env = new Environment("Test env", config, new ObjectMapperFactory(), new Validator());
	    h = new DBIFactory().build(env, config.getDatabase(), "mysql").open();
		this.dao = h.attach(RssDAO.class);
	}

	@Test
	public void testInsertItems() {
		List<Item> l = new ArrayList<>();
		l.add(new Item("q",new Timestamp(0),"g",1));
		l.add(new Item("q",new Timestamp(0),"f",1));
		dao.createItemsTable();
		dao.insertItems(l);
		assertTrue(l.size() == 2);
	}

	@Test
	public void testInsertItem() {
		//h.execute("drop table items");
		dao.createItemsTable();
		dao.insertItem(new Item("a", new Timestamp(0), "a",3));
		String name = h.createQuery("select title from items")
                .map(StringMapper.FIRST)
                .first();
		assertTrue(name.equals("a"));
	}

	@Test
	public void testCreateItemsTable() {
		dao.createItemsTable();
		h.execute("insert into items (hash,title,date,description,lessonId) values ('asd','2013-07-01 23:17:00+03','add','azz',1)");
		String name = h.createQuery("select hash from items")
                .map(StringMapper.FIRST)
                .first();
		assertTrue(name.equals("asd"));
	}

	@Test
	public void testGetItemsForId() {
		List<Item> l = new ArrayList<>();
		l.add(new Item("q",new Timestamp(0),"g",1));
		l.add(new Item("q",new Timestamp(0),"g",2));
		l.add(new Item("b",new Timestamp(0),"g",1));
		l.add(new Item("s",new Timestamp(0),"g",2));
		dao.createItemsTable();
		dao.insertItems(l);
		assertTrue(dao.getItemsForId(1).size() == 2);
	}
	
	@Test
	public void testGetItemCount(){
		dao.createItemsTable();
		dao.insertItem(new Item("a", new Timestamp(0), "a",3));
		dao.insertItem(new Item("a", new Timestamp(0), "a",2));
		assertTrue(2 == dao.getItemCount());
	}
	
	@Test
	public void testGetItemCountForId(){
		dao.createItemsTable();
		dao.insertItem(new Item("a", new Timestamp(0), "a",3));
		dao.insertItem(new Item("a", new Timestamp(0), "a",2));
		assertTrue(1 == dao.getItemCountForId(2));
		assertTrue(0 == dao.getItemCountForId(1));
	}
	
	@Test
	public void keepFirstXForId(){
		dao.createItemsTable();
		dao.insertItem(new Item("a", new Timestamp(0), "a",3));
		dao.insertItem(new Item("a", new Timestamp(0), "a",2));
		dao.insertItem(new Item("b", new Timestamp(0), "a",2));
		dao.deleteAllbutFirstXForId(2, 1);
		assertTrue(1 == dao.getItemCountForId(2));
	}
	
	@After
	public void breakDown(){
		h.close();
	}

}
