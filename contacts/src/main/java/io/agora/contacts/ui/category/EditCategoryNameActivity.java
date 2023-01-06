package io.agora.contacts.ui.category;

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

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityEditCategoryNameBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleCategory;
import io.agora.service.global.Constants;
import io.agora.service.model.CategoryViewModel;

public class EditCategoryNameActivity extends BaseInitActivity<ActivityEditCategoryNameBinding> implements View.OnClickListener {

    //输入框初始值
    private int namePrimaryNum = 0;
    //输入框最大值
    public int mMaxNameNum = 50;
    private CategoryViewModel mViewModel;
    private CircleCategory category;

    public static void actionStart(Context context, CircleCategory category) {
        Intent intent = new Intent(context, EditCategoryNameActivity.class);
        intent.putExtra(Constants.CATEGORY, category);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_edit_category_name;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        mViewModel.updateCategoryLiveData.observe(this,response->{
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    dismissLoading();
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_update_success));
                    finish();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    dismissLoading();
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_update_failure));
                    finish();
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

        mBinding.tvSave.setOnClickListener(this);
        mBinding.ivBack.setOnClickListener(this);
    }

    private void checkCreateChannelButtonStatus() {
        String name = mBinding.edtCategoryName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            mBinding.tvSave.setEnabled(false);
        } else {
            mBinding.tvSave.setEnabled(true);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        category = (CircleCategory) getIntent().getSerializableExtra(Constants.CATEGORY);
        mBinding.setVm(mViewModel);

        if (category != null) {
            String name = category.categoryName;
            if (name != null) {
                mViewModel.cagegoryName.set(name);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_save) {
            String name = mBinding.edtCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtils.showShort(getString(io.agora.service.R.string.circle_category_name_is_null));
                return;
            }
            showLoading(null);
            if (category != null) {
                mViewModel.updateCategory(category.serverId, category.categoryId,name);
            }
        } else if (v.getId() == R.id.iv_back) {
            finish();
        }
    }
}