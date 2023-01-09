package io.agora.contacts.ui.channel;

import static com.hyphenate.chat.EMPushManager.EMPushRemindType.ALL;
import static com.hyphenate.chat.EMPushManager.EMPushRemindType.NONE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMSilentModeParam;
import com.hyphenate.chat.EMSilentModeResult;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityChannelNotificationSettingBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleMute;
import io.agora.service.global.Constants;
import io.agora.service.model.ChatViewModel;

public class ChannelNotificationSettingActivity extends BaseInitActivity<ActivityChannelNotificationSettingBinding> {

    private CircleChannel channel;
    private ChatViewModel viewModel;

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
        viewModel=new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.setSilentModeForConversationLiveData.observe(this,response->{
            parseResource(response, new OnResourceParseCallback<EMSilentModeResult>() {
                @Override
                public void onSuccess(@Nullable EMSilentModeResult result) {
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
        mBinding.rgNotification.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(channel==null) {
                    return;
                }
                EMSilentModeParam emSilentModeParam = new EMSilentModeParam(EMSilentModeParam.EMSilentModeParamType.REMIND_TYPE);
                CircleMute circleMute = new CircleMute(channel.channelId);
                if (checkedId == R.id.rb_all_msg) {
                    circleMute.isMuted=true;
                    emSilentModeParam.setRemindType(ALL);
                } else if (checkedId == R.id.rb_no_notify) {
                    circleMute.isMuted=false;
                    emSilentModeParam.setRemindType(NONE);
                }
                DatabaseManager.getInstance().getCircleMuteDao().insert(circleMute);
                //设置免打扰
                viewModel.setSilentModeForConversation(channel.channelId, EMConversation.EMConversationType.GroupChat,emSilentModeParam);
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