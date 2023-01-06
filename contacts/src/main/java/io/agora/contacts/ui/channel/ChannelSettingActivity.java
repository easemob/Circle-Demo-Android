package io.agora.contacts.ui.channel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.utils.TextUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCircleChannelAttribute;
import com.hyphenate.chat.EMCircleChannelStyle;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityChannelSettingBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.CircleVoiceChannelStateListener;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.global.Constants;
import io.agora.service.model.ChannelViewModel;
import io.agora.service.widget.SwitchItemView;

public class ChannelSettingActivity extends BaseInitActivity<ActivityChannelSettingBinding>
        implements SwitchItemView.OnCheckedChangeListener, View.OnClickListener, CircleVoiceChannelStateListener {

    private EMCircleChannelStyle channelStyle = EMCircleChannelStyle.EMChannelStylePublic;
    private ChannelViewModel mViewModel;
    private CircleChannel channel;

    public static void actionStart(Context context, CircleChannel channel) {
        Intent intent = new Intent(context, ChannelSettingActivity.class);
        intent.putExtra(Constants.CHANNEL, (Serializable) channel);
        context.startActivity(intent);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        channel = (CircleChannel) getIntent().getSerializableExtra(Constants.CHANNEL);
        if (channel != null) {
            mBinding.swiPrivate.getSwitch().setChecked(channel.type == 0);
            mBinding.groupCount.setVisibility(channel.channelMode == 1 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_channel_setting;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);

        mViewModel.updateChannelLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleChannel>() {
                @Override
                public void onSuccess(@Nullable CircleChannel channel) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.update_channel_success));
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
        mBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser&&channel != null) {
                    EMCircleChannelAttribute attribute = new EMCircleChannelAttribute();
                    attribute.setSeatCount(progress);
                    mViewModel.updateChannel(channel, attribute);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        if (!isChecked) {
            channelStyle = EMCircleChannelStyle.EMChannelStylePrivate;
        } else {
            channelStyle = EMCircleChannelStyle.EMChannelStylePublic;
        }
        if (channel != null) {
            EMCircleChannelAttribute attribute = new EMCircleChannelAttribute();
            attribute.setType(channelStyle);
            mViewModel.updateChannel(channel, attribute);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        }
    }
}