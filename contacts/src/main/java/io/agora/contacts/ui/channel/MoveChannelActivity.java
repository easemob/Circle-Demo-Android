package io.agora.contacts.ui.channel;

import static io.agora.service.global.Constants.DEFAULT_CATEGORY_NAME;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.agora.common.base.BaseAdapter;
import io.agora.contacts.R;
import io.agora.contacts.adapter.CategoryListAdapter;
import io.agora.contacts.bean.CircleCategoryData;
import io.agora.contacts.databinding.ActivityMoveChannelBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.entity.CircleCategory;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.global.Constants;
import io.agora.service.model.CategoryViewModel;

public class MoveChannelActivity extends BaseInitActivity<ActivityMoveChannelBinding> implements View.OnClickListener, BaseAdapter.ItemClickListener {

    private CategoryViewModel viewModel;
    private CategoryListAdapter adapter;
    private CircleChannel channel;
    private List<CircleCategoryData> categoryDatas = new ArrayList<>();

    public static void actionStart(Context context, CircleChannel channel) {
        Intent intent = new Intent(context, MoveChannelActivity.class);
        intent.putExtra(Constants.CHANNEL, (Serializable) channel);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_move_channel;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mBinding.recycleview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        viewModel.transferCategoryLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String newCategoryId) {
                    //刷新列表
                    setTargetIdChecked(newCategoryId);
                    adapter.refresh(categoryDatas);
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
        mBinding.ivBack.setOnClickListener(this);
    }

    private void setTargetIdChecked(String newCategoryId) {
        for (int i = 0; i < categoryDatas.size(); i++) {
            if(categoryDatas.get(i).categoryId.equals(newCategoryId)) {
                categoryDatas.get(i).checked=true;
            }else {
                categoryDatas.get(i).checked=false;
            }
        }
    }

    @Override
    protected void initData() {
        super.initData();

        channel = (CircleChannel) getIntent().getSerializableExtra(Constants.CHANNEL);
        List<CircleCategory> categories = DatabaseManager.getInstance().getCagegoryDao().getCategoriesByChannelServerId(channel.serverId);
        if (!CollectionUtils.isEmpty(categories)) {
            for (CircleCategory category : categories) {
                CircleCategoryData circleCategoryData = new CircleCategoryData(category.serverId, category.categoryId, category.categoryName);
                if(TextUtils.equals(circleCategoryData.categoryName,DEFAULT_CATEGORY_NAME)) {
                    circleCategoryData.categoryName=getString(R.string.circle_default_category_name_alias);
                }
                if (TextUtils.equals(category.categoryId, channel.categoryId)) {
                    circleCategoryData.checked = true;
                } else {
                    circleCategoryData.checked = false;
                }
                categoryDatas.add(circleCategoryData);
            }
        }
        adapter = new CategoryListAdapter(mContext, categoryDatas,R.layout.item_check_category);
        mBinding.recycleview.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        }
    }

    @Override
    public void onClick(View itemView, int positon) {
        CircleCategoryData categoryData = categoryDatas.get(positon);
        viewModel.transferCategory(categoryData.serverId,categoryData.categoryId,channel.channelId);
    }
}