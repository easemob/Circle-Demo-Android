package io.agora.service.callbacks;

/**
 * 获取rtctoken后的回调
 */
public interface CircleRTCTokenCallback {

    void setToken(String rctToken, int uid);

}
