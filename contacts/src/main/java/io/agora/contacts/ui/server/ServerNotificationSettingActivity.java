package io.agora.contacts.ui.server;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityServerNotificationSettingBinding;
import io.agora.service.base.BaseInitActivity;

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


                } else if (checkedId == R.id.rb_no_notify) {
                    //对该社区下所有的channel设置为禁言


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

    }
}