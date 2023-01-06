package io.agora.contacts.ui.channel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityChannelNotificationSettingBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleMute;
import io.agora.service.global.Constants;
import io.agora.service.global.OptionsHelper;

public class ChannelNotificationSettingActivity extends BaseInitActivity<ActivityChannelNotificationSettingBinding> {

    private CircleChannel channel;

    public static void actionStart(Context context, CircleChannel circleChannel) {
        Intent intent = new Intent(context, ChannelNotificationSettingActivity.class);
        intent.putExtra(Constants.CHANNEL, (Serializable) circleChannel);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_channel_notification_setting;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        channel = (CircleChannel) getIntent().getSerializableExtra(Constants.CHANNEL);
        mBinding.rgNotification.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(channel==null) {
                    return;
                }
                CircleMute circleMute = new CircleMute(channel.channelId);
                if (checkedId == R.id.rb_all_msg) {
                    circleMute.isMuted=true;
                } else if (checkedId == R.id.rb_no_notify) {
                    circleMute.isMuted=false;
                }
                DatabaseManager.getInstance().getCircleMuteDao().insert(circleMute);
            }
        });

        mBinding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        mBinding.rgNotification.check(OptionsHelper.getInstance().IsAllowNotifyAllMessages() ? R.id.rb_all_msg : R.id.rb_no_notify);
    }
}