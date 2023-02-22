package io.agora.service.base;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentManager;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.hyphenate.easeui.widget.EaseImageView;
import com.jeremyliao.liveeventbus.LiveEventBus;

import io.agora.common.base.BaseActivity;
import io.agora.common.dialog.AlertDialog;
import io.agora.service.R;
import io.agora.service.callbacks.CircleVoiceChannelStateListener;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.dao.CircleChannelDao;
import io.agora.service.db.dao.CircleServerDao;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.managers.CircleRTCManager;
import io.agora.service.net.Resource;
import io.agora.service.net.Status;
import io.agora.service.repo.ServiceReposity;

public abstract class BaseInitActivity<T extends ViewDataBinding> extends BaseActivity<T> implements
        CircleVoiceChannelStateListener, View.OnTouchListener {

    private AlertDialog alertDialog;
    private TextView tvMessage;
    private ConstraintLayout floatView;//左下部悬浮button
    private CircleChannelDao channeldao;
    private CircleServerDao serverDao;
    private EaseImageView floatViewBg;
    private EaseImageView floatViewSrc;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout decoreview = (FrameLayout) getWindow().getDecorView();
        floatView = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.layout_float_voice_channel, decoreview, false);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(24) - ConvertUtils.dp2px(48),
                ScreenUtils.getScreenHeight() - ConvertUtils.dp2px(150) - ConvertUtils.dp2px(48),
                0,
                0);
        floatView.setLayoutParams(layoutParams);
        decoreview.addView(floatView);
        floatView.setOnTouchListener(this);
        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //语聊频道 底部弹框
                LiveEventBus.get(Constants.SHOW_VOICE_CHANNEL_DETAIL_BOTTOM_FRAGMENT, FragmentManager.class).post(getSupportFragmentManager());
            }
        });
        floatViewBg = floatView.findViewById(R.id.iv_float_voice_channel_bg);
        floatViewSrc = floatView.findViewById(R.id.iv_float_voice_channel_src);
        channeldao = DatabaseManager.getInstance().getChannelDao();
        serverDao = DatabaseManager.getInstance().getServerDao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CircleRTCManager.getInstance().registerVoiceChannelStateListener(this);
        if (floatView != null) {
            if (CircleRTCManager.getInstance().getCurrentUid() != null) {
                floatView.setVisibility(View.VISIBLE);
            } else {
                floatView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        CircleRTCManager.getInstance().unRegisterVoiceChannelStateListener(this);
        super.onDestroy();
    }

    @Override
    public void onSelfMicOffAndSpeaking(String rtcChannelName) {
        if (floatView != null && channeldao != null && serverDao != null) {
            CircleChannel circleChannel = channeldao.getChannelByChannelID(rtcChannelName);
            if (circleChannel != null) {
                CircleServer circleServer = serverDao.getServerById(circleChannel.serverId);
                if(circleServer!=null) {
                    floatViewSrc.setBackgroundResource(R.drawable.circle_in_voice_channel_mic_off_no_speaking);
                    floatViewBg.setShapeType(EaseImageView.ShapeType.ROUND);
                    floatViewBg.setBorderWidth(ConvertUtils.dp2px(2));
                    floatViewBg.setBorderColor(ContextCompat.getColor(this, R.color.ease_color_green_14ff72));
                    int randomServerIcon = ServiceReposity.getRandomServerIcon(circleServer.serverId);
                    Glide.with(this).load(circleServer.icon).placeholder(randomServerIcon).into(floatViewBg);
                }
            }
        }
    }

    @Override
    public void onSelfMicOnAndSpeaking(String rtcChannelName) {
        if (floatView != null && channeldao != null && serverDao != null) {
            CircleChannel circleChannel = channeldao.getChannelByChannelID(rtcChannelName);
            if (circleChannel != null) {
                CircleServer circleServer = serverDao.getServerById(circleChannel.serverId);
                if(circleServer!=null) {
                    floatViewSrc.setBackgroundResource(R.drawable.circle_in_voice_channel_mic_on_no_speaking);
                    floatViewBg.setBorderWidth(ConvertUtils.dp2px(2));
                    floatViewBg.setShapeType(EaseImageView.ShapeType.ROUND);
                    floatViewBg.setBorderColor(ContextCompat.getColor(this, R.color.ease_color_green_14ff72));
                    int randomServerIcon = ServiceReposity.getRandomServerIcon(circleServer.serverId);
                    Glide.with(this).load(circleServer.icon).placeholder(randomServerIcon).into(floatViewBg);
                }
            }
        }
    }

    @Override
    public void onSelfMicOffAndNoSpeaking(String rtcChannelName) {
        if (floatView != null && channeldao != null && serverDao != null) {
            CircleChannel circleChannel = channeldao.getChannelByChannelID(rtcChannelName);
            if (circleChannel != null) {
                CircleServer circleServer = serverDao.getServerById(circleChannel.serverId);
                if(circleServer!=null) {
                    floatViewSrc.setBackgroundResource(R.drawable.circle_in_voice_channel_mic_off_no_speaking);
                    floatViewBg.setBorderWidth(ConvertUtils.dp2px(0));
                    int randomServerIcon = ServiceReposity.getRandomServerIcon(circleServer.serverId);
                    Glide.with(this).load(circleServer.icon).placeholder(randomServerIcon).into(floatViewBg);
                }
            }
        }
    }

    @Override
    public void onSelfMicOnAndNoSpeaking(String rtcChannelName) {
        if (floatView != null && channeldao != null && serverDao != null) {
            CircleChannel circleChannel = channeldao.getChannelByChannelID(rtcChannelName);
            if (circleChannel != null) {
                CircleServer circleServer = serverDao.getServerById(circleChannel.serverId);
                if(circleServer!=null) {
                    floatViewSrc.setBackgroundResource(R.drawable.circle_in_voice_channel_mic_on_no_speaking);
                    floatViewBg.setBorderWidth(ConvertUtils.dp2px(0));
                    int randomServerIcon = ServiceReposity.getRandomServerIcon(circleServer.serverId);
                    Glide.with(this).load(circleServer.icon).placeholder(randomServerIcon).into(floatViewBg);
                }
            }
        }
    }

    @Override
    public void onVoiceChannelStart(String rtcChannelName) {
        if (floatView != null) {
            floatView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVoiceChannelLeave() {
        if (floatView != null) {
            floatView.setVisibility(View.GONE);
        }
    }

    // 记录按下时的起始坐标
    private int startX;
    private int startY;
    private boolean isEnableClick = true;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 记录 每次触摸对象 的坐标
        int eventX = (int) event.getX();
        int eventY = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录按下的坐标
                startX = eventX;
                startY = eventY;
                isEnableClick = true;
                break;
            case MotionEvent.ACTION_MOVE:

                // 根据 移动后的坐标 和起始坐标计算 移动的距离
                int moveX = eventX - startX;
                int moveY = eventY - startY;

                if (Math.abs(moveX) > 10 || Math.abs(moveY) > 10) {
                    //响应滑动事件
                    isEnableClick = false;
                }

                // 获取imageView当前在 布局中的位置
                int left = v.getLeft();// 左边 X轴的起始位置
                int top = v.getTop();// 上顶边在Y轴的起始位置
                //不能超出屏幕   获取屏幕的上下左右 x0 ?  y0 ?
                if (top + moveY < ConvertUtils.dp2px(48) || top + moveY > ScreenUtils.getScreenHeight() - v.getHeight()) {
                    moveY = 0;
                }
                if (left + moveX < 0 || left + moveX > ScreenUtils.getScreenWidth() - v.getWidth()) {
                    moveX = 0;
                }
                // 重新给imageView设置位置
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                layoutParams.setMargins(left + moveX, top + moveY, 0, 0);
                v.setLayoutParams(layoutParams);
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return !isEnableClick;
    }

    public <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if (response == null) {
            return;
        }
        if (response.status == Status.SUCCESS) {
            callback.onHideLoading();
            callback.onSuccess(response.data);
        } else if (response.status == Status.ERROR) {
            callback.onHideLoading();
            if (!callback.hideErrorMsg) {
//                ToastUtils.showShort(response.getMessage(getApplicationContext()));
            }
            callback.onError(response.errorCode, response.getMessage(getApplicationContext()));
        } else if (response.status == Status.LOADING) {
            callback.onLoading(response.data);
        }
    }

    public void showLoading() {
        showLoading(getString(R.string.loading));
    }

    public void showLoading(String message) {
        showLoading(message, true);
    }

    public void showLoading(String message, boolean cancelable) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setContentView(R.layout.circle_dialog_progressbar)
                    .setCancelable(cancelable)
                    .setGravity(Gravity.CENTER)
                    .create();
        }
        if (tvMessage == null) {
            tvMessage = alertDialog.getViewById(R.id.tv_message);
        }
        if (!TextUtils.isEmpty(message)) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setText("");
            tvMessage.setVisibility(View.GONE);
        }
        alertDialog.show();
    }

    public void dismissLoading() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
