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

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityEditChannelBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.CircleVoiceChannelStateListener;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.entity.CircleCategory;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.global.Constants;
import io.agora.service.model.ChannelViewModel;

@Route(path = "/contacts/EditChannelActivity")
public class EditChannelActivity extends BaseInitActivity<ActivityEditChannelBinding>
        implements View.OnClickListener {

    private CircleChannel channel;
    private ChannelViewModel mChannelViewModel;
    private CircleVoiceChannelStateListener circleVoiceChannelStateListener;

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
                    ToastUtils.showShort(getString(R.string.delete_channel_success));
                    //发出通知
                    //注意语聊频道还要所有人退出channel
                    LiveEventBus.get(Constants.CHANNEL_DELETE).post(circleChannel);
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
            CircleCategory category = DatabaseManager.getInstance().getCagegoryDao().getCategoryByCagegoryID(channel.channelId);
            if (category != null) {
                mBinding.tvCategoryName.setText(category.categoryName);
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
            mChannelViewModel.deleteChannel(channel);
        } else if (v.getId() == R.id.csl_channel_setting) {
            ChannelSettingActivity.actionStart(this, channel);
        }
    }
}