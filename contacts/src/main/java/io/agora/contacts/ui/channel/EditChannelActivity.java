package io.agora.contacts.ui.channel;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.io.Serializable;

import io.agora.common.dialog.AlertDialog;
import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityEditChannelBinding;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.CircleVoiceChannelStateListener;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.entity.CircleCategory;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.global.Constants;
import io.agora.service.managers.CircleRTCManager;
import io.agora.service.model.ChannelViewModel;

@Route(path = "/contacts/EditChannelActivity")
public class EditChannelActivity extends BaseInitActivity<ActivityEditChannelBinding>
        implements View.OnClickListener {

    private CircleChannel channel;
    private ChannelViewModel mChannelViewModel;
    private CircleVoiceChannelStateListener circleVoiceChannelStateListener;
    private AlertDialog dialog;
    private IRtcEngineEventHandler eventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onLeaveChannel(RtcStats stats) {
            //退出声网后再退聊天室
            if (channel != null) {
                mChannelViewModel.deleteChannel(channel);
            }
        }
    };

    public static void actionStart(Context context, CircleChannel channel) {
        Intent intent = new Intent(context, EditChannelActivity.class);
        intent.putExtra(Constants.CHANNEL, (Serializable) channel);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_edit_channel;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        channel = (CircleChannel) getIntent().getSerializableExtra(Constants.CHANNEL);
        mChannelViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        mChannelViewModel.deleteChannelResultLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleChannel>() {
                @Override
                public void onSuccess(@Nullable CircleChannel circleChannel) {
                    if(circleChannel.channelMode==1) {
                        ToastUtils.showShort(getString(R.string.delete_voice_channel_success));
                    }else{
                        ToastUtils.showShort(getString(R.string.delete_channel_success));
                    }
                    finish();
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
        if (channel != null) {
            CircleCategory category = DatabaseManager.getInstance().getCagegoryDao().getCategoryByCagegoryID(channel.categoryId);
            if (category != null) {
                mBinding.tvCategoryName.setText(category.categoryName.equals(Constants.DEFAULT_CATEGORY_NAME) ? getString(io.agora.service.R.string.circle_default_category) : category.categoryName);
            }
            if (channel.isDefault) {
                mBinding.cslDeleteChannel.setVisibility(View.GONE);
            }
        }
        mBinding.ivBack.setOnClickListener(this);
        mBinding.cslChannelOverview.setOnClickListener(this);
        mBinding.cslChannelSetting.setOnClickListener(this);
        mBinding.cslMoveChannel.setOnClickListener(this);
        mBinding.cslDeleteChannel.setOnClickListener(this);

        LiveEventBus.get(Constants.CHANNEL_CHANGED, CircleChannel.class).observe(this, channelUpdated -> {
            if (channelUpdated != null && channel != null && android.text.TextUtils.equals(channel.channelId, channelUpdated.channelId)) {
                channel = channelUpdated;
            }
        });

        if(channel.channelMode==1) {//语聊房频道
            CircleRTCManager.getInstance().registerRTCEventListener(eventHandler);
        }
    }

    private void showDeleteChannelDialog() {

        dialog = new AlertDialog.Builder(this)
                .setContentView(R.layout.dialog_delete_channel)
                .setText(R.id.tv_content, getString(R.string.circle_delete_channel_message, channel != null ? channel.name : ""))
                .setOnClickListener(R.id.tv_confirm, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ((!TextUtils.isEmpty(channel.channelId)) && TextUtils.equals(channel.channelId, CircleRTCManager.getInstance().getChannelId())) {
                            //退出语聊房，先退声网，再退聊天室
                            CircleRTCManager.getInstance().leaveChannel();
                        }else{
                            mChannelViewModel.deleteChannel(channel);
                        }
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
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.csl_channel_overview) {
            ChannelOverviewActivity.actionStart(this, channel);
        } else if (v.getId() == R.id.csl_move_channel) {
            MoveChannelActivity.actionStart(this, channel);
        } else if (v.getId() == R.id.csl_delete_channel) {
            showDeleteChannelDialog();
        } else if (v.getId() == R.id.csl_channel_setting) {
            ChannelSettingActivity.actionStart(this, channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(channel.channelMode==1) {
            CircleRTCManager.getInstance().unRegisterRTCEventListener(eventHandler);
        }
    }
}