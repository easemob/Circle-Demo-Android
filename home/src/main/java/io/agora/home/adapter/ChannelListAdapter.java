package io.agora.home.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMCircleUserRole;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.widget.EaseImageView;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.common.base.BaseAdapter;
import io.agora.home.R;
import io.agora.home.bean.Node;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.service.global.Constants;
import io.agora.service.managers.CircleRTCManager;

public class ChannelListAdapter extends BaseAdapter<Node> {
    private int roleId=EMCircleUserRole.USER.getRoleId();

    public void setRoleId(int roleId){
        this.roleId=roleId;
        notifyDataSetChanged();
    }


    public ChannelListAdapter(Context context, List<Node> datas, MuitiTypeSupport muitiTypeSupport) {
        super(context, datas, muitiTypeSupport);
    }

    @Override
    public void convert(ViewHolder holder, List<Node> datas, int position) {
        int level = datas.get(position).getLevel();
        Node node = datas.get(position);
        switch (level) {
            case 0:
                if (!node.getName().equals("Default Channel Category")) {
                    processCategoryItem(holder, node, position);
                }
                break;
            case 1:
                processChannelItem(holder, node, position);
                break;
            case 2:
                processChannelSubItemHead(holder, node, position);
                break;
            case 3:
                processChannelSubItem(holder, node, position);
                break;
        }
    }

