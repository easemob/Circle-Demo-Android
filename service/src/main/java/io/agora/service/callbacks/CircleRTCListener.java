package io.agora.service.callbacks;


public interface CircleRTCListener {

    default void onGenerateRTCToken(String channelName,CircleRTCTokenCallback callback){}

}
