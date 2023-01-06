package io.agora.contacts.bean;


import com.hyphenate.chat.EMCircleChannelCategory;

import io.agora.service.db.entity.CircleCategory;

public class CircleCategoryData extends CircleCategory {
    public boolean checked;

    public CircleCategoryData(String serverId, String categoryId, String categoryName) {
        super(serverId, categoryId, categoryName);
    }

    public CircleCategoryData(EMCircleChannelCategory emCircleChannelCategory) {
        super(emCircleChannelCategory);
    }

}
