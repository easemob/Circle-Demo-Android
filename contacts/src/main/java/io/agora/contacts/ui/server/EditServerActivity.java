package io.agora.contacts.ui.server;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.utils.TextUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.hyphenate.easeui.utils.EaseCompat;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityEditServerBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.model.ServerViewModel;
import io.agora.service.utils.UriFormatUtils;

public class EditServerActivity extends BaseInitActivity<ActivityEditServerBinding> implements View.OnClickListener {
    private static final int REQUEST_CODE_LOCAL = 1;
    private String imagePath;
    private CircleServer server;
    private RxPermissions rxPermissions;
    private ServerViewModel mViewModel;

    public static void actionStart(Context context, CircleServer server) {
        Intent intent = new Intent(context, EditServerActivity.class);
        intent.putExtra(Constants.SERVER, (Serializable) server);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_edit_server;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
        mViewModel.deleteServerResultLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    if (data) {
                        ToastUtils.showShort(getString(R.string.delete_server_success));
                       finish();
                    } else {
                        ToastUtils.showShort(getString(R.string.delete_server_failure));
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    if (!android.text.TextUtils.isEmpty(message)) {
                        ToastUtils.showShort(message);
                    }
                }
            });
        });
        mViewModel.updateServerLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleServer>() {
                @Override
                public void onSuccess(@Nullable CircleServer circleServer) {
                    //隐藏进度条
                    dismissLoading();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    dismissLoading();
                    if (!TextUtils.isEmpty(message)) {
                        ToastUtils.showShort(message);
                    }
                }
            });
        });
        LiveEventBus.get(Constants.SERVER_UPDATED, CircleServer.class).observe(this, serverUpdated -> {
            if (serverUpdated != null&&server!=null&& android.text.TextUtils.equals(server.serverId,serverUpdated.serverId)) {
                server=serverUpdated;
            }
        });
        mBinding.btnEditCover.setOnClickListener(this);
        mBinding.cslServerOverview.setOnClickListener(this);
        mBinding.cslServerSetting.setOnClickListener(this);
        mBinding.cslServerDissolve.setOnClickListener(this);
        mBinding.ivBack.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        rxPermissions = new RxPermissions(this);
        server = (CircleServer) getIntent().getSerializableExtra(Constants.SERVER);
        if (server != null) {
            Glide.with(this).load(server.icon).placeholder(io.agora.service.R.drawable.circle_default_avatar).into(mBinding.ivServer);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                onActivityResultForLocalPhotos(data);
            }
        }
    }

    /**
     * 选择本地图片处理结果
     *
     * @param data
     */
    private void onActivityResultForLocalPhotos(@Nullable Intent data) {
        if (data != null) {
            try {
                Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                imagePath = UriFormatUtils.getPathByUri4kitkat(mContext, selectedImage);
                Glide.with(this).load(imagePath).placeholder(io.agora.service.R.drawable.circle_default_avatar).into(mBinding.ivServer);
                if (server != null) {
                    mViewModel.updateServer(server, imagePath, server.name, server.desc);
                }
            } catch (Exception e) {
                imagePath = null;
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.csl_server_setting) {
            ServerSettingActivity.actionStart(this, server);
        } else if (v.getId() == R.id.csl_server_overview) {
            ServerOverviewActivity.actionStart(this, server);
        } else if (v.getId() == R.id.csl_server_dissolve) {
            mViewModel.deleteServer(server.serverId);
        } else if (v.getId() == R.id.btn_edit_cover) {
            //去相册选择
            //申请权限
            rxPermissions
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {
                            // All requested permissions are granted
                            //去相册选择
                            EaseCompat.openImage(this, REQUEST_CODE_LOCAL);
                        }
                    });
        }else if(v.getId()==R.id.iv_back) {
            finish();
        }
    }
}