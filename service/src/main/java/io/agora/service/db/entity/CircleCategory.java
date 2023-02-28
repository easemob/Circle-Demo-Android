package io.agora.service.db.entity;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.hyphenate.chat.EMCircleCategory;

import java.io.Serializable;

@Keep
@Entity(tableName = "circle_category")
public class CircleCategory implements Serializable {
    @PrimaryKey
    @NonNull
    public String categoryId;
    public String serverId;
    public String categoryName;
    public boolean isdefault;

    @Ignore
    public CircleCategory(String serverId, String categoryId, String categoryName) {
        this.serverId = serverId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public CircleCategory(String serverId, String categoryId, String categoryName,boolean isdefault) {
        this.serverId = serverId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.isdefault=isdefault;
    }

    @Ignore
    public CircleCategory(EMCircleCategory emCircleChannelCategory){
        serverId=emCircleChannelCategory.getServerId();
        categoryId=emCircleChannelCategory.getCategoryId();
        categoryName=emCircleChannelCategory.getName();
        isdefault=emCircleChannelCategory.isDefault();
    }

}
