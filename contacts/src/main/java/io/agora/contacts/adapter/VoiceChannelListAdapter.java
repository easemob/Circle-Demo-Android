package io.agora.contacts.adapter;


import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.easeui.widget.EaseImageView;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.common.base.BaseAdapter;
import io.agora.contacts.R;
import io.agora.contacts.bean.VoiceChannelUser;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.service.managers.CircleRTCManager;

public class VoiceChannelListAdapter extends BaseAdapter<VoiceChannelUser> {

    public VoiceChannelListAdapter(Context context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    public void convert(ViewHolder holder, List<VoiceChannelUser> datas, int position) {
        EaseImageView ivUser = holder.getView(R.id.iv_user_avatar);
        TextView tvName=holder.getView(R.id.tv_nick_name);
        ImageView imageView=holder.getView(R.id.iv_mic_off);
        VoiceChannelUser voiceChannelUser = datas.get(position);

        Glide.with(mContext).load(voiceChannelUser.avatar).placeholder(io.agora.service.R.drawable.circle_default_avatar).into(ivUser);
        tvName.setText(voiceChannelUser.getVisiableName());

        ConcurrentHashMap<String, Boolean> uidsMuted = CircleRTCManager.getInstance().getUidsMuted();
        ConcurrentHashMap<String, String> hxIdUids = CircleRTCManager.getInstance().getHxIdUids();
        String uid = hxIdUids.get(voiceChannelUser.username);
        if(uid!=null) {
            if(Boolean.TRUE.equals(uidsMuted.get(uid))) {
                imageView.setVisibility(View.VISIBLE);
            }else{
                imageView.setVisibility(View.GONE);
            }
        }else{
            imageView.setVisibility(View.GONE);
        }
        ConcurrentHashMap<String,IRtcEngineEventHandler.AudioVolumeInfo> uidsSpeak = CircleRTCManager.getInstance().getUidsSpeak();
        if(uidsSpeak.get(voiceChannelUser.username)!=null) {
            ivUser.setBorderColor(mContext.getResources().getColor(R.color.deep_green_27ae60));
        }else{
            ivUser.setBorderColor(mContext.getResources().getColor(com.hyphenate.easeui.R.color.transparent));
        }
    }
}
