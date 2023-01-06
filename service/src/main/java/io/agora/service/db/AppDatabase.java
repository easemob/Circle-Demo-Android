package io.agora.service.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.agora.service.db.converter.FormatConverter;
import io.agora.service.db.dao.CircleCategoryDao;
import io.agora.service.db.dao.CircleChannelDao;
import io.agora.service.db.dao.CircleMuteDao;
import io.agora.service.db.dao.CircleServerDao;
import io.agora.service.db.dao.CircleUserDao;
import io.agora.service.db.entity.CircleCategory;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleMute;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.db.entity.CircleUser;


@Database(entities = {CircleServer.class, CircleChannel.class, CircleUser.class, CircleMute.class, CircleCategory.class},
        version = 2
//        , autoMigrations = {@AutoMigration(from = 1, to = 2)}想使用数据库自动迁移功能可以用这个
)
@TypeConverters(FormatConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CircleUserDao userDao();

    public abstract CircleServerDao serverDao();

    public abstract CircleChannelDao channelDao();

    public abstract CircleCategoryDao categoryDao();

    public abstract CircleMuteDao muteDao();
}

