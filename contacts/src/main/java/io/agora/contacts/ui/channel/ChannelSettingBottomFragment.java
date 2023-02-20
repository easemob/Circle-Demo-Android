package io.agora.contacts.ui.channel;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCircleUserRole;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.jeremyliao.liveeventbus.LiveEventBus;

import io.agora.contacts.R;
import io.agora.contacts.databinding.FragmentChannelSettingBinding;
import io.agora.contacts.ui.InviteUserToChannelBottomFragment;
import io.agora.contacts.ui.ThreadListActivity;
import io.agora.service.base.BaseInitFragment;
import io.agora.service.base.ContainerBottomSheetFragment;
import io.agora.service.callbacks.BottomSheetChildHelper;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.global.Constants;
import io.agora.service.model.ChannelViewModel;
import io.agora.service.model.ServerViewModel;

public class ChannelSettingBottomFragment extends BaseInitFragment<FragmentChannelSettingBinding> implements BottomSheetChildHelper, View.OnClickListener {
    private EMCircleUserRole selfRole = EMCircleUserRole.USER;
    private ChannelViewModel mChannelViewModel;
    private ServerViewModel mServerViewModel;
    private CircleChannel channel;
    private ContainerBottomSheetFragment parentFragment;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Bundle arguments = getArguments();
        if (channel == null) {
            channel = (CircleChannel) arguments.getSerializable(Constants.CHANNEL);
        }
    }

    private void initRoleRelatedViewVisiablity(EMCircleUserRole role) {
        mBinding.tvEditChannel.setVisibility(role == EMCircleUserRole.USER ? View.GONE : View.VISIBLE);
        mBinding.cslMoveChannel.setVisibility(role == EMCircleUserRole.USER ? View.GONE : View.VISIBLE);

        //对荣耀等部分系统的ui适配，不可做通用代码
        mBinding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                if (parentFragment != null) {
                    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((1<<30)-1, View.MeasureSpec.AT_MOST);
                    int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((1<<30)-1, View.MeasureSpec.AT_MOST);
                    mBinding.getRoot().measure(widthMeasureSpec,heightMeasureSpec);
                    parentFragment.setLayoutHeight( mBinding.getRoot().getMeasuredHeight());
                }
            }
        });
    }

    @Override
    protected void initConfig() {
        super.initConfig();

        mChannelViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        mServerViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
        mChannelViewModel.leaveChannelResultLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleChannel>() {

                @Override
                public void onSuccess(@Nullable CircleChannel circleChannel) {
                    ToastUtils.showShort(getString(R.string.leave_channel_success));
                    hide();
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

        LiveEventBus.get(Constants.CHANNEL_CHANGED, CircleChannel.class).observe(this, circleChannel -> {
            if (circleChannel != null) {
                if (TextUtils.equals(channel.channelId, circleChannel.channelId)) {
                    channel = circleChannel;
                }
            }
        });

        mBinding.tvInvite.setOnClickListener(this);
        mBinding.tvThreadList.setOnClickListener(this);
        mBinding.tvEditChannel.setOnClickListener(this);
        mBinding.tvNotificationSetting.setOnClickListener(this);
        mBinding.cslMarkRead.setOnClickListener(this);
        mBinding.cslMoveChannel.setOnClickListener(this);
        mBinding.cslChannelMembers.setOnClickListener(this);
        mBinding.llFold.setOnClickListener(this);
    }

    private void onFetchSelfRoleSuccess(EMCircleUserRole role) {
        selfRole = role;
        initRoleRelatedViewVisiablity(role);
        if (role != EMCircleUserRole.USER) {
            mChannelViewModel.fetchChannelMuteUsers(channel.serverId, channel.channelId);
        }
    }

    @Override
    protected void initData() {
        mServerViewModel.fetchSelfServerRole(channel.serverId);
        if(channel!=null) {
            mBinding.tvChannelName.setText(channel.name);
        }
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_channel_setting;
    }

    @Override
    public void setParentContainerFragment(ContainerBottomSheetFragment fragment) {
        this.parentFragment = fragment;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_invite) {
            //跳转去邀请好友页面
            //去邀请好友页面
            InviteUserToChannelBottomFragment fragment = new InviteUserToChannelBottomFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.CHANNEL, channel);
            fragment.setArguments(bundle);
            fragment.show(getParentFragmentManager());
        } else if (v.getId() == R.id.tv_thread_list) {
            //跳转去子区列表页面
            ThreadListActivity.actionStart(mContext, channel);
        } else if (v.getId() == R.id.tv_edit_channel) {
            //跳转去编辑频道页面
            EditChannelActivity.actionStart(mContext, channel);
        } else if (v.getId() == R.id.tv_notification_setting) {
            ChannelNotificationSettingActivity.actionStart(mContext, channel);
        } else if (v.getId() == R.id.csl_mark_read) {
            //标记为已读
            EMConversation conversation = EMClient.getInstance().chatManager().getConversation(channel.channelId);
            if (conversation != null) {
                conversation.markAllMessagesAsRead();
            }
            LiveEventBus.get(Constants.NOTIFY_CHANGE).post(null);
            hide();
        } else if (v.getId() == R.id.csl_move_channel) {
            //移动频道至
            MoveChannelActivity.actionStart(mContext, channel);
        } else if (v.getId() == R.id.csl_channel_members) {
            //查看频道成员
            ChannelMembersActivity.actionStart(mContext, channel);
        }else if(v.getId()==R.id.ll_fold) {
            hide();
        }
//        else if (v.getId() == R.id.csl_leave_channel) {
//            //离开频道
//            mChannelViewModel.leaveChannel(channel);
//        }
    }
}