    private void processCategoryItem(ViewHolder holder, Node node, int position) {
        TextView tvCategoryName = holder.getView(R.id.tv_category_name);
        ImageView ivArrow = holder.getView(R.id.iv_category_list);
        ImageView ivAddChannel = holder.getView(R.id.iv_add_channel);

        if (roleId == EMCircleUserRole.USER.getRoleId()) {//普通成员
            ivAddChannel.setVisibility(View.GONE);
        } else if (roleId == EMCircleUserRole.MODERATOR.getRoleId()) {//管理员
            ivAddChannel.setVisibility(View.GONE);
        } else {//拥有者owner
            ivAddChannel.setVisibility(View.VISIBLE);
        }

        if (node != null) {
            tvCategoryName.setText(node.getName());
        } else {
            tvCategoryName.setText("");
        }
        //设置图标
        if (!node.isExpand()) {
            ivArrow.setImageResource(io.agora.service.R.drawable.circle_arrow_right_gray);
        } else {
            ivArrow.setImageResource(io.agora.service.R.drawable.circle_arrow_up);
        }
        ivAddChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onClick(v, position);
                }
            }
        });
    }

    private void processChannelSubItem(ViewHolder holder, Node node, int position) {
        if (node.getPId().startsWith(Constants.VOICE_CHANNEL_MEMBER_HEAD_ID)) {
            //语聊房成员item
            EaseImageView ivUser = holder.getView(R.id.iv_user_avatar);
            TextView tvName = holder.getView(R.id.tv_nick_name);
            ImageView ivMicOff = holder.getView(R.id.iv_mic_off);
            Glide.with(mContext).load(node.getExt()).placeholder(io.agora.service.R.drawable.circle_default_avatar).into(ivUser);
            tvName.setText(node.getName() + "");
            String channelId = node.getPId().substring(Constants.VOICE_CHANNEL_MEMBER_HEAD_ID.length());

            //设置是否在说话，静音状态等，仅仅针对当前语聊房的成员有效
            if (TextUtils.equals(channelId, CircleRTCManager.getInstance().getChannelId())) {
                ConcurrentHashMap<String, Boolean> uidsMuted = CircleRTCManager.getInstance().getUidsMuted();
                ConcurrentHashMap<String, String> hxIdUids = CircleRTCManager.getInstance().getHxIdUids();
                String uid = hxIdUids.get(node.getId());
                if (uid != null) {
                    if (Boolean.TRUE.equals(uidsMuted.get(uid))) {
                        ivMicOff.setVisibility(View.VISIBLE);
                    } else {
                        ivMicOff.setVisibility(View.GONE);
                    }
                } else {
                    ivMicOff.setVisibility(View.GONE);
                }

                ConcurrentHashMap<String,IRtcEngineEventHandler.AudioVolumeInfo> uidsSpeak = CircleRTCManager.getInstance().getUidsSpeak();
                IRtcEngineEventHandler.AudioVolumeInfo audioVolumeInfo = uidsSpeak.get(node.getId());
                if(audioVolumeInfo!=null&&audioVolumeInfo.volume>3) {
                    ivUser.setBorderColor(mContext.getResources().getColor(io.agora.service.R.color.deep_green_27ae60));
                }else{
                    ivUser.setBorderColor(mContext.getResources().getColor(com.hyphenate.easeui.R.color.transparent));
                }


//                ConcurrentHashMap<String, IRtcEngineEventHandler.AudioVolumeInfo> uidsSpeak = CircleRTCManager.getInstance().getUidsSpeak();
//                if (uidsSpeak.get(node.getId()) != null) {
//                    ivUser.setBorderWidth(ConvertUtils.dp2px(2));
//                    ivUser.setBorderColor(mContext.getResources().getColor(io.agora.service.R.color.deep_green_27ae60));
//                } else {
//                    ivUser.setBorderWidth(0);
//                    ivUser.setBorderColor(mContext.getResources().getColor(com.hyphenate.easeui.R.color.transparent));
//                }
            } else {
                ivMicOff.setVisibility(View.GONE);
                ivUser.setBorderWidth(0);
                ivUser.setBorderColor(mContext.getResources().getColor(com.hyphenate.easeui.R.color.transparent));
            }
        } else {
            TextView tvThreadName = holder.getView(R.id.tv_thread_name);
            if (node != null) {
                tvThreadName.setText(node.getName());
            } else {
                tvThreadName.setText("");
            }
        }
    }

    private void processChannelSubItemHead(ViewHolder holder, Node node, int position) {
        ImageView ivThreadHeadIcon = holder.getView(R.id.iv_thread_head_icon);
        TextView tvHeadName = holder.getView(R.id.tv_head_name);
        if (node != null) {
            if (TextUtils.equals(node.getId(), Constants.VOICE_CHANNEL_MEMBER_HEAD_ID + node.getPId())) {
                //语聊房成员item
                tvHeadName.setText(mContext.getString(R.string.circle_voice_channel_member));
            } else {
                tvHeadName.setText(mContext.getString(R.string.circle_thread));
            }
            ivThreadHeadIcon.setImageResource(node.getIcon());
        }
    }

    private void processChannelItem(ViewHolder holder, Node node, int position) {
        TextView tvChannelName = holder.getView(R.id.tv_channel_name);
        ImageView ivChannelIcon = holder.getView(R.id.iv_channel_icon);
        Glide.with(mContext).load(node.getIcon()).placeholder(R.drawable.circle_channel_public_icon).into(ivChannelIcon);
        TextView tvUnread = holder.getView(R.id.tv_unread);
        TextView tvSeatCount = holder.getView(R.id.tv_count);
        if (node != null) {
            tvChannelName.setText(node.getName());
        } else {
            tvChannelName.setText("");
        }
        int unreadMsgCount = 0;
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(node.getId());
        if (conversation != null) {
            unreadMsgCount = conversation.getUnreadMsgCount();
        }
        if (node.getChannelMode()==0&&unreadMsgCount > 0) {
            tvUnread.setText(unreadMsgCount + "");
            tvUnread.setVisibility(View.VISIBLE);
        } else {
            tvUnread.setVisibility(View.GONE);
        }

        if (node.getChannelMode() == 1) {
            tvSeatCount.setVisibility(View.VISIBLE);

            String channelId = node.getId();
            if (!TextUtils.isEmpty(channelId) && TextUtils.equals(channelId, CircleRTCManager.getInstance().getChannelId())) {
                //说明当前的item是自己正在加入的语聊房
                List<String> uidsInChannel = CircleRTCManager.getInstance().getUidsInChannel();
                tvSeatCount.setText(uidsInChannel.size()+"/"+node.getSeatCount());
                tvSeatCount.setSelected(true);
            }else{
                tvSeatCount.setSelected(false);
                tvSeatCount.setText(node.getSeatCount() + "");
            }
        } else {
            tvSeatCount.setVisibility(View.GONE);
        }

    }
}
