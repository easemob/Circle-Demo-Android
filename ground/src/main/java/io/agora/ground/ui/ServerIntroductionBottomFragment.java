package io.agora.ground.ui;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.hyphenate.easeui.utils.ShowMode;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.Map;

import io.agora.ground.R;
import io.agora.ground.databinding.FragmentJoinServerBinding;
import io.agora.ground.model.GroundViewModel;
import io.agora.service.base.BaseBottomSheetFragment;
import io.agora.service.base.BaseInitFragment;
import io.agora.service.callbacks.BottomSheetChildHelper;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.managers.AppUserInfoManager;
import io.agora.service.repo.ServiceReposity;
import io.agora.service.widget.FlowlayoutAdapter;

/**
 * 广场页面点击item、首页简介点击更多图标弹窗页
 */
public class ServerIntroductionBottomFragment extends BaseInitFragment<FragmentJoinServerBinding> implements BottomSheetChildHelper, View.OnClickListener {

    private CircleServer server;
    private GroundViewModel mViewModel;
    private BaseBottomSheetFragment parentFragment;

    @Override
    public void onContainerTitleBarInitialize(EaseTitleBar titlebar) {
        BottomSheetChildHelper.super.onContainerTitleBarInitialize(titlebar);
        titlebar.setVisibility(View.GONE);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_join_server;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Bundle arguments = getArguments();
        server = (CircleServer) arguments.get(Constants.SERVER);
        if (server != null) {
            Map<String, CircleServer> userJoinedSevers = AppUserInfoManager.getInstance().getUserJoinedSevers();
            if (userJoinedSevers.get(server.serverId) != null) {
                mBinding.btnJoinServer.setVisibility(View.GONE);
            } else {
                mBinding.btnJoinServer.setVisibility(View.VISIBLE);
            }
        }
        mBinding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                if(parentFragment!=null) {
                    parentFragment.setLayoutHeight(mBinding.cslIntroduction.getHeight());
                }
            }
        });
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(GroundViewModel.class);
        mViewModel.joinServerLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleServer>() {
                @Override
                public void onSuccess(@Nullable CircleServer circleServer) {
                    if (circleServer != null) {
                        ToastUtils.showShort(getString(io.agora.service.R.string.circle_join_in_server_success));
                        //发送广播
                        LiveEventBus.get(Constants.SERVER_UPDATED).post(circleServer);

                        //跳转到首页，显示社区详情
                        Postcard postcard = ARouter.getInstance().build("/app/MainActivity");
                        //直接跳转到首页,显示目标server详情
                        postcard.withSerializable(Constants.SHOW_MODE, ShowMode.NORMAL);
                        postcard.withInt(Constants.NAV_POSITION, 0);
                        postcard.withParcelable(Constants.SERVER, circleServer);
                        postcard.navigation();
                    }
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
        mBinding.btnJoinServer.setOnClickListener(this);
        mBinding.llFold.setOnClickListener(this);
    }

    @Override
    public void setParentContainerFragment(BaseBottomSheetFragment fragment) {
        this.parentFragment = fragment;
    }

    @Override
    protected void initData() {
        super.initData();
        if (server != null) {
            Glide.with(mContext)
                    .load(server.background)
                    .placeholder(ServiceReposity.getRandomServerBg(server.serverId))
                    .into(mBinding.ivServer);
            Glide.with(mContext)
                    .load(server.icon)
                    .placeholder(ServiceReposity.getRandomServerIcon(server.serverId))
                    .into(mBinding.ivServerIcon);

            mBinding.tvServerName.setText(server.name);
            mBinding.tvDesc.setText(server.desc);
            mBinding.flTag.setAdapter(new FlowlayoutAdapter() {
                @Override
                public int getCount() {
                    return server.tags.size();
                }

                @Override
                public View getView(int position, ViewGroup parent) {
                    final android.widget.TextView textView = (android.widget.TextView) LayoutInflater.from(mContext).inflate(R.layout.tag_view, parent, false);
                    Drawable drawableLeft = mContext.getResources().getDrawable(io.agora.service.R.drawable.circle_bookmark);
                    textView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                            null, null, null);
                    textView.setCompoundDrawablePadding(4);
                    textView.setText(server.tags.get(position).tag_name);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    textView.setTextColor(Color.WHITE);
                    textView.setPadding(ConvertUtils.dp2px(5), ConvertUtils.dp2px(5), ConvertUtils.dp2px(5), ConvertUtils.dp2px(5));
                    textView.setBackground(mContext.getDrawable(io.agora.service.R.drawable.shape_white26fff_radius4));
                    return textView;
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_join_server) {
            mViewModel.joinServer(server.serverId);
        } else if (v.getId() == R.id.ll_fold) {
            hide();
        }
    }
}
