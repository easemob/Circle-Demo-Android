package io.agora.service.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agora.service.db.entity.CircleMute;

@Dao
public interface CircleMuteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(CircleMute... muteInfo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<CircleMute> muteInfos);

    @Delete
    void delete(CircleMute muteInfo);

    @Query("DELETE FROM circle_mute WHERE channelId=:id")
    int deleteByChannelId(String id);

    @Update
    void updateMuteInfo(CircleMute muteInfo);

    @Query("SELECT * FROM circle_mute")
    List<CircleMute> getAllMuteInfos();

    @Query("SELECT * FROM circle_mute WHERE channelId = :id LIMIT 1")
    CircleMute getMuteInfoByChannelId(String id);
}
