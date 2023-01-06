package io.agora.service.callbacks;


import com.hyphenate.EMChatRoomChangeListener;

import java.util.List;

public interface  SimpleChatRoomChangeListener extends EMChatRoomChangeListener {
    @Override
    default void onChatRoomDestroyed(String roomId, String roomName) {

    }

    @Override
    default void onMemberJoined(String roomId, String participant) {

    }

    @Override
    default void onMemberExited(String roomId, String roomName, String participant) {

    }

    @Override
    default void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {

    }

    @Override
    default void onMuteListAdded(String chatRoomId, List<String> mutes, long expireTime) {

    }

    @Override
    default void onMuteListRemoved(String chatRoomId, List<String> mutes) {

    }

    @Override
    default void onWhiteListAdded(String chatRoomId, List<String> whitelist) {

    }

    @Override
    default void onWhiteListRemoved(String chatRoomId, List<String> whitelist) {

    }

    @Override
    default void onAllMemberMuteStateChanged(String chatRoomId, boolean isMuted) {

    }

    @Override
    default void onAdminAdded(String chatRoomId, String admin) {

    }

    @Override
    default void onAdminRemoved(String chatRoomId, String admin) {

    }

    @Override
    default void onOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {

    }

    @Override
    default void onAnnouncementChanged(String chatRoomId, String announcement) {

    }
}
