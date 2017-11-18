package com.spapageo.thmmyrss.jdbi;

import com.spapageo.ethmmyrss.api.domain.Announcement;
import com.spapageo.ethmmyrss.jdbi.AnnouncementDAO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.StringMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnnouncementDAOTest {

    private AnnouncementDAO dao;
    private Handle handle;

    @Before
    public void setUp() throws Exception {
        DBI dbi = new DBI("jdbc:h2:mem:ethmmyrss");
        handle = dbi.open();
        dao = handle.attach(AnnouncementDAO.class);
        dao.createAnnouncementsTable();
    }

    @Test
    public void testInsertItems() {
        List<Announcement> l = new ArrayList<>();
        l.add(new Announcement("q", new Timestamp(0), "g", 1));
        l.add(new Announcement("q", new Timestamp(0), "f", 1));
        dao.insertAnnouncements(l);
        assertTrue(l.size() == 2);
    }

    @Test
    public void testCreateItemsTable() {
        handle.execute("INSERT INTO announcements (hash,title,date,description,lessonId) VALUES ('asd','add',"
                + "'2013-07-01 "
                + "23:17:00+03','azz',1)");
        String name = handle.createQuery("select hash from announcements")
                .map(StringMapper.FIRST)
                .first();

        assertEquals("asd", name);
    }

    @Test
    public void testGetItemsForId() {
        List<Announcement> l = new ArrayList<>();
        l.add(new Announcement("q", new Timestamp(0), "g", 1));
        l.add(new Announcement("q", new Timestamp(0), "g", 2));
        l.add(new Announcement("b", new Timestamp(0), "g", 1));
        l.add(new Announcement("s", new Timestamp(0), "g", 2));
        dao.insertAnnouncements(l);
        assertTrue(dao.getLast10AnnouncementsForLessonId(1).size() == 2);
    }

    @After
    public void breakDown() {
        dao.dropAnnouncementsTable();
        handle.close();
    }

}
