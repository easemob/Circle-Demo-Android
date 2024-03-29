package io.agora.service.db.entity;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.hyphenate.chat.EMCircleServer;
import com.hyphenate.chat.EMCircleServerType;
import com.hyphenate.chat.EMCircleTag;
import com.hyphenate.chat.adapter.EMACircleServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.agora.service.db.converter.CircleChannelConverter;
import io.agora.service.db.converter.TagConverter;

@Keep
@Entity(tableName = "circle_server")
@TypeConverters({TagConverter.class, CircleChannelConverter.class})
public class CircleServer implements Parcelable, Serializable {

    @PrimaryKey
    @NonNull
    public String serverId;
    public String defaultChannelID;
    public String name;
    public String icon;
    public String background;
    public String desc;
    public String custom;
    public String owner;
    public List<Tag> tags;
    //以下为demo层扩展的属性
    public List<CircleChannel> channels;
    public List<String> modetators;
    public boolean isRecommand;
    public boolean isJoined;
    public int type;

    @Ignore
    protected CircleServer(Parcel in) {
        serverId = in.readString();
        defaultChannelID = in.readString();
        name = in.readString();
        icon = in.readString();
        background = in.readString();
        desc = in.readString();
        custom = in.readString();
        owner = in.readString();
        tags = in.createTypedArrayList(Tag.CREATOR);
        channels = in.createTypedArrayList(CircleChannel.CREATOR);
        modetators = in.createStringArrayList();
        isRecommand = in.readByte() != 0;
        isJoined = in.readByte() != 0;
        type = in.readInt();
    }

    public static final Creator<CircleServer> CREATOR = new Creator<CircleServer>() {
        @Override
        public CircleServer createFromParcel(Parcel in) {
            return new CircleServer(in);
        }

        @Override
        public CircleServer[] newArray(int size) {
            return new CircleServer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serverId);
        dest.writeString(defaultChannelID);
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeString(background);
        dest.writeString(desc);
        dest.writeString(custom);
        dest.writeString(owner);
        dest.writeTypedList(tags);
        dest.writeTypedList(channels);
        dest.writeStringList(modetators);
        dest.writeByte((byte) (isRecommand ? 1 : 0));
        dest.writeByte((byte) (isJoined ? 1 : 0));
        dest.writeInt(type);
    }


    @Keep
    @Entity
    public static class Tag implements Serializable, Parcelable {
        @PrimaryKey
        @NonNull
        public String server_tag_id;
        public String tag_name;
        public String serverId;

        public Tag(String id, String name, String serverId) {
            this.server_tag_id = id;
            this.tag_name = name;
            this.serverId = serverId;
        }

        @Ignore
        public Tag(EMCircleTag tag) {
            this.server_tag_id = tag.getId();
            this.tag_name = tag.getName();
        }

        @Ignore
        public static List<Tag> EMTagsConvertToTags(List<EMCircleTag> emTags) {
            List<Tag> tags = new ArrayList<>();
            if (emTags != null) {
                for (EMCircleTag emTag : emTags) {
                    tags.add(new Tag(emTag));
                }
            }
            return tags;
        }

        @Ignore
        protected Tag(Parcel in) {
            server_tag_id = in.readString();
            tag_name = in.readString();
            serverId = in.readString();
        }

        public static final Creator<Tag> CREATOR = new Creator<Tag>() {
            @Override
            public Tag createFromParcel(Parcel in) {
                return new Tag(in);
            }

            @Override
            public Tag[] newArray(int size) {
                return new Tag[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(server_tag_id);
            dest.writeString(tag_name);
            dest.writeString(serverId);
        }
    }

    @Ignore
    public CircleServer() {
    }

    public CircleServer(@NonNull String serverId, String defaultChannelID, String name, String icon,String background, String desc, String custom, String owner, List<Tag> tags, List<CircleChannel> channels, List<String> modetators, boolean isRecommand, boolean isJoined,int type) {
        this.serverId = serverId;
        this.defaultChannelID = defaultChannelID;
        this.name = name;
        this.icon = icon;
        this.background = background;
        this.desc = desc;
        this.custom = custom;
        this.owner = owner;
        this.tags = tags;
        this.channels = channels;
        this.modetators = modetators;
        this.isRecommand = isRecommand;
        this.isJoined = isJoined;
        this.type=type;
    }

    @Ignore
    public CircleServer(EMCircleServer emCircleServer) {
        this.serverId = emCircleServer.getServerId();
        this.name = emCircleServer.getName();
        this.defaultChannelID = emCircleServer.getDefaultChannelID();
        this.icon = emCircleServer.getIcon();
        this.background = emCircleServer.getBackground();
        this.desc = emCircleServer.getDesc();
        this.custom = emCircleServer.getExt();
        this.owner = emCircleServer.getOwner();
        this.type=emCircleServer.getType().getCode();
        List<EMCircleTag> tags = emCircleServer.getTags();
        List<Tag> tagList = new ArrayList<>();
        if (tags != null) {
            for (int i = 0; i < tags.size(); i++) {
                tagList.add(new Tag(tags.get(i).getId(), tags.get(i).getName(), serverId));
            }
        }
        this.tags = tagList;

    }

    @Ignore
    public static List<EMCircleServer> converToEMCirlceServerList(List<CircleServer> input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        List<EMCircleServer> output = new ArrayList<>();
        ArrayList<String> tagList = new ArrayList<>();
        for (CircleServer circleServer : input) {
            EMCircleServer emCircleServer = new EMCircleServer(new EMACircleServer());
            emCircleServer.setServerId(circleServer.serverId);
            emCircleServer.setName(circleServer.name);
            emCircleServer.setIcon(circleServer.icon);
            emCircleServer.setBackground(circleServer.background);
            emCircleServer.setDesc(circleServer.desc);
            emCircleServer.setExt(circleServer.custom);
            emCircleServer.setOwner(circleServer.owner);
            if(circleServer.type==0) {
                emCircleServer.setType(EMCircleServerType.EM_CIRCLE_SERVER_TYPE_PUBLIC);
            }else if(circleServer.type==1) {
                emCircleServer.setType(EMCircleServerType.EM_CIRCLE_SERVER_TYPE_PRIVATE);
            }
            tagList.clear();
            for (int i = 0; i < circleServer.tags.size(); i++) {
                Tag tag = circleServer.tags.get(i);
                tagList.add(tag.tag_name);
            }
            emCircleServer.setTags(tagList);
            output.add(emCircleServer);
        }
        return output;
    }

    @Ignore
    public static List<CircleServer> converToCirlceServerList(List<EMCircleServer> input) {
        if (input == null) {
            return null;
        }
        List<CircleServer> output = new ArrayList<>();
        for (EMCircleServer emCircleServer : input) {
            CircleServer circleServer = new CircleServer(emCircleServer);
            output.add(circleServer);
        }
        return output;
    }
}
