/**
 * 
 */
package com.spapageo.thmmyrss.jdbi;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import com.spapageo.thmmyrss.api.Item;

/**
 * @author Doom
 *
 */
@RegisterMapper(ItemMapper.class)
public interface RssDAO {
	
	@SqlBatch("merge into items (hash,title,date,description,lessonId) values (:hash,:title,:date,:description,:lessonId)")
	int[] insertItems(@BindBean List<Item> items);

	@SqlUpdate("insert into items (hash,title,date,description,lessonId) values (:hash,:title,:date,:description,:lessonId)")
	int insertItem(@BindBean Item item);
	
	@SqlUpdate("create table items (hash varchar(64) primary key,title varchar(1024),date varchar(32),description varchar(max), lessonId int)")
	void createItemsTable();
	
	@SqlQuery("select hash,title,date,description,lessonId from items where lessonId = :lessonId limit 10")
	List<Item> getItemsForId(@Bind("lessonId") int lessonId);
	
	@SqlQuery("select count(*) from items")
	int getItemCount();
	
	@SqlQuery("select count(*) from items where lessonId = :lessonId")
	int getItemCountForId(@Bind("lessonId") int lessonId);
	
	@SqlUpdate("delete from items where hash not in (select hash from items where lessonId = :lessonId limit :x)")
	void deleteAllbutFirstXForId(@Bind("lessonId") int lessonId,@Bind("x") int x);
}
