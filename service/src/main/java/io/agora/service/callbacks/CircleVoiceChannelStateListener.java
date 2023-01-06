package io.agora.service.callbacks;

/**
 * 语聊房状态监听
 */
public interface CircleVoiceChannelStateListener {
   void onVoiceChannelStart();
   void onVoiceChannelLeave();
   void onSelfMicOffAndSpeaking();
   void onSelfMicOnAndSpeaking();
   void onSelfMicOffAndNoSpeaking();
   void onSelfMicOnAndNoSpeaking();
}
