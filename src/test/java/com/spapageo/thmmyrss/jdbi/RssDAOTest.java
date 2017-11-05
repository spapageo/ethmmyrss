package com.spapageo.thmmyrss.jdbi;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.Item;
import com.spapageo.ethmmyrss.jdbi.RssDAO;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.StringMapper;
import org.skife.jdbi.v2.util.TimestampMapper;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RssDAOTest {

    private RssDAO dao;
    private Handle h;

    @Before
    public void setUp() throws Exception {
        String configFile = getClass().getResource("/thmmyrss.yml").getFile();
        ThmmyRssConfiguration config = ConfigurationFactory.forClass(ThmmyRssConfiguration.class, new Validator()).build(new File(configFile));
        Environment env = new Environment("Test env", config, new ObjectMapperFactory(), new Validator());
        h = new DBIFactory().build(env, config.getDatabase(), "mysql").open();
        this.dao = h.attach(RssDAO.class);
        dao.createItemsTable();
    }

    @Test
    public void testInsertItems() {
        List<Item> l = new ArrayList<>();
        l.add(new Item("q", new Timestamp(0), "g", 1));
        l.add(new Item("q", new Timestamp(0), "f", 1));
        dao.insertItems(l);
        assertTrue(l.size() == 2);
    }

    @Test
    public void testInsertItem() {
        dao.insertItem(new Item("a", new Timestamp(0), "a", 3));
        String name = h.createQuery("select title from items")
                .map(StringMapper.FIRST)
                .map(StringMapper.FIRST)
                .first();
        assertEquals("a", name);

        Timestamp timestamp = h.createQuery("select date from items")
                .map(TimestampMapper.FIRST)
                .first();
        assertEquals(new Timestamp(0), timestamp);
    }

    @Test
    public void testCreateItemsTable() {
        h.execute("INSERT INTO items (hash,title,date,description,lessonId) VALUES ('asd','add','2013-07-01 "
                + "23:17:00+03','azz',1)");
        String name = h.createQuery("select hash from items")
                .map(StringMapper.FIRST)
                .first();

        assertEquals("asd", name);
    }

    @Test
    public void testGetItemsForId() {
        List<Item> l = new ArrayList<>();
        l.add(new Item("q", new Timestamp(0), "g", 1));
        l.add(new Item("q", new Timestamp(0), "g", 2));
        l.add(new Item("b", new Timestamp(0), "g", 1));
        l.add(new Item("s", new Timestamp(0), "g", 2));
        dao.insertItems(l);
        assertTrue(dao.getItemsForId(1).size() == 2);
    }

    @Test
    public void testGetItemCount() {
        dao.insertItem(new Item("a", new Timestamp(0), "a", 3));
        dao.insertItem(new Item("a", new Timestamp(0), "a", 2));
        assertTrue(2 == dao.getItemCount());
    }

    @Test
    public void testGetItemCountForId() {
        dao.insertItem(new Item("a", new Timestamp(0), "a", 3));
        dao.insertItem(new Item("a", new Timestamp(0), "a", 2));
        assertTrue(1 == dao.getItemCountForId(2));
        assertTrue(0 == dao.getItemCountForId(1));
    }

    @Test
    public void keepFirstXForId() {
        dao.insertItem(new Item("a", new Timestamp(0), "a", 3));
        dao.insertItem(new Item("a", new Timestamp(0), "a", 2));
        dao.insertItem(new Item("b", new Timestamp(0), "a", 2));
        dao.deleteAllButFirstXForId(2, 1);
        assertTrue(1 == dao.getItemCountForId(2));
    }

    @After
    public void breakDown() {
        dao.dropItemsTable();
        h.close();
    }

}
