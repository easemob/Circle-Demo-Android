package io.agora.contacts.ui.category;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.utils.TextUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCircleChannelCategory;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityCreateCategoryBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.model.CategoryViewModel;

public class CreateCategoryActivity extends BaseInitActivity<ActivityCreateCategoryBinding> implements View.OnClickListener {
    //输入框初始值
    private int namePrimaryNum = 0;
    //输入框最大值
    public int mMaxNameNum = 50;
    private CategoryViewModel mViewModel;
    private CircleServer server;

    public static void actionStart(Context context, CircleServer server) {
        Intent intent = new Intent(context, CreateCategoryActivity.class);
        intent.putExtra(Constants.SERVER, (Serializable) server);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_create_category;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        mViewModel.createCategoryLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EMCircleChannelCategory>() {
                @Override
                public void onSuccess(@Nullable EMCircleChannelCategory circleChannel) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.create_category_success));
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

        mBinding.edtCategoryName.addTextChangedListener(new TextWatcher() {
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
                mBinding.tvNameCount.setText("" + number + "/50");
                selectionStart = mBinding.edtCategoryName.getSelectionStart();
                selectionEnd = mBinding.edtCategoryName.getSelectionEnd();
                //判断大于最大值
                if (wordNum.length() > mMaxNameNum) {
                    //删除多余输入的字（不会显示出来）
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    mBinding.edtCategoryName.setText(s);
                    mBinding.edtCategoryName.setSelection(tempSelection);//设置光标在最后
                }
                checkCreateChannelButtonStatus();
            }
        });
        mBinding.tvCreate.setOnClickListener(this);
        mBinding.ivBack.setOnClickListener(this);
    }

    private void checkCreateChannelButtonStatus() {
        String name = mBinding.edtCategoryName.getText().toString().trim();
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
    public void onClick(View v) {
        if (v.getId() == R.id.tv_create) {
            String name = mBinding.edtCategoryName.getText().toString().trim();
            mViewModel.createCategory(server.serverId, name);
        } else if (v.getId() == R.id.iv_back) {
            finish();
        }
    }
}