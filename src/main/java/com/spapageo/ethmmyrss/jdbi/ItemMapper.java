/**
 *
 */
package com.spapageo.ethmmyrss.jdbi;

import com.spapageo.ethmmyrss.api.Item;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemMapper implements ResultSetMapper<Item> {

    @Override
    public Item map(int index, ResultSet r, StatementContext ctx)
            throws SQLException {
        return new Item(r.getString("title"), r.getTimestamp("date"),
                r.getString("description"), r.getInt("lessonId"), r.getString("hash"));
    }


}
