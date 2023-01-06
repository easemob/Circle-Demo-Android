package io.agora.contacts.ui.channel;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.utils.TextUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCircleChannelAttribute;
import com.hyphenate.chat.EMCircleChannelMode;
import com.hyphenate.chat.EMCircleChannelRank;
import com.hyphenate.chat.EMCircleChannelStyle;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityCreateChannelBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.model.ChannelViewModel;
import io.agora.service.widget.SwitchItemView;

public class CreateChannelActivity extends BaseInitActivity<ActivityCreateChannelBinding> implements SwitchItemView.OnCheckedChangeListener, View.OnClickListener {
    //输入框初始值
    private int namePrimaryNum = 0;
    //输入框最大值
    public int mMaxNameNum = 16;

    private ChannelViewModel mViewModel;
    private CircleServer server;
    private EMCircleChannelStyle style = EMCircleChannelStyle.EMChannelStylePublic;
    private EMCircleChannelMode mode = EMCircleChannelMode.EMCircleChannelModeChat;

    public static void actionStart(Context context, CircleServer server) {
        Intent intent = new Intent(context, CreateChannelActivity.class);
        intent.putExtra(Constants.SERVER, (Serializable) server);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_create_channel;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        mViewModel.createChannelResultLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleChannel>() {
                @Override
                public void onSuccess(@Nullable CircleChannel circleChannel) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.create_channel_success));
                    //跳转到聊天页面
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
        initListener();
    }

    private void initListener() {
        mBinding.rgChannelType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_text_channel) {
                    mode = EMCircleChannelMode.EMCircleChannelModeChat;
                } else if (checkedId == R.id.rb_voice_channel) {
                    mode = EMCircleChannelMode.EMCircleChannelModeVoice;
                }
            }
        });

        mBinding.edtChannelName.addTextChangedListener(new TextWatcher() {
            //记录输入的字数
            private CharSequence wordNum;
            private int selectionStart;
            private int selectionEnd;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //实时记录输入的字数
                wordNum = s;
            }

            @Override
            public void afterTextChanged(Editable s) {
                int number = namePrimaryNum + s.length();
                //TextView显示剩余字数
                mBinding.tvNameCount.setText("" + number + "/16");
                selectionStart = mBinding.edtChannelName.getSelectionStart();
                selectionEnd = mBinding.edtChannelName.getSelectionEnd();
                //判断大于最大值
                if (wordNum.length() > mMaxNameNum) {
                    //删除多余输入的字（不会显示出来）
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    mBinding.edtChannelName.setText(s);
                    mBinding.edtChannelName.setSelection(tempSelection);//设置光标在最后
                }
                checkCreateChannelButtonStatus();
            }
        });
        mBinding.rgChannelType.check(R.id.rb_text_channel);
        mBinding.swiPrivate.setOnCheckedChangeListener(this);
        mBinding.tvCreate.setOnClickListener(this);
        mBinding.ivBack.setOnClickListener(this);
    }

    private void checkCreateChannelButtonStatus() {
        String name = mBinding.edtChannelName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            mBinding.tvCreate.setEnabled(false);
        } else {
            mBinding.tvCreate.setEnabled(true);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        server = (CircleServer) getIntent().getSerializableExtra(Constants.SERVER);
        mBinding.setVm(mViewModel);
    }

    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        if (isChecked) {
            style = EMCircleChannelStyle.EMChannelStylePrivate;
        } else {
            style = EMCircleChannelStyle.EMChannelStylePublic;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_create) {
            String name = mBinding.edtChannelName.getText().toString().trim();
            String desc = "";
            if (TextUtils.isEmpty(name)) {
                ToastUtils.showShort(getString(io.agora.service.R.string.home_channel_name_is_null));
                return;
            }
            EMCircleChannelAttribute attribute = new EMCircleChannelAttribute();
            attribute.setName(name);
            attribute.setDesc(desc);
            attribute.setRank(EMCircleChannelRank.RANK_2000);
            attribute.setType(style);
            mViewModel.createChannel(mode, server.serverId, null, attribute, style);
        } else if (v.getId() == R.id.iv_back) {
            finish();
        }
    }
}