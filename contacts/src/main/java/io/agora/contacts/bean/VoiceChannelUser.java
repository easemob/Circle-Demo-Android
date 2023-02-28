package io.agora.contacts.bean;


import io.agora.service.db.entity.CircleUser;

public class VoiceChannelUser extends CircleUser {
    public boolean isSpeaking;
    public boolean isMicOff;

    public VoiceChannelUser(CircleUser circleUser) {
        this.roleID = circleUser.roleID;
        this.inviteState = circleUser. inviteState;
        this.isMuted = circleUser. isMuted;
        this.username = circleUser. username;
        this.nickname = circleUser. nickname;
        this.initialLetter = circleUser. initialLetter;
        this.avatar = circleUser. avatar;
        this.contact = circleUser. contact;
        this.lastModifyTimestamp = circleUser. lastModifyTimestamp;
        this.modifyInitialLetterTimestamp = circleUser. modifyInitialLetterTimestamp;
        this.email = circleUser. email;
        this.phone = circleUser. phone;
        this.gender = circleUser. gender;
        this.sign = circleUser. sign;
        this.birth = circleUser. birth;
        this.ext = circleUser. ext;
    }
}
