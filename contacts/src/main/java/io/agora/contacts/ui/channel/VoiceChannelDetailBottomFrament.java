package io.agora.contacts.ui.channel;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMCircleUserRole;
import com.hyphenate.chat.EMPresence;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.common.base.BaseAdapter;
import io.agora.common.dialog.AlertDialog;
import io.agora.contacts.R;
import io.agora.contacts.adapter.VoiceChannelListAdapter;
import io.agora.contacts.bean.VoiceChannelUser;
import io.agora.contacts.databinding.DialogUserinfoBottomBinding;
import io.agora.contacts.databinding.FragmentVoicechannelDetailBottomBinding;
import io.agora.contacts.ui.InviteUserToChannelBottomFragment;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.service.base.BaseInitFragment;
import io.agora.service.bean.channel.ChannelEventNotifyBean;
import io.agora.service.bean.channel.ChannelMemberRemovedNotifyBean;
import io.agora.service.callbacks.BottomSheetChildHelper;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.db.entity.CircleUser;
import io.agora.service.global.Constants;
import io.agora.service.managers.AppUserInfoManager;
import io.agora.service.managers.CircleRTCManager;
import io.agora.service.model.ChannelViewModel;
import io.agora.service.model.ServerViewModel;
import io.agora.service.utils.EasePresenceUtil;

/**
 * 点击首页语聊房item底部弹窗
 */
