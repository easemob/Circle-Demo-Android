package io.agora.contacts.ui.channel;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.example.zhouwei.library.CustomPopWindow;
import com.hyphenate.chat.EMCircleUserRole;
import com.hyphenate.chat.EMPresence;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.widget.EaseRecyclerView;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.common.dialog.AlertDialog;
import io.agora.contacts.R;
import io.agora.contacts.adapter.ContactListAdapter;
import io.agora.contacts.databinding.DialogUserinfoBottomBinding;
import io.agora.contacts.ui.ContactListFragment;
import io.agora.service.bean.channel.ChannelEventNotifyBean;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleUser;
import io.agora.service.global.Constants;
import io.agora.service.managers.AppUserInfoManager;
import io.agora.service.model.ChannelViewModel;
import io.agora.service.model.ServerViewModel;
import io.agora.service.utils.EasePresenceUtil;

public class ChannelMembersFragment extends ContactListFragment implements View.OnClickListener {
    private EMCircleUserRole selfRole = EMCircleUserRole.USER;
    private ChannelViewModel mChannelViewModel;
    private ServerViewModel mServerViewModel;
    private CircleChannel channel;
    private CustomPopWindow mCustomPopWindow;
    private AlertDialog dialog;
    private CircleUser selectedUser;
    private long muteDuration = 24 * 60 * 60 * 1000;//毫秒
    private List<CircleUser> circleUsersMuted;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mBinding.etSearch.setVisibility(View.GONE);
        Bundle arguments = getArguments();
        if (channel == null) {
            channel = (CircleChannel) arguments.getSerializable(Constants.CHANNEL);
        }
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mContactsListViewModel.getContactObservable().removeObservers(getViewLifecycleOwner());

        mChannelViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        mServerViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
        mChannelViewModel.channelMembersLiveData.observe(this, response -> {
            finishRefresh();
            parseResource(response, new OnResourceParseCallback<List<CircleUser>>() {
                @Override
                public void onSuccess(@Nullable List<CircleUser> data) {
                    mData.clear();
                    if (data != null) {
                        mData.addAll(data);
                        setData(mData);
                    }
                }
            });
        });
        mChannelViewModel.muteUserLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean isMuted) {
                    if (isMuted) {
                        ToastUtils.showShort(getString(io.agora.service.R.string.circle_mute_success));
                    } else {
                        ToastUtils.showShort(getString(io.agora.service.R.string.circle_unmute_success));
                    }
                    selectedUser.isMuted = isMuted;
                    refreshDialogView();
                    mListAdapter.notifyDataSetChanged();
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
        mChannelViewModel.removeUserLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String userRemoved) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_removeUser_from_channel_success));
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    //刷新列表
                    if (mData != null) {
                        for (int i = 0; i < mData.size(); i++) {
                            if (mData.get(i).getUsername().equals(userRemoved)) {
                                mData.remove(i);
                                i--;
                            }
                        }
                        mListAdapter.notifyDataSetChanged();
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
        mServerViewModel.addModeratorLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_addModerator_success));
                    selectedUser.roleID = EMCircleUserRole.MODERATOR.getRoleId();
                    refreshDialogView();
                    mListAdapter.notifyDataSetChanged();
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
        mServerViewModel.removeModeratorLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_removeModerator_success));
                    selectedUser.roleID = EMCircleUserRole.USER.getRoleId();
                    refreshDialogView();
                    mListAdapter.notifyDataSetChanged();
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
        mServerViewModel.selfRoleLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EMCircleUserRole>() {
                @Override
                public void onSuccess(@Nullable EMCircleUserRole role) {
                    onFetchSelfRoleSuccess(role);
                }
            });
        });
        mChannelViewModel.fetchChannelMuteUsersLiveData.observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<CircleUser>>() {
                @Override
                public void onSuccess(@Nullable List<CircleUser> circleUsersMuted) {
                    onFetchChannelMuteUsersSuccess(circleUsersMuted);
                }
            });
        });
        LiveEventBus.get(Constants.CHANNEL_CHANGED, CircleChannel.class).observe(this, circleChannel -> {
            if (circleChannel != null) {
                if (TextUtils.equals(channel.channelId, circleChannel.channelId)) {
                    channel = circleChannel;
                }
            }
        });

        LiveEventBus.get(Constants.MEMBER_LEFT_CHANNEL_NOTIFY, ChannelEventNotifyBean.class).observe(getViewLifecycleOwner(), channelEventNotifyBean -> {
            if (channelEventNotifyBean != null && TextUtils.equals(channelEventNotifyBean.getChannelId(), channel.channelId)) {
                mChannelViewModel.getChannelMembers(channel.serverId, channel.channelId);
            }
        });
        LiveEventBus.get(Constants.MEMBER_JOINED_CHANNEL_NOTIFY, ChannelEventNotifyBean.class).observe(getViewLifecycleOwner(), channelEventNotifyBean -> {
            if (channelEventNotifyBean != null && TextUtils.equals(channelEventNotifyBean.getChannelId(), channel.channelId)) {
                mChannelViewModel.getChannelMembers(channel.serverId, channel.channelId);
            }
        });
    }

    private void onFetchSelfRoleSuccess(EMCircleUserRole role) {
        selfRole = role;
        refreshDialogView();
        if (role != EMCircleUserRole.USER) {
            mChannelViewModel.fetchChannelMuteUsers(channel.serverId, channel.channelId);
        }
    }

    @Override
    protected void setData(List<CircleUser> datas) {
        Map<String, Boolean> circleUsersMutedMap = new HashMap<>();
        if (circleUsersMuted != null) {
            for (CircleUser circleUser : circleUsersMuted) {
                circleUsersMutedMap.put(circleUser.username, circleUser.isMuted);
            }
        }
        for (CircleUser circleUser : datas) {
            Boolean isMuted = circleUsersMutedMap.get(circleUser.username);
            if (isMuted != null) {
                circleUser.isMuted = isMuted;
            }
        }
        mListAdapter.setData(datas);
    }

    private void onFetchChannelMuteUsersSuccess(List<CircleUser> circleUsersMuted) {
        this.circleUsersMuted = circleUsersMuted;
        setData(mData);
    }

    @Override
    public void onItemClick(View view, int position) {

//        List<CircleUser> datas = mListAdapter.getData();
//        if (datas != null) {
//            CircleUser circleUser = datas.get(position - 1);
//            if (!TextUtils.equals(circleUser.getUsername(), AppUserInfoManager.getInstance().getCurrentUserName())) {
//                ARouter.getInstance().build("/chat/ChatActivity")
//                        .withString(EaseConstant.EXTRA_CONVERSATION_ID, circleUser.getUsername())
//                        .withInt(EaseConstant.EXTRA_CHAT_TYPE, Constants.CHATTYPE_SINGLE)
//                        .navigation();
//            }
//        }
    }

    @Override
    public void onItemSubViewClick(View view, int position) {
        if (view.getId() == R.id.presenceView) {
            List<CircleUser> datas = mListAdapter.getData();
            if (datas != null) {
                CircleUser circleUser = datas.get(position - 1);
                if (circleUser != null) {
                    if (!TextUtils.equals(circleUser.username, AppUserInfoManager.getInstance().getCurrentUserName())) {
                        showUserInfoBottomDialog(circleUser);
                    }
                }
            }
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
            tvMute.setVisibility(View.VISIBLE);
            tvSetAdmin.setVisibility(View.VISIBLE);
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
    protected void initListener() {
        super.initListener();
    }

    @Override
    protected void initData() {
        mRecyclerView.setAdapter(concatAdapter);
        ((ContactListAdapter) mListAdapter).setDisplayMode(ContactListAdapter.DisplayMode.SHOW_NONE);
        mChannelViewModel.getChannelMembers(channel.serverId, channel.channelId);
        mServerViewModel.fetchSelfServerRole(channel.serverId);
    }

    @Override
    public void onRefresh() {
        mChannelViewModel.getChannelMembers(channel.serverId, channel.channelId);
    }

    protected void checkView(String content) {
        super.checkView(content);
        if (!TextUtils.isEmpty(content)) {
            ((EaseRecyclerView) mRecyclerView).removeHeaderViews();
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

    @Override
    public void onClick(View v) {
        if  (v.getId() == R.id.tv_chat) {
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
            getActivity().finish();
        } else if (v.getId() == R.id.tv_mute) {
            //成员禁言/解禁
            if (selectedUser != null) {
                mChannelViewModel.muteUserInChannel(channel, muteDuration, selectedUser.getUsername(), !selectedUser.isMuted);
            }

        } else if (v.getId() == R.id.tv_set_role) {
            //设为/移除管理员
            if (selectedUser != null) {
                if (selectedUser.roleID == EMCircleUserRole.USER.getRoleId()) {
                    mServerViewModel.addModeratorToServer(channel.serverId, selectedUser.getUsername());
                } else if (selectedUser.roleID == EMCircleUserRole.MODERATOR.getRoleId()) {
                    mServerViewModel.removeModeratorFromServer(channel.serverId, selectedUser.getUsername());
                }
            }
        } else if (v.getId() == R.id.tv_kick) {
            //踢出频道
            if (selectedUser != null) {
                mChannelViewModel.removeUserFromChannel(channel.serverId, channel.channelId, selectedUser.getUsername());
            }
        }
    }
}
