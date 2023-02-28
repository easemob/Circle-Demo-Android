package io.agora.ground.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.zhouwei.library.CustomPopWindow;
import com.google.gson.Gson;
import com.hyphenate.chat.EMCircleServerSearchType;
import com.hyphenate.easeui.utils.ShowMode;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.ArrayList;
import java.util.List;

import io.agora.ground.R;
import io.agora.ground.adapter.GroundAdapter;
import io.agora.ground.callbacks.OnItemClickListener;
import io.agora.ground.databinding.ActivitySearchGroundBinding;
import io.agora.ground.model.GroundViewModel;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.managers.AppUserInfoManager;

public class SearchGroundActivity extends BaseInitActivity<ActivitySearchGroundBinding> implements TextWatcher, TextView.OnEditorActionListener, OnItemClickListener<CircleServer>, View.OnClickListener {
    private GroundViewModel mViewModel;
    private RecyclerView mRvResult;
    private GroundAdapter mAdapter;
    private EditText mEtSearch;
    private ImageView mIvClear;
    private ArrayList<CircleServer> servers;
    private CustomPopWindow mCustomPopWindow;
    private EMCircleServerSearchType searchType=EMCircleServerSearchType.EM_CIRCLE_SERVER_SEARCH_TYPE_NAME;

    public static void actionStart(Context context, ArrayList<CircleServer> servers) {
        Intent intent = new Intent(context, SearchGroundActivity.class);
        if (!CollectionUtils.isEmpty(servers)) {
            new Gson().toJson(servers);
            intent.putParcelableArrayListExtra("servers", servers);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_search_ground;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        servers = getIntent().getParcelableArrayListExtra("servers");
        mRvResult = findViewById(R.id.rv_result);
        mEtSearch = findViewById(R.id.et_search);
        mEtSearch.requestFocus();
        mIvClear = findViewById(R.id.iv_clear);
    }


    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(GroundViewModel.class);
        mViewModel.joinServerLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleServer>() {
                @Override
                public void onSuccess(@Nullable CircleServer data) {
                    if (data != null) {
                        //存入缓存
                        AppUserInfoManager.getInstance().getUserJoinedSevers().put(data.serverId, data);
                        ToastUtils.showShort(getString(io.agora.service.R.string.circle_join_in_server_success));
                        //发送广播
                        LiveEventBus.get(Constants.SERVER_CHANGED).post(data);
                    }
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
        mEtSearch.addTextChangedListener(this);
        mEtSearch.setOnEditorActionListener(this);
        mBinding.tvSearchType.setOnClickListener(this);
        mBinding.ivArrowDown.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRvResult.setLayoutManager(layoutManager);
        mAdapter = new GroundAdapter(false);
        mAdapter.setOnItemClickListener(this);
        mRvResult.setAdapter(mAdapter);
        mAdapter.setData(servers);
        mBinding.etSearch.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void getServerListByKey(String key, EMCircleServerSearchType searchType) {
        mViewModel.getServerListByKey(searchType,key).observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<CircleServer>>() {
                @Override
                public void onSuccess(@Nullable List<CircleServer> data) {
                    mAdapter.setData(data, key);
                    hideKeyboard();
                    if (CollectionUtils.isEmpty(data)) {
                        ToastUtils.showShort(getString(io.agora.service.R.string.circle_no_result));
                    }
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
    }

    public void onClear(View v) {
        mEtSearch.setText("");
        mAdapter.clearData();
    }

    public void onCancel(View v) {
        hideKeyboard();
        finish();
    }

    public void onSeach(View v) {
        doSearch();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String key = mEtSearch.getText().toString();
        if (key.length() > 0) {
            mIvClear.setVisibility(View.VISIBLE);
        } else {
            mIvClear.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            doSearch();
            return true;
        }
        return false;
    }

    private void doSearch() {
        String key = mEtSearch.getText().toString();
        if (TextUtils.isEmpty(key.trim())) {
            ToastUtils.showShort(getString(R.string.keyword_not_empty));
            return;
        }
        getServerListByKey(key,searchType);
    }

    @Override
    public void onItemClick(CircleServer circleServer) {
        if (circleServer == null) {
            return;
        }
        if (AppUserInfoManager.getInstance().getUserJoinedSevers().containsKey(circleServer.serverId)) {
            //跳转去首页展示详情
            //跳转到首页，显示社区详情
            Postcard postcard = ARouter.getInstance().build("/app/MainActivity");
            //直接跳转到首页,显示目标server详情
            postcard.withSerializable(Constants.SHOW_MODE, ShowMode.NORMAL);
            postcard.withInt(Constants.NAV_POSITION, 0);
            postcard.withParcelable(Constants.SERVER, circleServer);
            postcard.navigation();
            finish();
        } else {
            //弹框
            ServerIntroductionBottomFragment fragment = new ServerIntroductionBottomFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.SERVER, circleServer);
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager());
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_search_type || v.getId() == R.id.iv_arrow_down) {
            showPopWindow(mBinding.cslSearch);
        }
    }

    private void showPopWindow(View locationView) {

        View contentView = LayoutInflater.from(this).inflate(R.layout.search_server_pop, (ViewGroup) locationView.getParent(), false);
        //处理popWindow 显示内容
        handleLogic(contentView);

        //显示PopupWindow
        if (mCustomPopWindow == null) {
            mCustomPopWindow = new CustomPopWindow.PopupWindowBuilder(this)
                    .setView(contentView)
                    .size(ConvertUtils.dp2px(118), ConvertUtils.dp2px(106))
                    .setFocusable(true)
                    .setOutsideTouchable(true)
                    .create()
                    .showAsDropDown(locationView, ConvertUtils.dp2px(0), 0);
        } else {
            mCustomPopWindow.showAsDropDown(locationView, ConvertUtils.dp2px(0), 0);
        }
    }

    /**
     * 处理弹出显示内容、点击事件等逻辑
     *
     * @param contentView
     */
    private void handleLogic(View contentView) {
        TextView tvSearchName = contentView.findViewById(R.id.tv_search_name);
        TextView tvSearchTag = contentView.findViewById(R.id.tv_search_tag);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.tv_search_name) {
                    mBinding.tvSearchType.setText(getString(io.agora.service.R.string.circle_server_name));
                    searchType=EMCircleServerSearchType.EM_CIRCLE_SERVER_SEARCH_TYPE_NAME;
                    tvSearchName.setEnabled(false);
                    tvSearchTag.setEnabled(true);
                } else if (id == R.id.tv_search_tag) {
                    mBinding.tvSearchType.setText(getString(io.agora.service.R.string.circle_server_tag));
                    searchType=EMCircleServerSearchType.EM_CIRCLE_SERVER_SEARCH_TYPE_TAG;
                    tvSearchName.setEnabled(true);
                    tvSearchTag.setEnabled(false);
                }
                if (mCustomPopWindow != null) {
                    mCustomPopWindow.dissmiss();
                }
            }
        };
        if(searchType==EMCircleServerSearchType.EM_CIRCLE_SERVER_SEARCH_TYPE_NAME) {
            tvSearchName.setEnabled(false);
            tvSearchTag.setEnabled(true);
        }else{
            tvSearchName.setEnabled(true);
            tvSearchTag.setEnabled(false);
        }
        tvSearchName.setOnClickListener(listener);
        tvSearchTag.setOnClickListener(listener);
    }

}
