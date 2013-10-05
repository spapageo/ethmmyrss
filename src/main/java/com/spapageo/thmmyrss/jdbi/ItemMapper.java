/**
 * 
 */
package com.spapageo.thmmyrss.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.spapageo.thmmyrss.api.Item;

/**
 * @author Doom
 *
 */
public class ItemMapper implements ResultSetMapper<Item> {

	@Override
	public Item map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new Item(r.getString("title"),r.getString("date"),
						r.getString("description"),r.getInt("lessonId"),r.getString("hash"));
	}
	

}
