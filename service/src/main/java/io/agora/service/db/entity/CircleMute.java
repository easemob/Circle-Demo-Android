package io.agora.service.db.entity;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * channel新消息是否通知信息存储类
 */
@Keep
@Entity(tableName = "circle_mute")
public class CircleMute {
   @PrimaryKey
   @NonNull
   public String channelId;
   public boolean isMuted;//是否禁言

   public CircleMute(@NonNull String channelId, boolean isMuted) {
      this.channelId = channelId;
      this.isMuted = isMuted;
   }
   @Ignore
   public CircleMute(@NonNull String channelId) {
      this.channelId = channelId;
   }

}
