package io.agora.service.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.hyphenate.chat.EMCircleChannel;
import com.hyphenate.chat.EMCircleVoiceChannel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.agora.service.db.converter.CircleUserConverter;

@Keep
@Entity(tableName = "circle_channel")
@TypeConverters({CircleUserConverter.class})
public class CircleChannel implements Serializable, Parcelable {

    @PrimaryKey
    @NonNull
    public String channelId;
    public String categoryId;
    public String serverId;
    public String name;
    public String desc;
    public String custom;
    public int inviteMode;
    public boolean isDefault;
    public int type;
    public int channelMode;//0:文字频道 1：语聊频道
    public int seatCount;
    public String rtcName;
    public List<CircleUser> channelUsers;
    public List<String> modetators;//目前暂时与server的一致

    public CircleChannel(@NonNull String channelId,String categoryId, String serverId, String name, String desc, String custom, int inviteMode, boolean isDefault,
                         int type,int channelMode,int seatCount ,String rtcName, List<CircleUser> channelUsers, List<String> modetators) {
        this.channelId = channelId;
        this.categoryId=categoryId;
        this.serverId = serverId;
        this.name = name;
        this.desc = desc;
        this.custom = custom;
        this.inviteMode = inviteMode;
        this.isDefault = isDefault;
        this.type = type;
        this.channelMode=channelMode;
        this.seatCount =seatCount;
        this.rtcName=rtcName;
        this.channelUsers = channelUsers;
        this.modetators = modetators;
    }

    @Ignore
    public CircleChannel(String serverlId, String channelId) {
        this.serverId = serverlId;
        this.channelId = channelId;
    }
    @Ignore
    public CircleChannel(String serverlId,String categoryId, String channelId) {
        this.serverId = serverlId;
        this.categoryId=categoryId;
        this.channelId = channelId;
    }

    @Ignore
    public CircleChannel(EMCircleChannel emCircleChannel) {
        this.serverId = emCircleChannel.getServerlId();
        this.categoryId=emCircleChannel.getCategoryId();
        this.channelId = emCircleChannel.getChannelId();
        this.name = emCircleChannel.getName();
        this.desc = emCircleChannel.getDesc();
        this.custom = emCircleChannel.getExt();
        this.inviteMode = 0;
        this.isDefault = emCircleChannel.isDefault();
        this.type = emCircleChannel.getType().getCode();
        this.channelMode=emCircleChannel.getMode().getCode();
        if(channelMode==1) {//语聊频道
            EMCircleVoiceChannel emCircleVoiceChannel = null;
            try {
                emCircleVoiceChannel = new EMCircleVoiceChannel(emCircleChannel);
                this.rtcName=emCircleVoiceChannel.getRtcName();
                this.seatCount =emCircleVoiceChannel.getMaxUsers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Ignore
    protected CircleChannel(Parcel in) {
        channelId = in.readString();
        categoryId=in.readString();
        serverId = in.readString();
        name = in.readString();
        desc = in.readString();
        custom = in.readString();
        inviteMode = in.readInt();
        isDefault = in.readByte() != 0;
        type = in.readInt();
        channelMode=in.readInt();
        seatCount =in.readInt();
        rtcName=in.readString();
        channelUsers = in.createTypedArrayList(CircleUser.CREATOR);
        modetators = in.createStringArrayList();
    }

    public static final Creator<CircleChannel> CREATOR = new Creator<CircleChannel>() {
        @Override
        public CircleChannel createFromParcel(Parcel in) {
            return new CircleChannel(in);
        }

        @Override
        public CircleChannel[] newArray(int size) {
            return new CircleChannel[size];
        }
    };

    public static List<CircleChannel> converToCirlceChannelList(List<EMCircleChannel> input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        List<CircleChannel> output = new ArrayList<>();
        for (EMCircleChannel emCircleChannel : input) {
            CircleChannel circleChannel = new CircleChannel(emCircleChannel);
            output.add(circleChannel);
        }
        return output;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(channelId);
        dest.writeString(categoryId);
        dest.writeString(serverId);
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeString(custom);
        dest.writeInt(inviteMode);
        dest.writeByte((byte) (isDefault ? 1 : 0));
        dest.writeInt(type);
        dest.writeInt(channelMode);
        dest.writeInt(seatCount);
        dest.writeString(rtcName);
        dest.writeTypedList(channelUsers);
        dest.writeStringList(modetators);
    }
}