public class VoiceChannelDetailBottomFrament extends BaseInitFragment<FragmentVoicechannelDetailBottomBinding>
        implements BottomSheetChildHelper, View.OnClickListener, BaseAdapter.ItemClickListener {

    private CircleChannel channel;
    private ChannelViewModel channelViewModel;
    private ServerViewModel serverViewModel;
    private List<VoiceChannelUser> voiceChannelUsers = new ArrayList<>();
    private VoiceChannelListAdapter adapter;
    private EMCircleUserRole selfRole = EMCircleUserRole.USER;
    private IRtcEngineEventHandler eventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channelName, int uid, int elapsed) {
            super.onJoinChannelSuccess(channelName, uid, elapsed);
            //默认设置自己闭麦
            CircleRTCManager.getInstance().muteSelf(false);
            mBinding.btnJoinVoiceChannel.setVisibility(View.GONE);
            mBinding.cslMuteExit.setVisibility(View.VISIBLE);
            channelViewModel.getVoiceChannelMembers(channel.serverId, channel.channelId);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onLocalAudioStateChanged(int state, int error) {
        }

        @Override
        public void onAudioPublishStateChanged(String channel, int oldState, int newState, int elapseSinceLastState) {
            initInChannelReferenceButton();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            //退出声网后再退聊天室
            if (channel != null) {
                channelViewModel.leaveChannel(channel);
            }
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            Log.e("uidsSpeak", "onAudioVolumeIndication notifyDataSetChanged");
            adapter.notifyDataSetChanged();
        }
    };
    private AlertDialog dialog;
    private CircleUser selectedUser;


    @Override
    public void onContainerTitleBarInitialize(EaseTitleBar titlebar) {
        titlebar.setVisibility(View.GONE);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_voicechannel_detail_bottom;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Bundle arguments = getArguments();
        if (channel == null) {
            channel = (CircleChannel) arguments.getSerializable(Constants.CHANNEL);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mBinding.recycleview.setLayoutManager(layoutManager);
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        channelViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        serverViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
        channelViewModel.voiceChannelMembersLiveData.observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<ConcurrentHashMap<String, List<CircleUser>>>() {
                @Override
                public void onSuccess(@Nullable ConcurrentHashMap<String, List<CircleUser>> circleUsersHp) {
                    //刷新列表
                    if (circleUsersHp != null && channel != null) {
                        voiceChannelUsers.clear();
                        List<CircleUser> circleUsers = circleUsersHp.get(channel.channelId);
                        for (CircleUser circleUser : circleUsers) {
                            VoiceChannelUser voiceChannelUser = new VoiceChannelUser(circleUser);
                            voiceChannelUsers.add(voiceChannelUser);
                        }
                    }
                    adapter.refresh(voiceChannelUsers);
                    checkRTCState();
                    initInChannelReferenceButton();
                }
            });
        });

        channelViewModel.leaveChannelResultLiveData.observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<CircleChannel>() {

                @Override
                public void onSuccess(@Nullable CircleChannel circleChannel) {
                    hide();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    if (!TextUtils.isEmpty(message)&&code!=205) {//205 -> user is not in channel
                        ToastUtils.showShort(message);
                    }
                }
            });
        });

        channelViewModel.joinChannelResultLiveData.observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String userId) {
                    try {
                        //加入声网频道
                        CircleRTCManager.getInstance().joinChannel(channel.channelId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    if (!TextUtils.isEmpty(message)) {
                        ToastUtils.showShort(message);
                    }
                }
            });
        });

        channelViewModel.removeUserLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String userRemoved) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_removeUser_from_channel_success));
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    //刷新列表
                    if (voiceChannelUsers != null) {
                        for (int i = 0; i < voiceChannelUsers.size(); i++) {
                            if (voiceChannelUsers.get(i).getUsername().equals(userRemoved)) {
                                voiceChannelUsers.remove(i);
                                i--;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    if (!TextUtils.isEmpty(message)) {
                        ToastUtils.showShort(message);
                    }
                }
            });
        });

        LiveEventBus.get(Constants.MEMBER_JOINED_CHANNEL_NOTIFY, ChannelEventNotifyBean.class).observe(getViewLifecycleOwner(), channelEventNotifyBean -> {
            if (channel != null) {
                channelViewModel.getVoiceChannelMembers(channel.serverId, channel.channelId);
            }
        });
        LiveEventBus.get(Constants.MEMBER_LEFT_CHANNEL_NOTIFY, ChannelEventNotifyBean.class).observe(getViewLifecycleOwner(), channelEventNotifyBean -> {
            if (channel != null) {
                channelViewModel.getVoiceChannelMembers(channel.serverId, channel.channelId);
            }
        });
        LiveEventBus.get(Constants.MEMBER_REMOVED_FROM_CHANNEL_NOTIFY, ChannelMemberRemovedNotifyBean.class).observe(getViewLifecycleOwner(), channelMemberRemovedNotifyBean -> {
            if (channel != null) {
                if(TextUtils.equals(channelMemberRemovedNotifyBean.getMember(),AppUserInfoManager.getInstance().getCurrentUserName())) {
                    //被踢的人是自己就退出RTC频道
                    CircleRTCManager.getInstance().leaveChannel();
                }
                //刷新列表
                channelViewModel.getVoiceChannelMembers(channel.serverId, channel.channelId);
            }
        });
        mBinding.ivSetting.setOnClickListener(this);
        mBinding.ivAddFriend.setOnClickListener(this);
        mBinding.ibMicOff.setOnClickListener(this);
        mBinding.ibExit.setOnClickListener(this);
        mBinding.btnJoinVoiceChannel.setOnClickListener(this);

        CircleRTCManager.getInstance().registerRTCEventListener(eventHandler);

    }

    private void checkRTCState() {
        String currentUserName = AppUserInfoManager.getInstance().getCurrentUserName();
        for (VoiceChannelUser data : voiceChannelUsers) {
            if (TextUtils.equals(data.username, currentUserName)) {//我在语聊房
                if (!TextUtils.equals(CircleRTCManager.getInstance().getChannelId(), channel.channelId)) {
                    try {
                        CircleRTCManager.getInstance().joinChannel(channel.channelId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    private void onFetchSelfRoleSuccess(EMCircleUserRole role) {
        selfRole = role;
        mBinding.ivSetting.setVisibility(role == EMCircleUserRole.USER ? View.GONE : View.VISIBLE);
    }

    private void initInChannelReferenceButton() {
        String currentUserName = AppUserInfoManager.getInstance().getCurrentUserName();
        boolean inRTCChannel = false;
        for (VoiceChannelUser data : voiceChannelUsers) {
            if (TextUtils.equals(data.username, currentUserName)) {//我在语聊房
                inRTCChannel = true;
                break;
            }
        }
        if (inRTCChannel) {
            mBinding.btnJoinVoiceChannel.setVisibility(View.GONE);
            mBinding.cslMuteExit.setVisibility(View.VISIBLE);
            mBinding.ivAddFriend.setVisibility(View.VISIBLE);
        } else {
            mBinding.btnJoinVoiceChannel.setVisibility(View.VISIBLE);
            mBinding.cslMuteExit.setVisibility(View.GONE);
            mBinding.ivAddFriend.setVisibility(View.GONE);
        }
        ConcurrentHashMap<String, Boolean> uidsMuted = CircleRTCManager.getInstance().getUidsMuted();
        String currentUid = CircleRTCManager.getInstance().getCurrentUid();
        if (currentUid != null) {
            if (Boolean.TRUE.equals(uidsMuted.get(currentUid))) {
                //闭麦状态
                mBinding.ibMicOff.setImageResource(io.agora.service.R.drawable.circle_voice_mic_off_gray);
            } else {
                //开麦状态
                mBinding.ibMicOff.setImageResource(io.agora.service.R.drawable.circle_voice_mic_on_white);
            }
        }
    }

    @Override
    protected void initData() {
        super.initData();
        if (channel != null) {
            channelViewModel.getVoiceChannelMembers(channel.serverId, channel.channelId);
            mBinding.tvChannelName.setText(channel.name);
            CircleServer server = DatabaseManager.getInstance().getServerDao().getServerById(channel.serverId);
            mBinding.tvServerName.setText(server.name);
            AppUserInfoManager.getInstance().getSelfServerRoleMapLiveData().observe(getViewLifecycleOwner(), serverRoleMap -> {
                if (serverRoleMap != null) {//在首页已经请求过了，所以这里必定有数据
                    Integer roleId = serverRoleMap.get(channel.serverId);
                    if (roleId != null) {
                        if (roleId.intValue() == EMCircleUserRole.USER.getRoleId()) {
                            selfRole = EMCircleUserRole.USER;
                        } else if (roleId.intValue() == EMCircleUserRole.MODERATOR.getRoleId()) {
                            selfRole = EMCircleUserRole.MODERATOR;
                        } else if (roleId.intValue() == EMCircleUserRole.OWNER.getRoleId()) {
                            selfRole = EMCircleUserRole.OWNER;
                        }
                    }
                    onFetchSelfRoleSuccess(selfRole);
                }
            });
        }
        adapter = new VoiceChannelListAdapter(mContext, R.layout.item_voice_channel_member);
        mBinding.recycleview.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_add_friend) {
            //邀请成员加入频道
            //去邀请好友页面
            InviteUserToChannelBottomFragment fragment = new InviteUserToChannelBottomFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.CHANNEL, channel);
            fragment.setArguments(bundle);
            fragment.show(getParentFragmentManager());
        } else if (v.getId() == R.id.iv_setting) {
            //设置
            EditChannelActivity.actionStart(mContext, channel);
        } else if (v.getId() == R.id.ib_mic_off) {
            //关闭麦克风
            ConcurrentHashMap<String, Boolean> uidsMuted = CircleRTCManager.getInstance().getUidsMuted();
            String currentUid = CircleRTCManager.getInstance().getCurrentUid();
            if (currentUid != null && Boolean.TRUE.equals(uidsMuted.get(currentUid))) {
                CircleRTCManager.getInstance().muteSelf(true);
            } else {
                CircleRTCManager.getInstance().muteSelf(false);
            }
        } else if (v.getId() == R.id.ib_exit) {
            //退出语聊房，先退声网，再退聊天室
            CircleRTCManager.getInstance().leaveChannel();
        } else if (v.getId() == R.id.btn_join_voice_channel) {
            //加入语聊房,先加聊天室，再加声网。不同于文字频道在外边就会校验，语聊房当前页面进来时有可能还不是语聊房内部成员。
            if (channel != null) {
                channelViewModel.joinChannel(channel, AppUserInfoManager.getInstance().getCurrentUserName());
            }
        } else if (v.getId() == R.id.ll_fold) {
            hide();
        } else if (v.getId() == R.id.tv_chat) {
            //发起私聊
            if (selectedUser != null) {
                ARouter.getInstance().build("/chat/ChatActivity")
                        .withString(EaseConstant.EXTRA_CONVERSATION_ID, selectedUser.getUsername())
                        .withInt(EaseConstant.EXTRA_CHAT_TYPE, Constants.CHATTYPE_SINGLE)
                        .navigation();
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        } else if (v.getId() == R.id.tv_kick) {
            //踢出频道
            if (selectedUser != null) {
                channelViewModel.removeUserFromChannel(channel.serverId, channel.channelId, selectedUser.getUsername());
            }
        }
    }

    private void showUserInfoBottomDialog(CircleUser user) {
        if (user != null) {
            //根据user初始化selectedMemberRole、isSelectedMemberMuteState
            this.selectedUser = user;
            DialogUserinfoBottomBinding dialogBinding = DialogUserinfoBottomBinding.inflate(getLayoutInflater());
            dialog = new AlertDialog.Builder(mContext)
                    .setContentView(dialogBinding.getRoot())
                    .setOnClickListener(R.id.tv_chat, this)
                    .setOnClickListener(R.id.tv_mute, this)
                    .setOnClickListener(R.id.tv_set_role, this)
                    .setOnClickListener(R.id.tv_kick, this)
                    .setText(R.id.tv_nick_name, TextUtils.isEmpty(user.getNickname()) ? user.getUsername() : user.getNickname())
                    .setText(R.id.tv_id, getString(io.agora.service.R.string.hx_id) + user.getUsername())
                    .setGravity(Gravity.BOTTOM)
                    .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .show();
            Glide.with(mContext)
                    .load(user.getAvatar())
                    .placeholder(io.agora.service.R.drawable.circle_default_avatar)
                    .into(dialogBinding.ivUserAvatar);
            EMPresence presence = AppUserInfoManager.getInstance().getPresences().get(user.getUsername());
            int presenceIcon = EasePresenceUtil.getPresenceIcon(mContext, presence);
            dialogBinding.ivPresence.setImageResource(presenceIcon);
            refreshDialogView();
        }
    }

    private void refreshDialogView() {
        if (dialog != null) {
            TextView tvChat = dialog.getViewById(R.id.tv_mute);
            TextView tvMute = dialog.getViewById(R.id.tv_mute);
            TextView tvSetAdmin = dialog.getViewById(R.id.tv_set_role);
            TextView tvKick = dialog.getViewById(R.id.tv_kick);
            ImageView ivMute = dialog.getViewById(R.id.iv_mute);
            TextView tvRoleTag = dialog.getViewById(R.id.tv_role_tag);
            tvChat.setVisibility(View.VISIBLE);
            tvMute.setVisibility(View.GONE);
            tvSetAdmin.setVisibility(View.GONE);
            tvKick.setVisibility(View.VISIBLE);
            ivMute.setVisibility(View.VISIBLE);
            tvRoleTag.setVisibility(View.VISIBLE);


            if (selfRole == EMCircleUserRole.MODERATOR) {
                tvSetAdmin.setVisibility(View.GONE);
            } else if (selfRole == EMCircleUserRole.USER) {
                tvSetAdmin.setVisibility(View.GONE);
                tvMute.setVisibility(View.GONE);
                tvKick.setVisibility(View.GONE);
            }
            Drawable drawable = null;
            if (selectedUser.isMuted) {
                ivMute.setVisibility(View.VISIBLE);
                tvMute.setText(getString(io.agora.service.R.string.dialog_member_unmute));
                drawable = getResources().getDrawable(io.agora.service.R.drawable.circle_member_unmute);
            } else {
                ivMute.setVisibility(View.GONE);
                tvMute.setText(getString(io.agora.service.R.string.dialog_member_mute));
                drawable = getResources().getDrawable(io.agora.service.R.drawable.circle_member_mute);
            }
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tvMute.setCompoundDrawables(null, drawable, null, null);

            if (selectedUser.roleID == EMCircleUserRole.USER.getRoleId()) {
                tvSetAdmin.setText(getString(io.agora.service.R.string.dialog_set_admin));
                drawable = getResources().getDrawable(io.agora.service.R.drawable.circle_make_admin);
                tvRoleTag.setVisibility(View.GONE);
            } else if (selectedUser.roleID == EMCircleUserRole.MODERATOR.getRoleId()) {
                tvSetAdmin.setText(getString(io.agora.service.R.string.dialog_cancel_admin));
                drawable = getResources().getDrawable(io.agora.service.R.drawable.circle_cancel_admin);
                tvRoleTag.setVisibility(View.VISIBLE);
                tvRoleTag.setEnabled(true);
                tvRoleTag.setText(getString(R.string.circle_role_moderator));
            }
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tvSetAdmin.setCompoundDrawables(null, drawable, null, null);

            //当目标是群主时
            if (selectedUser.roleID == EMCircleUserRole.OWNER.getRoleId()) {
                tvRoleTag.setVisibility(View.VISIBLE);
                tvRoleTag.setEnabled(false);
                tvRoleTag.setText(getString(R.string.circle_role_creater));

                tvMute.setVisibility(View.GONE);
                tvSetAdmin.setVisibility(View.GONE);
                tvKick.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CircleRTCManager.getInstance().unRegisterRTCEventListener(eventHandler);
    }

    @Override
    public void onClick(View itemView, int position) {
        CircleUser circleUser = voiceChannelUsers.get(position);
        if (circleUser != null) {
            if (!TextUtils.equals(circleUser.username, AppUserInfoManager.getInstance().getCurrentUserName())) {
                showUserInfoBottomDialog(circleUser);
            }
        }
    }
}
