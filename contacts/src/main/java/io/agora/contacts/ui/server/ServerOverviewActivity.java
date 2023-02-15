package io.agora.contacts.ui.server;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.utils.TextUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.Serializable;
import java.util.List;

import io.agora.common.dialog.AlertDialog;
import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityServerOverviewBinding;
import io.agora.contacts.widget.TagView;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.model.ServerViewModel;

public class ServerOverviewActivity extends BaseInitActivity<ActivityServerOverviewBinding> implements View.OnClickListener {
    //输入框初始值
    private int namePrimaryNum = 0;
    //输入框最大值
    public int mMaxNameNum = 16;
    //输入框初始值
    private int descPrimaryNum = 0;
    //输入框最大值
    public int mMaxDescNum = 120;
    private ServerViewModel mViewModel;

    private AlertDialog alertDialog;
    private EditText edtTag;
    private CircleServer server;

    public static void actionStart(Context context, CircleServer server) {
        Intent intent = new Intent(context, ServerOverviewActivity.class);
        intent.putExtra(Constants.SERVER, (Serializable) server);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_server_overview;
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        mViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
        mViewModel.updateServerLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleServer>() {
                @Override
                public void onSuccess(@Nullable CircleServer circleServer) {
                    //隐藏进度条
                    dismissLoading();
                    ToastUtils.showShort(getString(io.agora.service.R.string.home_update_server_success));
                    finish();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    dismissLoading();
                    if (!TextUtils.isEmpty(message)) {
                        ToastUtils.showShort(message);
                    }
                    finish();
                }
            });
        });
        mViewModel.addServerTagsLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<CircleServer.Tag>>() {
                @Override
                public void onSuccess(@Nullable List<CircleServer.Tag> allTags) {
                    //插入容器
                    insertContainer(allTags);
                    if (alertDialog != null) {
                        alertDialog.dismiss();
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
        mViewModel.removeServerTagLiveData.observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<CircleServer>() {
                @Override
                public void onSuccess(@Nullable CircleServer circleServer) {
                    server = circleServer;
                    //插入容器
                    insertContainer(circleServer.tags);
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
        mBinding.setVm(mViewModel);
        initListener();
    }

    private void initListener() {

        mBinding.edtServerName.addTextChangedListener(new TextWatcher() {
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
                selectionStart = mBinding.edtServerName.getSelectionStart();
                selectionEnd = mBinding.edtServerName.getSelectionEnd();
                //判断大于最大值
                if (wordNum.length() > mMaxNameNum) {
                    //删除多余输入的字（不会显示出来）
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    mBinding.edtServerName.setText(s);
                    mBinding.edtServerName.setSelection(tempSelection);//设置光标在最后
                }
                checkCreateServerButtonStatus();
            }
        });
        mBinding.edtServerDesc.addTextChangedListener(new TextWatcher() {
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
                selectionStart = mBinding.edtServerDesc.getSelectionStart();
                selectionEnd = mBinding.edtServerDesc.getSelectionEnd();
                //判断大于最大值
                if (wordNum.length() > mMaxDescNum) {
                    //删除多余输入的字（不会显示出来）
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    mBinding.edtServerDesc.setText(s);
                    mBinding.edtServerDesc.setSelection(tempSelection);//设置光标在最后
                }
                checkCreateServerButtonStatus();
            }
        });
        mBinding.ivAddTag.setOnClickListener(this);
        mBinding.tvSave.setOnClickListener(this);
        mBinding.ivBack.setOnClickListener(this);
    }

    private void checkCreateServerButtonStatus() {
        String name = mBinding.edtServerName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            mBinding.tvSave.setEnabled(false);
        } else {
            mBinding.tvSave.setEnabled(true);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        server = (CircleServer) getIntent().getSerializableExtra(Constants.SERVER);
        if (server != null) {
            String name = server.name;
            if (name != null) {
                mViewModel.serverName.set(name);
            }
            String desc = server.desc;
            if (desc != null) {
                mBinding.edtServerDesc.setText(desc);
            }

            List<CircleServer.Tag> tags = server.tags;
            insertContainer(tags);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.tv_save) {
            String name = mBinding.edtServerName.getText().toString().trim();
            String desc = mBinding.edtServerDesc.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastUtils.showShort(getString(io.agora.service.R.string.circle_category_name_is_null));
                return;
            }
            showLoading(null);
            mViewModel.updateServer(server, server.icon, name, desc);
        } else if (v.getId() == R.id.iv_add_tag) {
            int childCount = mBinding.llContainerTags.getChildCount();
            if (childCount >= 10) {
                ToastUtils.showShort(getString(io.agora.service.R.string.circle_tags_to_max, "10"));
                return;
            }
            //添加标签弹框
            alertDialog = new AlertDialog.Builder(this)
                    .setContentView(R.layout.dialog_add_tag)
                    .setLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setOnClickListener(R.id.tv_cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    })
                    .setOnClickListener(R.id.tv_confirm, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tag = edtTag.getText().toString().trim();
                            if(TextUtils.isEmpty(tag)) {
                                ToastUtils.showShort(getString(R.string.circle_tag_is_empty));
                            }else{
                                if((!checkTagExist(tag))) {
                                    if ( server != null) {
                                        //更新到服务器
                                        mViewModel.addTagToServer(server, tag);
                                    }
                                }else{
                                    ToastUtils.showShort(getString(R.string.circle_tag_exist));
                                }
                            }
                        }
                    })
                    .show();

            edtTag = alertDialog.getViewById(R.id.edt_tag);
            TextView tvCount = alertDialog.getViewById(R.id.tv_tag_count);

            edtTag.setFocusable(true);
            edtTag.setFocusableInTouchMode(true);
            edtTag.requestFocus();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            edtTag.addTextChangedListener(new TextWatcher() {
                //记录输入的字数
                private CharSequence wordNum;
                private int selectionStart;
                private int selectionEnd;
                private int tagPrimaryNum;

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
                    int number = tagPrimaryNum + s.length();
                    //TextView显示剩余字数
                    tvCount.setText("" + number + "/16");
                    selectionStart = edtTag.getSelectionStart();
                    selectionEnd = edtTag.getSelectionEnd();
                    //判断大于最大值
                    if (wordNum.length() > mMaxNameNum) {
                        //删除多余输入的字（不会显示出来）
                        s.delete(selectionStart - 1, selectionEnd);
                        int tempSelection = selectionEnd;
                        edtTag.setText(s);
                        edtTag.setSelection(tempSelection);//设置光标在最后
                    }
                }
            });
        }
    }

    private boolean checkTagExist(String tag) {
        if (server != null&&server.tags!=null) {
            for (int i = 0; i < server.tags.size(); i++) {
                if (android.text.TextUtils.equals(tag, server.tags.get(i).tag_name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void insertContainer(List<CircleServer.Tag> tags) {
        mBinding.llContainerTags.removeAllViews();
        if (CollectionUtils.isEmpty(tags)) {
            return;
        }
        for (CircleServer.Tag tag : tags) {
            TagView tagView = new TagView(mContext);
            tagView.setTagData(tag);
            mBinding.llContainerTags.addView(tagView);
            tagView.setonDeleteClickListener(new TagView.OnDeleteClickListener() {
                @Override
                public void onClick(View view) {
                    mViewModel.removeTagFromServer(server, ((TagView) view).getData());
                }
            });
        }
        mBinding.tvTagsCount.setText(mBinding.llContainerTags.getChildCount() + "/10");
    }

}