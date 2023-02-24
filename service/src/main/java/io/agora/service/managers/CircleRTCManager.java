package io.agora.service.managers;

import static io.agora.rtc2.Constants.PUB_STATE_NO_PUBLISHED;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.EMLog;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.service.callbacks.CircleRTCListener;
import io.agora.service.callbacks.CircleRTCTokenCallback;
import io.agora.service.callbacks.CircleVoiceChannelStateListener;
import io.agora.service.callbacks.SimpleChatRoomChangeListener;

/**
 * RTC相关管理类
 */
public class CircleRTCManager {
    private String TAG = getClass().getSimpleName();

    private static final CircleRTCManager ourInstance = new CircleRTCManager();
    private CircleRTCListener circleRTCListener;
    private CopyOnWriteArrayList<IRtcEngineEventHandler> eventHandlers = new CopyOnWriteArrayList();
    private CopyOnWriteArrayList<CircleVoiceChannelStateListener> voiceChannelStateListeners = new CopyOnWriteArrayList();
    // 填写项目的 App ID，可在 Agora 控制台中生成。
    private String appId = "15cb0d28b87b425ea613fc46f7c9f974";
    private RtcEngine mRtcEngine;
    private ChannelMediaOptions options;
    private String channelId;
    private String currentUid;
    private CopyOnWriteArrayList<String> uidsInChannel = new CopyOnWriteArrayList<>();//在频道里的成员
    private ConcurrentHashMap<String, Boolean> uidsMuted = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IRtcEngineEventHandler.AudioVolumeInfo> uidsSpeak = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> uidHxIds = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> hxIdUids = new ConcurrentHashMap<>();
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // 监听频道内的远端主播，获取主播的 uid 信息。
        public void onUserJoined(int uid, int elapsed) {
            EMLog.e("mRtcEventHandler", "onUserJoined uid=" + uid);
            uidsInChannel.add(String.valueOf(uid));
            ThreadUtils.runOnUiThread(() -> {
                //有人进来时主动拉一次key value值
                fetchChatRoomAttribute(channelId);
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onUserJoined(uid, elapsed);
                }
            });
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            EMLog.e("mRtcEventHandler", "onJoinChannelSuccess uid=" + uid + ",channel=" + channel);

            currentUid = String.valueOf(uid);
            uidsInChannel.add(String.valueOf(uid));
            ThreadUtils.runOnUiThread(() -> {
                setChannelAttribute(channelId, AppUserInfoManager.getInstance().getCurrentUserName(), String.valueOf(uid));
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onJoinChannelSuccess(channel, uid, elapsed);
                }
                for (CircleVoiceChannelStateListener voiceChannelStateListener : voiceChannelStateListeners) {
                    voiceChannelStateListener.onVoiceChannelStart(channel);
                }
            });
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            EMLog.e("mRtcEventHandler", "err err=" + err);
            ThreadUtils.runOnUiThread(() -> {
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onError(err);
                }
            });
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            EMLog.e("TAG", "onLeaveChannel");
            callbackOnLeaveChannel(stats);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            EMLog.e("mRtcEventHandler", "onUserOffline uid=" + uid);
            uidsInChannel.remove(String.valueOf(uid));
            ThreadUtils.runOnUiThread(() -> {
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onUserOffline(uid, reason);
                }
            });
        }

        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            EMLog.e("mRtcEventHandler", "onUserMuteAudio uid=" + uid + ",muted=" + muted);
            ThreadUtils.runOnUiThread(() -> {
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onUserMuteAudio(uid, muted);
                }
            });
            uidsMuted.put(String.valueOf(uid), muted);
        }

        @Override
        public void onLocalAudioStateChanged(int state, int error) {
//            EMLog.e("mRtcEventHandler", "onLocalAudioStateChanged state=" + state+",error="+error);
            ThreadUtils.runOnUiThread(() -> {
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onLocalAudioStateChanged(state, error);
                }
            });
        }

        @Override
        public void onAudioPublishStateChanged(String channel, int oldState, int newState, int elapseSinceLastState) {
//            EMLog.e("mRtcEventHandler", "onAudioPublishStateChanged channel=" + channel + ",oldState=" + oldState + ",newState=" + newState + ",elapseSinceLastState=" + elapseSinceLastState);
            if (currentUid != null) {
                if (newState == PUB_STATE_NO_PUBLISHED) {//静音
                    uidsMuted.put(currentUid, true);
                } else {
                    uidsMuted.put(currentUid, false);
                }
            }

            ThreadUtils.runOnUiThread(() -> {
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onAudioPublishStateChanged(channel, oldState, newState, elapseSinceLastState);
                }
            });
        }

        @Override
        public void onRemoteAudioStateChanged(int uid, int state, int reason, int elapsed) {
            EMLog.e("mRtcEventHandler", "onRemoteAudioStateChanged state=" + state + "，uid=" + uid);
            ThreadUtils.runOnUiThread(() -> {
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onRemoteAudioStateChanged(uid, state, reason, elapsed);
                }
            });

        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
