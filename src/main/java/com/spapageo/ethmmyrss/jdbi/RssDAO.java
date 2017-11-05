/**
 *
 */
package com.spapageo.ethmmyrss.jdbi;

import com.spapageo.ethmmyrss.api.Item;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(ItemMapper.class)
public interface RssDAO {

    @SqlBatch("INSERT INTO items(hash,title,date,description,lessonId) SELECT :hash,:title,:date,:description,:lessonId WHERE NOT exists (SELECT hash FROM items WHERE hash = :hash)")
    void insertItems(@BindBean List<Item> items);

    @SqlUpdate("INSERT INTO items(hash,title,date,description,lessonId) VALUES (:hash,:title,:date,:description,:lessonId)")
    void insertItem(@BindBean Item item);

    @SqlUpdate("CREATE TABLE items(hash VARCHAR(64) PRIMARY KEY,title VARCHAR(1024),date TIMESTAMP,description TEXT, lessonId INT)")
    void createItemsTable();

    @SqlQuery("SELECT hash,title,date,description,lessonId FROM items WHERE lessonId = :lessonId ORDER BY date DESC LIMIT 10")
    List<Item> getItemsForId(@Bind("lessonId") int lessonId);

    @SqlQuery("SELECT count(*) FROM items")
    int getItemCount();

    @SqlQuery("SELECT count(*) FROM items WHERE lessonId = :lessonId")
    int getItemCountForId(@Bind("lessonId") int lessonId);

    @SqlUpdate("DELETE FROM items WHERE hash NOT IN (SELECT hash FROM items WHERE lessonId = :lessonId LIMIT :x)")
    void deleteAllButFirstXForId(@Bind("lessonId") int lessonId, @Bind("x") int x);

    @SqlUpdate("DROP TABLE items")
    void dropItemsTable();
}
