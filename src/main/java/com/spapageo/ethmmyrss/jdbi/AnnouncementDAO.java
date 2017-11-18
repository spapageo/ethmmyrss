package com.spapageo.ethmmyrss.jdbi;

import com.spapageo.ethmmyrss.api.domain.Announcement;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(AnnouncementMapper.class)
public interface AnnouncementDAO {

    @SqlBatch("INSERT INTO announcements(hash,title,date,description,lessonId) SELECT :hash,:title,:date,"
            + ":description,:lessonId WHERE NOT exists (SELECT hash FROM announcements WHERE hash = :hash)")
    void insertAnnouncements(@BindBean List<Announcement> announcements);

    @SqlUpdate("CREATE TABLE announcements(hash VARCHAR(64) NOT NULL PRIMARY KEY ,title VARCHAR(1024) NOT NULL,date "
            + "TIMESTAMP"
            + " NOT NULL , description TEXT NOT NULL , lessonId INT NOT NULL )")
    void createAnnouncementsTable();

    @SqlQuery("SELECT hash,title,date,description,lessonId FROM announcements WHERE lessonId = :lessonId ORDER BY "
            + "date DESC LIMIT 10")
    List<Announcement> getLast10AnnouncementsForLessonId(@Bind("lessonId") int lessonId);

    @SqlUpdate("DROP TABLE announcements")
    void dropAnnouncementsTable();
}