//            EMLog.e("mRtcEventHandler","onAudioVolumeIndication speakers size=" + speakers.length+"，totalVolume="+totalVolume);
            ThreadUtils.runOnUiThread(() -> {
                int maxVolume = 0;
                if (speakers != null && speakers.length > 0) {

                    for (AudioVolumeInfo speaker : speakers) {
                        String hxUserId = AppUserInfoManager.getInstance().getCurrentUserName();
                        if (speaker.uid != 0) {//非本地用户
                            hxUserId = uidHxIds.get(String.valueOf(speaker.uid));
                        }
                        if (hxUserId != null) {
                            uidsSpeak.put(hxUserId, speaker);
                            EMLog.d("uidsSpeak=", " hxuserid=" + hxUserId + " speakers.uid=" + speaker.uid + " speakers.volume=" + speaker.volume);
                        }
                        maxVolume = Math.max(maxVolume, speaker.volume);
                    }
                }
                for (CircleVoiceChannelStateListener stateListener : voiceChannelStateListeners) {
                    if (currentUid != null) {
                        if (maxVolume > 3) {
                            if (Boolean.TRUE.equals(uidsMuted.get(currentUid))) {
                                stateListener.onSelfMicOffAndSpeaking(channelId);
                            } else {
                                stateListener.onSelfMicOnAndSpeaking(channelId);
                            }
                        } else {
                            if (Boolean.TRUE.equals(uidsMuted.get(currentUid))) {
                                stateListener.onSelfMicOffAndNoSpeaking(channelId);
                            } else {
                                stateListener.onSelfMicOnAndNoSpeaking(channelId);
                            }
                        }
                    }
                }
                for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                    eventHandler.onAudioVolumeIndication(speakers, totalVolume);
                }
            });
        }
    };

    private void callbackOnLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        clearAll();
        ThreadUtils.runOnUiThread(() -> {
            for (IRtcEngineEventHandler eventHandler : eventHandlers) {
                eventHandler.onLeaveChannel(stats);
            }
            for (CircleVoiceChannelStateListener voiceChannelStateListener : voiceChannelStateListeners) {
                voiceChannelStateListener.onVoiceChannelLeave();
            }
        });

    }

    public void clearAll() {
        uidsInChannel.clear();
        uidsMuted.clear();
        uidHxIds.clear();
        hxIdUids.clear();
        uidsSpeak.clear();
        currentUid = null;
        channelId = null;
    }

    private void setChannelAttribute(String channelName, String key, String value) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //设置语聊房kv属性值
                EMClient.getInstance().chatroomManager().asyncSetChatroomAttributeForced(channelName, key, value, true, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        EMLog.e("onAttributesUpdate", "asyncSetChatroomAttributeForced-->  onSuccess()");
                        fetchChatRoomAttribute(channelName);
                    }

                    @Override
                    public void onError(int code, String error) {
                        EMLog.e("onAttributesUpdate", "asyncSetChatroomAttributeForced-->  error=" + error);
                    }
                });
            }
        });
    }

    private void fetchChatRoomAttribute(String channelName) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EMClient.getInstance().chatroomManager().asyncFetchChatRoomAllAttributesFromServer(channelName, new EMValueCallBack<Map<String, String>>() {
                    @Override
                    public void onSuccess(Map<String, String> attributeMap) {
                        EMLog.e("onAttributesUpdate", "fetchChatRoomAttribute onSuccess, attributeMap.size=" + attributeMap.size());
                        Iterator<String> iterator = attributeMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();//环信Id
                            String value = attributeMap.get(key);//声网uid
                            EMLog.e("onAttributesUpdate", "fetchChatRoomAttribute hxid=" + key + ",uid=" + value);
                            uidHxIds.put(value, key);
                            hxIdUids.put(key, value);
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.e("onAttributesUpdate", "fetchChatRoomAttribute onError=" + errorMsg);
                    }
                });
            }
        });
    }

    public static CircleRTCManager getInstance() {
        return ourInstance;
    }

    private CircleRTCManager() {}

    public CircleRTCManager init(Context context) {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context;
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
            //Enable detection of who is talking(启动谁在说话检测)
            mRtcEngine.enableAudioVolumeIndication(1000, 3, false);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
        options = new ChannelMediaOptions();
        // 设置频道场景为 BROADCASTING。
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        // 将用户角色设置为 BROADCASTER。
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

        EMClient.getInstance().chatroomManager().addChatRoomChangeListener(new SimpleChatRoomChangeListener() {
            @Override
            public void onAttributesUpdate(String chatRoomId, Map<String, String> attributeMap, String from) {
                EMLog.e("onAttributesUpdate", "chatRoomId=" + chatRoomId + ",attributeMap=" + attributeMap + ",from=" + from);
                if (TextUtils.equals(chatRoomId, channelId)) {
                    Iterator<String> iterator = attributeMap.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();//环信Id
                        String value = attributeMap.get(key);//声网uid
                        EMLog.e("onAttributesUpdate", "hxid=" + key + ",uid=" + value);
                        uidHxIds.put(value, key);
                        hxIdUids.put(key, value);
                    }
                }
            }

            @Override
            public void onAttributesRemoved(String chatRoomId, List<String> keyList, String from) {

            }
        });
        return this;
    }

    public void joinChannel(String channelId) throws Exception {
        //同时只允许进一个频道，进下一个频道前退出上一个频道。
        if (this.channelId != null && !TextUtils.equals(this.channelId, channelId)) {
            ToastUtils.showShort("请先退出现有语聊房");
            return;
        }
        this.channelId = channelId;
        if (circleRTCListener == null) {
            throw new Exception("you should register circleRTCListener first");
        }
        ThreadUtils.runOnUiThread(() -> {
            circleRTCListener.onGenerateRTCToken(channelId, new CircleRTCTokenCallback() {
                @Override
                public void setToken(String rctToken, int uid) {
                    //uid传入0，agora sdk内部会给我们随机分配一个uid
                    Log.e("TAG", "mRtcEngine.joinChannel");
                    mRtcEngine.joinChannel(rctToken, channelId, uid, options);
                }
            });
        });
    }

    public void muteSelf(boolean muted) {
        mRtcEngine.enableLocalAudio(muted);
    }

    public void leaveChannel() {
        if (TextUtils.isEmpty(currentUid)) {
            EMLog.e("TAG", "leaveChannel ->currentUid isEmpty");
            callbackOnLeaveChannel(null);
        } else {
            EMLog.e("TAG", "mRtcEngine.leaveChannel();");
            mRtcEngine.leaveChannel();
        }
    }

    public String getChannelId() {
        return channelId;
    }

    public void registerCircleRTCListener(CircleRTCListener listener) {
        this.circleRTCListener = listener;
    }

    public void unRegisterCircleRTCListener() {
        this.circleRTCListener = null;
    }

    public void registerRTCEventListener(IRtcEngineEventHandler listener) {
        if (!eventHandlers.contains(listener)) {
            eventHandlers.add(listener);
        }
    }

    public void unRegisterRTCEventListener(IRtcEngineEventHandler listener) {
        eventHandlers.remove(listener);
    }

    public void clearRTCEventListener() {
        eventHandlers.clear();
    }

    public void registerVoiceChannelStateListener(CircleVoiceChannelStateListener listener) {
        if (!voiceChannelStateListeners.contains(listener)) {
            voiceChannelStateListeners.add(listener);
        }
    }

    public void unRegisterVoiceChannelStateListener(CircleVoiceChannelStateListener listener) {
        voiceChannelStateListeners.remove(listener);
    }

    public void clearVoiceChannelStateListener() {
        voiceChannelStateListeners.clear();
    }

    public List<String> getUidsInChannel() {
        return uidsInChannel;
    }

    public ConcurrentHashMap<String, Boolean> getUidsMuted() {
        return uidsMuted;
    }

    public String getCurrentUid() {
        return currentUid;
    }

    public ConcurrentHashMap<String, IRtcEngineEventHandler.AudioVolumeInfo> getUidsSpeak() {
        return uidsSpeak;
    }

    public ConcurrentHashMap<String, String> getHxIdUids() {
        return hxIdUids;
    }
}
