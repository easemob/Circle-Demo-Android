package io.agora.service.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agora.service.db.entity.CircleCategory;

@Dao
public interface CircleCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(CircleCategory... categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<CircleCategory> categories);

    @Delete
    void delete(CircleCategory category);

    @Query("DELETE FROM circle_category WHERE categoryId=:id")
    int deleteByCagegoryId(String id);

    @Update
    void updateCategory(CircleCategory category);

    @Query("SELECT * FROM circle_category")
    List<CircleCategory> getAllCategories();

    @Query("SELECT * FROM circle_category WHERE categoryId = :id LIMIT 1")
    CircleCategory getCategoryByCagegoryID(String id);

    @Query("SELECT * FROM circle_category WHERE serverId = :serverID")
    LiveData<List<CircleCategory>> getCategoriesByChannelServerIdLiveData(String serverID);

    @Query("SELECT * FROM circle_category WHERE serverId = :serverID")
    List<CircleCategory> getCategoriesByChannelServerId(String serverID);

}
