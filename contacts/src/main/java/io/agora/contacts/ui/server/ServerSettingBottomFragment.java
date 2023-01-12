package io.agora.contacts.ui.server;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCircleUserRole;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.jeremyliao.liveeventbus.LiveEventBus;

import io.agora.contacts.R;
import io.agora.contacts.databinding.FragmentServerSettingBinding;
import io.agora.contacts.ui.InviteUserToServerBottomFragment;
import io.agora.contacts.ui.category.CreateCategoryActivity;
import io.agora.contacts.ui.channel.CreateChannelActivity;
import io.agora.service.base.BaseBottomSheetFragment;
import io.agora.service.base.BaseInitFragment;
import io.agora.service.callbacks.BottomSheetChildHelper;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.managers.AppUserInfoManager;
import io.agora.service.model.ChatViewModel;
import io.agora.service.model.ServerViewModel;

public class ServerSettingBottomFragment extends BaseInitFragment<FragmentServerSettingBinding> implements BottomSheetChildHelper, View.OnClickListener {
    private ServerViewModel mServerViewModel;
    private ChatViewModel mChatViewModel;
    private CircleServer server;
    private EMCircleUserRole selfRole = EMCircleUserRole.USER;
    private BaseBottomSheetFragment parentContainerFragment;

    @Override
    protected void initConfig() {
        super.initConfig();

        mServerViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
        mChatViewModel=new ViewModelProvider(this).get(ChatViewModel.class);
        mServerViewModel.leaveServerResultLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    if (data) {
                        ToastUtils.showShort(getString(R.string.leave_server_success));
                        hide();
                    } else {
                        ToastUtils.showShort(getString(R.string.leave_server_failure));
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
        mChatViewModel.markServerMessagesAsReadLiveData.observe(getViewLifecycleOwner(),response->{
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    hide();
                }
            });
        });

        AppUserInfoManager.getInstance().getSelfServerRoleMapLiveData().observe(getViewLifecycleOwner(), serverRoleMap -> {
            if (serverRoleMap != null) {//在首页已经请求过了，所以这里必定有数据
                Integer roleId = serverRoleMap.get(server.serverId);
                if (roleId != null) {
                    if (roleId.intValue() == EMCircleUserRole.USER.getRoleId()) {
                        selfRole = EMCircleUserRole.USER;
                    } else if (roleId.intValue() == EMCircleUserRole.MODERATOR.getRoleId()) {
                        selfRole = EMCircleUserRole.MODERATOR;
                    } else if (roleId.intValue() == EMCircleUserRole.OWNER.getRoleId()) {
                        selfRole = EMCircleUserRole.OWNER;
                    }
                }
                initRoleRelatedViewVisiablity(selfRole);
            }
        });
        LiveEventBus.get(Constants.SERVER_UPDATED, CircleServer.class).observe(this, serverUpdated -> {
            if (serverUpdated != null && server != null && android.text.TextUtils.equals(server.serverId, serverUpdated.serverId)) {
                server = serverUpdated;
            }
        });
        mBinding.tvInvite.setOnClickListener(this);
        mBinding.tvNotificationSetting.setOnClickListener(this);
        mBinding.tvEditServer.setOnClickListener(this);
        mBinding.cslMarkRead.setOnClickListener(this);
        mBinding.cslCreateChannel.setOnClickListener(this);
        mBinding.cslCreateCategory.setOnClickListener(this);
        mBinding.cslServerMembers.setOnClickListener(this);
        mBinding.cslExitServer.setOnClickListener(this);
    }

    private void initRoleRelatedViewVisiablity(EMCircleUserRole role) {
        mBinding.cslCreateChannel.setVisibility(role == EMCircleUserRole.OWNER ? View.VISIBLE : View.GONE);
        mBinding.cslCreateCategory.setVisibility(role == EMCircleUserRole.USER ? View.GONE : View.VISIBLE);
        mBinding.tvEditServer.setVisibility(role == EMCircleUserRole.USER ? View.GONE : View.VISIBLE);
        mBinding.cslExitServer.setVisibility(role == EMCircleUserRole.USER ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Bundle arguments = getArguments();
        server = (CircleServer) arguments.get(Constants.SERVER);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_server_setting;
    }

    @Override
    public void onContainerTitleBarInitialize(EaseTitleBar titlebar) {
        if (server != null) {
            titlebar.setTitle(server.name);
        }
        titlebar.setLeftLayoutVisibility(View.GONE);
        titlebar.getRightImage().setVisibility(View.GONE);
    }

    @Override
    public void showllFold(View llfold) {
        llfold.setVisibility(View.VISIBLE);
    }

    @Override
    public void setParentContainerFragment(BaseBottomSheetFragment parentContainerFragment) {
        this.parentContainerFragment = parentContainerFragment;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_invite) {
            //去邀请好友页面
            InviteUserToServerBottomFragment fragment = new InviteUserToServerBottomFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.SERVER, server);
            fragment.setArguments(bundle);
            fragment.show(getParentFragmentManager());
        } else if (v.getId() == R.id.tv_edit_server) {
            //去编辑社区页面
            EditServerActivity.actionStart(mContext, server);
            hide();
        } else if (v.getId() == R.id.tv_notification_setting) {
            //通知设定
            Intent intent = new Intent(mContext, ServerNotificationSettingActivity.class);
            startActivity(intent);
            hide();
        } else if (v.getId() == R.id.csl_mark_read) {
            //标记为已读
            mChatViewModel.markServerMessagesAsRead(server.serverId);
        } else if (v.getId() == R.id.csl_create_channel) {
            //创建频道
            //去创建频道页面
            CreateChannelActivity.actionStart(mContext, server);
            hide();
        } else if (v.getId() == R.id.csl_create_category) {
            //创建频道分组
            CreateCategoryActivity.actionStart(mContext, server);
            hide();
        } else if (v.getId() == R.id.csl_server_members) {
            //查看社区成员
            ServerMembersActivity.actionStart(mContext, server);
        } else if (v.getId() == R.id.csl_exit_server) {
            //退出社区
            mServerViewModel.leaveServer(server.serverId);
        }
    }
}
