package io.agora.service.db;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hyphenate.util.EMLog;

import io.agora.common.utils.MD5;
import io.agora.service.db.dao.CircleCategoryDao;
import io.agora.service.db.dao.CircleChannelDao;
import io.agora.service.db.dao.CircleMuteDao;
import io.agora.service.db.dao.CircleServerDao;
import io.agora.service.db.dao.CircleUserDao;


public class DatabaseManager {
    private final String TAG = getClass().getSimpleName();
    private static DatabaseManager instance;
    private Context mContext;
    private String currentUser;
    private AppDatabase mDatabase;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private DatabaseManager(){
    }

    public static DatabaseManager getInstance() {
        if(instance == null) {
            synchronized (DatabaseManager.class) {
                if(instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }
    public void init(Context context){
        mContext=context.getApplicationContext();
    }

    /**
     * Initialize the database
     * @param user
     */
    public void initDB(String user) {
        if(currentUser != null) {
            if(TextUtils.equals(currentUser, user)) {
                EMLog.i(TAG, "you have opened the db");
                return;
            }
            closeDb();
        }
        this.currentUser = user;
        String userMd5 = MD5.encrypt2MD5(user);
        // The following database upgrade settings, in order to upgrade the database will clear the previous data,
        // if you want to keep the data, use this method carefully
        // You can use addMigrations() to upgrade the database
        String dbName = String.format("em_%1$s.db", userMd5);
        EMLog.i(TAG, "db name = "+dbName);
        mDatabase = Room.databaseBuilder(mContext, AppDatabase.class, dbName)
                        .allowMainThreadQueries()
                        .addMigrations(migration1_2)
                        .fallbackToDestructiveMigrationOnDowngrade()
                        .fallbackToDestructiveMigration()
                        .build();
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreatedObservable() {
        return mIsDatabaseCreated;
    }

    /**
     * Close database
     */
    public void closeDb() {
        if(mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
        currentUser = null;
    }

    public CircleUserDao getUserDao() {
        if(mDatabase != null) {
            return mDatabase.userDao();
        }
        EMLog.i(TAG, "get userDao failed, should init db first");
        return null;
    }

    public CircleServerDao getServerDao() {
        if(mDatabase != null) {
            return mDatabase.serverDao();
        }
        EMLog.i(TAG, "get ServerDao failed, should init db first");
        return null;
    }
    public CircleChannelDao getChannelDao() {
        if(mDatabase != null) {
            return mDatabase.channelDao();
        }
        EMLog.i(TAG, "get ChannelDao failed, should init db first");
        return null;
    }
    public CircleCategoryDao getCagegoryDao() {
        if(mDatabase != null) {
            return mDatabase.categoryDao();
        }
        EMLog.i(TAG, "get CagegoryDao failed, should init db first");
        return null;
    }
    public CircleMuteDao getCircleMuteDao() {
        if(mDatabase != null) {
            return mDatabase.muteDao();
        }
        EMLog.i(TAG, "get CircleMuteDao failed, should init db first");
        return null;
    }

    private Migration migration1_2=new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //插入categoryId列
            database.execSQL("ALTER TABLE circle_channel ADD COLUMN categoryId TEXT DEFAULT NULL");
            //插入channelMode列
            database.execSQL("ALTER TABLE circle_channel ADD COLUMN channelMode INTEGER DEFAULT 0");
            //插入seatcount列
            database.execSQL("ALTER TABLE circle_channel ADD COLUMN seatCount INTEGER DEFAULT 0");
            //插入rtcName列
            database.execSQL("ALTER TABLE circle_channel ADD COLUMN rtcName TEXT DEFAULT NULL");
            //插入type列
            database.execSQL("ALTER TABLE circle_server ADD COLUMN type INTEGER DEFAULT 0");
        }
    };
}
