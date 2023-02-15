package io.agora.service.callbacks;

/**
 * 语聊房状态监听
 */
public interface CircleVoiceChannelStateListener {
   default void onVoiceChannelStart(String rtcChannelName){}//注意本demo为方便使用，设计rtcChannelName等于channelId，用户可根据自己公司实际情况自定义
   default void onVoiceChannelLeave(){}
   default void onSelfMicOffAndSpeaking(String rtcChannelName ){}
   default void onSelfMicOnAndSpeaking(String rtcChannelName ){}
   default void onSelfMicOffAndNoSpeaking(String rtcChannelName ){}
   default void onSelfMicOnAndNoSpeaking(String rtcChannelName ){}
}
