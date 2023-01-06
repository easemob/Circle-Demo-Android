package io.agora.contacts.ui.category;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ToastUtils;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityEditCategoryBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleCategory;
import io.agora.service.global.Constants;
import io.agora.service.model.CategoryViewModel;

@Route(path = "/contacts/EditCategoryActivity")
public class EditCategoryActivity extends BaseInitActivity<ActivityEditCategoryBinding> implements View.OnClickListener {

    private CategoryViewModel viewModel;
    private CircleCategory category;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_edit_category;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        category = (CircleCategory) getIntent().getSerializableExtra(Constants.CATEGORY);
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        viewModel.deleteCategoryLiveData.observe(this,response->{
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    ToastUtils.showShort(getString(io.agora.service.R.string.circle_delete_success));
                    finish();
                }
            });
        });
        mBinding.ivBack.setOnClickListener(this);
        mBinding.cslCategoryOverview.setOnClickListener(this);
        mBinding.cslCategorySetting.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.iv_back) {
            finish();
        }else if(v.getId()==R.id.csl_category_overview) {
            if(category!=null) {
                EditCategoryNameActivity.actionStart(this,category);
            }
        }else if(v.getId()==R.id.csl_category_setting) {
            if(category!=null) {
                viewModel.deleteCategory(category.serverId,category.categoryId);
            }
        }
    }
}