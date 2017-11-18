package com.spapageo.ethmmyrss.jdbi;

import com.spapageo.ethmmyrss.api.domain.Announcement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AnnouncementMapper implements ResultSetMapper<Announcement> {

    @Override
    public Announcement map(int index, ResultSet r, StatementContext ctx)
            throws SQLException {
        return new Announcement(r.getString("title"), r.getTimestamp("date"),
                r.getString("description"), r.getInt("lessonId"), r.getString("hash"));
    }

}
