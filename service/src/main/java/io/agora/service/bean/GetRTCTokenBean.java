package io.agora.service.bean;


public class GetRTCTokenBean {

   private Integer code;
   private String accessToken;
   private Long expireTimestamp;
   private String agoraUid;

   public Integer getCode() {
      return code;
   }

   public void setCode(Integer code) {
      this.code = code;
   }

   public String getAccessToken() {
      return accessToken;
   }

   public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
   }

   public Long getExpireTimestamp() {
      return expireTimestamp;
   }

   public void setExpireTimestamp(Long expireTimestamp) {
      this.expireTimestamp = expireTimestamp;
   }

   public String getAgoraUid() {
      return agoraUid;
   }

   public void setAgoraUid(String agoraUid) {
      this.agoraUid = agoraUid;
   }

   @Override
   public String toString() {
      return "GetRTCTokenBean{" +
              "code=" + code +
              ", accessToken='" + accessToken + '\'' +
              ", expireTimestamp=" + expireTimestamp +
              ", agoraUid='" + agoraUid + '\'' +
              '}';
   }
}
