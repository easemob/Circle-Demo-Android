package io.agora.contacts.ui.server;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityServerNotificationSettingBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.global.OptionsHelper;

public class ServerNotificationSettingActivity extends BaseInitActivity<ActivityServerNotificationSettingBinding> {

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_server_notification_setting;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mBinding.rgNotification.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_all_msg) {
                    OptionsHelper.getInstance().setAllowNotifyAllMessages(true);
                } else if (checkedId == R.id.rb_no_notify) {
                    OptionsHelper.getInstance().setAllowNotifyAllMessages(false);
                }
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