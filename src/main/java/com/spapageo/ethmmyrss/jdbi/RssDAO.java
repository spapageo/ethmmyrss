/**
 * 
 */
package com.spapageo.ethmmyrss.jdbi;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import com.spapageo.ethmmyrss.api.Item;

/**
 * @author Doom
 *
 */
@RegisterMapper(ItemMapper.class)
public interface RssDAO {
	
	@SqlBatch("insert into \"items\" (hash,title,date,description,lessonId) select :hash,:title,:date,:description,:lessonId where not exists (select hash from \"items\" where hash = :hash)")
	void insertItems(@BindBean List<Item> items);

	@SqlUpdate("insert into \"items\" (hash,title,date,description,lessonId) values (:hash,:title,:date,:description,:lessonId)")
	void insertItem(@BindBean Item item);
	
	@SqlUpdate("create table \"items\" (hash varchar(64) primary key,title varchar(1024),date timestamp with time zone,description text, lessonId int)")
	void createItemsTable();
	
	@SqlQuery("select hash,title,date,description,lessonId from \"items\" where lessonId = :lessonId order by date desc limit 10")
	List<Item> getItemsForId(@Bind("lessonId") int lessonId);
	
	@SqlQuery("select count(*) from \"items\"")
	int getItemCount();
	
	@SqlQuery("select count(*) from \"items\" where lessonId = :lessonId")
	int getItemCountForId(@Bind("lessonId") int lessonId);
	
	@SqlUpdate("delete from \"items\" where hash not in (select hash from items where lessonId = :lessonId limit :x)")
	void deleteAllbutFirstXForId(@Bind("lessonId") int lessonId,@Bind("x") int x);
}
