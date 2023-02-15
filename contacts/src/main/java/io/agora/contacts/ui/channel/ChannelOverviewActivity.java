package io.agora.contacts.ui.channel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.utils.TextUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCircleChannelAttribute;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityChannelOverviewBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.global.Constants;
import io.agora.service.model.ChannelViewModel;

public class ChannelOverviewActivity extends BaseInitActivity<ActivityChannelOverviewBinding> implements View.OnClickListener {
    //输入框初始值
    private int namePrimaryNum = 0;
    private int descPrimaryNum = 0;
    //输入框最大值
    public int mMaxNameNum = 16;
    public int mMaxDescNum = 120;

    private ChannelViewModel mViewModel;
    private CircleChannel channel;

    public static void actionStart(Context context, CircleChannel channel) {
        Intent intent = new Intent(context, ChannelOverviewActivity.class);
        intent.putExtra(Constants.CHANNEL, (Serializable) channel);
        context.startActivity(intent);
    }


    @Override
    protected int getResLayoutId() {
        return R.layout.activity_channel_overview;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        mViewModel.updateChannelLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleChannel>() {
                @Override
                public void onSuccess(@Nullable CircleChannel circleChannel) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.update_channel_success));
                    finish();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    if(!TextUtils.isEmpty(message)) {
                        ToastUtils.showShort( message);
                    }
                }
            });
        });
        mBinding.setVm(mViewModel);
        initListener();
    }

    private void initListener() {

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
        mBinding.edtChannelDesc.addTextChangedListener(new TextWatcher() {
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
                int number = descPrimaryNum + s.length();
                //TextView显示剩余字数
                mBinding.tvDescCount.setText("" + number + "/120");
                selectionStart = mBinding.edtChannelDesc.getSelectionStart();
                selectionEnd = mBinding.edtChannelDesc.getSelectionEnd();
                //判断大于最大值
                if (wordNum.length() > mMaxDescNum) {
                    //删除多余输入的字（不会显示出来）
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    mBinding.edtChannelDesc.setText(s);
                    mBinding.edtChannelDesc.setSelection(tempSelection);//设置光标在最后
                }
                checkCreateChannelButtonStatus();
            }
        });
        mBinding.ivBack.setOnClickListener(this);
        mBinding.tvSave.setOnClickListener(this);
    }

    private void checkCreateChannelButtonStatus() {
        String name = mBinding.edtChannelName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            mBinding.tvSave.setEnabled(false);
        } else {
            mBinding.tvSave.setEnabled(true);
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        channel = (CircleChannel) getIntent().getSerializableExtra(Constants.CHANNEL);
        if(channel.channelMode==1) {
            mBinding.groupDesc.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData() {
        super.initData();

        if (channel != null) {
            mViewModel.channelName.set(channel.name);
            mBinding.edtChannelDesc.setText(channel.desc);
        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.iv_back) {
            finish();
        }else if(v.getId()==R.id.tv_save) {

            String name = mBinding.edtChannelName.getText().toString().trim();
            String desc = mBinding.edtChannelDesc.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtils.showShort(getString(io.agora.service.R.string.home_channel_name_is_null));
                return;
            }
            EMCircleChannelAttribute attribute = new EMCircleChannelAttribute();
            attribute.setName(name);
            attribute.setDesc(desc);
            mViewModel.updateChannel(channel, attribute);
        }
    }
}