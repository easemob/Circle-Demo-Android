package io.agora.home.repo;


import com.blankj.utilcode.util.CollectionUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.agora.service.managers.AppUserInfoManager;
import io.agora.service.managers.NotificationMsgManager;
import io.agora.service.repo.ServiceReposity;

public class ConversationReposity extends ServiceReposity {

    public List<EMConversation> getAllConversations() {
        Map<String, EMConversation> conversationsMap = EMClient.getInstance().chatManager().getAllConversations();
        Collection<EMConversation> values = conversationsMap.values();
        List<EMConversation> conversationList = new ArrayList<>();
        for (EMConversation value : values) {
            if (NotificationMsgManager.getInstance().isNotificationConversation(value)) {
                continue;
            }
            conversationList.add(value);
        }
        return conversationList;
    }

    public List<EMConversation> getConversationsWithType(EMConversation.EMConversationType type) {
        List<EMConversation> conversations = EMClient.getInstance().chatManager().getConversationsByType(type);
        for (int i = 0; i < conversations.size(); i++) {
            EMConversation conversation = conversations.get(i);
            if (NotificationMsgManager.getInstance().isNotificationConversation(conversation)) {
                conversations.remove(conversation);
                i--;
            }
        }
        return conversations;
    }

    public List<EMConversation> getConversationsByServerId(String serverId) {
        List<EMConversation> datas=new ArrayList<>();
        List<String> ids = AppUserInfoManager.getInstance().getChannelIds().get(serverId);
        if (!CollectionUtils.isEmpty(ids)) {
            for (String conversationId : ids) {
                EMConversation conversation = EMClient.getInstance().chatManager().getConversation(conversationId, EMConversation.EMConversationType.GroupChat, false, false,true);
                if(conversation!=null) {
                    datas.add(conversation);
                }
            }
        }
        return datas;
    }
}
