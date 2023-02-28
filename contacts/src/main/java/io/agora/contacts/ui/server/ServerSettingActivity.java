package io.agora.contacts.ui.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.utils.TextUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCircleServerAttribute;
import com.hyphenate.chat.EMCircleServerType;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityServerSettingBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.model.ServerViewModel;
import io.agora.service.widget.SwitchItemView;

public class ServerSettingActivity extends BaseInitActivity<ActivityServerSettingBinding> implements SwitchItemView.OnCheckedChangeListener, View.OnClickListener {

    private EMCircleServerType serverType = EMCircleServerType.EM_CIRCLE_SERVER_TYPE_PUBLIC;
    private ServerViewModel mViewModel;
    private CircleServer server;

    public static void actionStart(Context context, CircleServer server) {
        Intent intent = new Intent(context, ServerSettingActivity.class);
        intent.putExtra(Constants.SERVER, (Serializable) server);
        context.startActivity(intent);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        server = (CircleServer) getIntent().getSerializableExtra(Constants.SERVER);
        mBinding.swiPrivate.getSwitch().setChecked(server.type==0);
    }

    @Override
    protected void initData() {
        super.initData();

    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_server_setting;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(ServerViewModel.class);

        mViewModel.updateServerLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleServer>() {
                @Override
                public void onSuccess(@Nullable CircleServer circleServer) {
                    //隐藏进度条
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_update_success));
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

        mBinding.swiPrivate.setOnCheckedChangeListener(this);
        mBinding.ivBack.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        if (!isChecked) {
            serverType = EMCircleServerType.EM_CIRCLE_SERVER_TYPE_PRIVATE;
        } else {
            serverType = EMCircleServerType.EM_CIRCLE_SERVER_TYPE_PUBLIC;
        }
        if (server != null) {
            EMCircleServerAttribute attribute = new EMCircleServerAttribute();
            attribute.setType(serverType);
            mViewModel.updateServer(server.serverId, attribute);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.iv_back) {
            finish();
        }
    }
}