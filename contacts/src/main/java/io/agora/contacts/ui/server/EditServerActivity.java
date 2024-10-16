package io.agora.contacts.ui.server;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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

import io.agora.common.dialog.AlertDialog;
import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityEditServerBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.dao.CircleChannelDao;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.managers.CircleRTCManager;
import io.agora.service.model.ServerViewModel;
import io.agora.service.repo.ServiceReposity;
import io.agora.service.utils.UriFormatUtils;

public class EditServerActivity extends BaseInitActivity<ActivityEditServerBinding> implements View.OnClickListener {
    private static final int REQUEST_CODE_SERVER_BG = 1;
    private static final int REQUEST_CODE_SERVER_ICON = 2;
    private String bgImagePath;
    private String iconImagePath;
    private CircleServer server;
    private RxPermissions rxPermissions;
    private ServerViewModel mViewModel;
    private AlertDialog dialog;

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
                        checkoutRtcChannelState();
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
            if (serverUpdated != null && server != null && android.text.TextUtils.equals(server.serverId, serverUpdated.serverId)) {
                server = serverUpdated;
            }
        });
        mBinding.btnEditCover.setOnClickListener(this);
        mBinding.cslServerOverview.setOnClickListener(this);
        mBinding.cslServerSetting.setOnClickListener(this);
        mBinding.cslServerDissolve.setOnClickListener(this);
        mBinding.ivBack.setOnClickListener(this);
        mBinding.ivServerIcon.setOnClickListener(this);
    }

    private void checkoutRtcChannelState() {
        //检查自己是否在对应社区的语聊房里，是就退出
        String channelId = CircleRTCManager.getInstance().getChannelId();
        CircleChannelDao channelDao = DatabaseManager.getInstance().getChannelDao();
        if (!android.text.TextUtils.isEmpty(channelId)&&channelDao!=null) {
            CircleChannel circleChannel = channelDao.getChannelByChannelID(channelId);
            if (circleChannel != null&&server!=null) {
                if (!android.text.TextUtils.isEmpty(server.serverId)&& android.text.TextUtils.equals(circleChannel.serverId, server.serverId)) {
                    //所在社区删除就退出语聊房
                    CircleRTCManager.getInstance().leaveChannel();
                }
            }
        }
    }

    @Override
    protected void initData() {
        super.initData();
        rxPermissions = new RxPermissions(this);
        server = (CircleServer) getIntent().getSerializableExtra(Constants.SERVER);
        if (server != null) {
            Glide.with(this).load(server.background).placeholder(ServiceReposity.getRandomServerBg(server.serverId)).into(mBinding.ivServerBg);
            Glide.with(this).load(server.icon).placeholder(ServiceReposity.getRandomServerIcon(server.serverId)).into(mBinding.ivServerIcon);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SERVER_BG) { // send local image
                onActivityResultForLocalBgPhotos(data);
            } else if (requestCode == REQUEST_CODE_SERVER_ICON) {
                onActivityResultForLocalIconPhotos(data);
            }
        }
    }

    /**
     * 选择本地图片处理结果
     *
     * @param data
     */
    private void onActivityResultForLocalBgPhotos(@Nullable Intent data) {
        if (data != null) {
            try {
                Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                bgImagePath = UriFormatUtils.getPathByUri4kitkat(mContext, selectedImage);
                Glide.with(this).load(bgImagePath).placeholder(io.agora.service.R.drawable.circle_default_avatar).into(mBinding.ivServerBg);
                if (server != null) {
                    mViewModel.updateServerBg(server, bgImagePath);
                }
            } catch (Exception e) {
                bgImagePath = null;
                e.printStackTrace();
            }
        }
    }

    /**
     * 选择本地图片处理结果
     *
     * @param data
     */
    private void onActivityResultForLocalIconPhotos(@Nullable Intent data) {
        if (data != null) {
            try {
                Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                iconImagePath = UriFormatUtils.getPathByUri4kitkat(mContext, selectedImage);
                Glide.with(this).load(iconImagePath).placeholder(io.agora.service.R.drawable.circle_default_avatar).into(mBinding.ivServerIcon);
                if (server != null) {
                    mViewModel.updateServer(server, iconImagePath, server.name, server.desc);
                }
            } catch (Exception e) {
                iconImagePath = null;
                e.printStackTrace();
            }
        }
    }

    private void showDissolveServerDialog() {

        dialog = new AlertDialog.Builder(this)
                .setContentView(R.layout.dialog_dissolve_server)
                .setText(R.id.tv_content, getString(R.string.circle_delete_server_message, server != null ? server.name : ""))
                .setOnClickListener(R.id.tv_confirm, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.deleteServer(server.serverId);
                        dialog.dismiss();
                    }
                })
                .setOnClickListener(R.id.tv_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void onClick(View v) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissions= new String[]{ Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO};
        }else{
            permissions= new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        if (v.getId() == R.id.csl_server_setting) {
            ServerSettingActivity.actionStart(this, server);
        } else if (v.getId() == R.id.csl_server_overview) {
            ServerOverviewActivity.actionStart(this, server);
        } else if (v.getId() == R.id.csl_server_dissolve) {
            showDissolveServerDialog();
        } else if (v.getId() == R.id.btn_edit_cover) {
            //去相册选择
            //申请权限
            rxPermissions
                    .request(permissions)
                    .subscribe(granted -> {
                        if (granted) {
                            // All requested permissions are granted
                            //去相册选择
                            EaseCompat.openImage(this, REQUEST_CODE_SERVER_BG);
                        }
                    });
        } else if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.iv_server_icon) {
            //去相册选择
            //申请权限
            rxPermissions
                    .request(permissions)
                    .subscribe(granted -> {
                        if (granted) {
                            // All requested permissions are granted
                            //去相册选择
                            EaseCompat.openImage(this, REQUEST_CODE_SERVER_ICON);
                        }
                    });
        }
    }
}