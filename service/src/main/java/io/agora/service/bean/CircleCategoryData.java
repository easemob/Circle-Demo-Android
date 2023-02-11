package io.agora.service.bean;


import com.hyphenate.chat.EMCircleCategory;

import io.agora.service.db.entity.CircleCategory;

public class CircleCategoryData extends CircleCategory {
    public boolean checked;

    public CircleCategoryData(String serverId, String categoryId, String categoryName) {
        super(serverId, categoryId, categoryName);
    }

    public CircleCategoryData(EMCircleCategory emCircleChannelCategory) {
        super(emCircleChannelCategory);
    }

}
