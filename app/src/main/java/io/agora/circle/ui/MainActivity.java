package io.agora.circle.ui;

import static io.agora.service.global.Constants.CHANNEL;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.navigation.NavigationBarView;
import com.hyphenate.easeui.utils.ShowMode;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.agora.circle.R;
import io.agora.circle.adapter.MainViewPagerAdapter;
import io.agora.circle.databinding.ActivityMainBinding;
import io.agora.circle.model.MainViewModel;
import io.agora.common.base.BaseFragment;
import io.agora.contacts.ui.ContactsFragment;
import io.agora.contacts.ui.InviteUserToServerBottomFragment;
import io.agora.contacts.ui.channel.ChannelSettingBottomFragment;
import io.agora.contacts.ui.channel.CreateChannelActivity;
import io.agora.contacts.ui.channel.VoiceChannelDetailBottomFrament;
import io.agora.contacts.ui.server.ServerSettingBottomFragment;
import io.agora.ground.ui.GroundFragment;
import io.agora.ground.ui.ServerIntroductionBottomFragment;
import io.agora.home.ui.HomeFragment;
import io.agora.mine.ui.MineFragment;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.bean.CircleCategoryData;
import io.agora.service.bean.GetRTCTokenBean;
import io.agora.service.callbacks.CircleRTCListener;
import io.agora.service.callbacks.CircleRTCTokenCallback;
import io.agora.service.callbacks.OnResourceParseCallback;
import io.agora.service.db.DatabaseManager;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.db.entity.CircleServer;
import io.agora.service.global.Constants;
import io.agora.service.global.GlobalEventMonitor;
import io.agora.service.managers.AppUserInfoManager;
import io.agora.service.managers.CircleRTCManager;

@Route(path = "/app/MainActivity")
public class MainActivity extends BaseInitActivity<ActivityMainBinding> implements CircleRTCListener {
    private List<BaseFragment> fragments = new ArrayList<>();
    private BaseFragment homeFragment, groundFragment, contactsFragment, mineFragment;
    private BaseFragment mCurrentFragment;
    private int[] badgeIds = {R.layout.badge_contacts};
    private int[] msgIds = {R.id.tv_main_contacts_msg};
    private TextView mTvMainContactsMsg;
    private int count = 1;
    private RxPermissions rxPermissions;
    private MainViewModel viewModel;
    private CircleRTCTokenCallback circleRTCTokenCallback;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        checkIfShowSavedFragment(savedInstanceState);
    }


    private void checkIfShowSavedFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String tag = savedInstanceState.getString("tag");
            if (!TextUtils.isEmpty(tag)) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment instanceof HomeFragment) {
                    homeFragment = (BaseFragment) fragment;
                } else if (fragment instanceof GroundFragment) {
                    groundFragment = (BaseFragment) fragment;
                } else if (fragment instanceof ContactsFragment) {
                    contactsFragment = (BaseFragment) fragment;
                } else if (fragment instanceof MineFragment) {
                    mineFragment = (BaseFragment) fragment;
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentFragment != null) {
            outState.putString("tag", mCurrentFragment.getTag());
        }
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        initPermission();
        initFragments();
        initListener();

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getRTCTokenLiveData.observeForever( response -> {
            parseResource(response, new OnResourceParseCallback<GetRTCTokenBean>() {
                @Override
                public void onSuccess(@Nullable GetRTCTokenBean getRTCTokenBean) {
                    if(circleRTCTokenCallback!=null) {
                        if (getRTCTokenBean != null) {
                            circleRTCTokenCallback.setToken(getRTCTokenBean.getAccessToken(), Integer.parseInt(getRTCTokenBean.getAgoraUid()));
                        }
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                }
            });
        });
        CircleRTCManager.getInstance().init(getBaseContext())
                .registerCircleRTCListener(this);
    }

    private void initPermission() {
        rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    if (granted) {
                        // All requested permissions are granted
                    }
                });
    }

    private void initListener() {
        Map<Integer, Integer> map = new HashMap<>();
        Menu menu = mBinding.bnvMain.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            map.put(menu.getItem(i).getItemId(), i);
        }

        mBinding.vp2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mBinding.bnvMain.setSelectedItemId(mBinding.bnvMain.getMenu().getItem(position).getItemId());
                mCurrentFragment = fragments.get(position);
                if (mCurrentFragment instanceof HomeFragment) {
                    //发送通知恢复正常模式
                    LiveEventBus.get(Constants.HOME_CHANGE_MODE, CircleServer.class).post(null);
                }
            }
        });

        mBinding.bnvMain.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mBinding.vp2.setCurrentItem(map.get(item.getItemId()), false);
                return true;
            }
        });

        LiveEventBus.get(Constants.SHOW_SERVER_INVITE_FRAGMENT, CircleServer.class).observe(this, new Observer<CircleServer>() {
            @Override
            public void onChanged(CircleServer server) {
                InviteUserToServerBottomFragment fragment = new InviteUserToServerBottomFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.SERVER, server);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager());
            }
        });

        LiveEventBus.get(Constants.SHOW_SERVER_SETTING_FRAGMENT, CircleServer.class).observe(this, new Observer<CircleServer>() {
            @Override
            public void onChanged(CircleServer server) {
                ServerSettingBottomFragment fragment = new ServerSettingBottomFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("server", server);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager());
            }
        });
        LiveEventBus.get(Constants.SHOW_SERVER_INTRODUCTION_FRAGMENT, CircleServer.class).observe(this, new Observer<CircleServer>() {
            @Override
            public void onChanged(CircleServer server) {
                ServerIntroductionBottomFragment fragment = new ServerIntroductionBottomFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("server", server);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), ScreenUtils.getScreenHeight() - ConvertUtils.dp2px(1));
            }
        });
        LiveEventBus.get(Constants.SHOW_CREATE_CHANNEL_FRAGMENT, CircleCategoryData.class).observe(this, new Observer<CircleCategoryData>() {
            @Override
            public void onChanged(CircleCategoryData categoryData) {
                if(categoryData!=null) {
                    CreateChannelActivity.actionStart(MainActivity.this,categoryData.serverId,categoryData.categoryId);
                }
            }
        });
        LiveEventBus.get(Constants.SHOW_VOICE_CHANNEL_DETAIL_BOTTOM_FRAGMENT, FragmentManager.class).observe(this, new Observer<FragmentManager>() {
            @Override
            public void onChanged(FragmentManager fragmentManager) {
                String channelId = CircleRTCManager.getInstance().getChannelId();
                if(!TextUtils.isEmpty(channelId)) {
                    CircleChannel channel = DatabaseManager.getInstance().getChannelDao().getChannelByChannelID(channelId);
                    if(channel!=null) {
                        VoiceChannelDetailBottomFrament fragment = new VoiceChannelDetailBottomFrament();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.CHANNEL, channel);
                        fragment.setArguments(bundle);
                        fragment.show(fragmentManager, ScreenUtils.getScreenHeight() - ConvertUtils.dp2px(410));
                    }
                }
            }
        });
        LiveEventBus.get(Constants.SHOW_CHANNEL_SETTING_FRAGMENT, CircleChannel.class).observe(this, new Observer<CircleChannel>() {
            @Override
            public void onChanged(CircleChannel channel) {
                ChannelSettingBottomFragment fragment = new ChannelSettingBottomFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(CHANNEL, channel);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager());
            }
        });
        LiveEventBus.get(Constants.SHOW_VOICE_CHANNEL_DETAIL_BOTTOM_FRAGMENT, CircleChannel.class).observe(this, new Observer<CircleChannel>() {
            @Override
            public void onChanged(CircleChannel channel) {
                VoiceChannelDetailBottomFrament fragment = new VoiceChannelDetailBottomFrament();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.CHANNEL, channel);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), ScreenUtils.getScreenHeight() - ConvertUtils.dp2px(410));
            }
        });
        LiveEventBus.get(Constants.SHOW_RED_DOT, Boolean.class).observe(this, isShow -> {
            mTvMainContactsMsg.setVisibility(isShow ? View.VISIBLE : View.GONE);
        });
        GlobalEventMonitor.getInstance().init(this);
    }

    private void initFragments() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        if (groundFragment == null) {
            groundFragment = new GroundFragment();
        }
        if (contactsFragment == null) {
            contactsFragment = new ContactsFragment();
        }
        if (mineFragment == null) {
            mineFragment = new MineFragment();
        }
        fragments.add(homeFragment);
        fragments.add(groundFragment);
        fragments.add(contactsFragment);
        fragments.add(mineFragment);
    }

    @Override
    protected void initData() {
        super.initData();
        MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(this, fragments);
        mBinding.vp2.setAdapter(pagerAdapter);
        mBinding.vp2.setUserInputEnabled(false);
        mBinding.vp2.setCurrentItem(0, false);
        mBinding.vp2.setOffscreenPageLimit(fragments.size());

        addTabBadge();
    }


    private void addTabBadge() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) mBinding.bnvMain.getChildAt(0);
        BottomNavigationItemView itemTab;
        for (int i = 0; i < 3; i++) {
            itemTab = (BottomNavigationItemView) menuView.getChildAt(i);
            View badge = LayoutInflater.from(this).inflate(badgeIds[0], menuView, false);
            switch (i) {
                case 2:
                    mTvMainContactsMsg = badge.findViewById(msgIds[0]);
                    break;
            }
            itemTab.addView(badge);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ShowMode showMode = (ShowMode) intent.getSerializableExtra(Constants.SHOW_MODE);
        int positon = intent.getIntExtra(Constants.NAV_POSITION, 0);
        CircleServer server = intent.getParcelableExtra(Constants.SERVER);

        BaseFragment targetFragment = fragments.get(positon);
        mBinding.vp2.setCurrentItem(positon, false);
        if (targetFragment instanceof HomeFragment) {
            if (showMode == ShowMode.SERVER_PREVIEW) {
                ((HomeFragment) targetFragment).showServerPreview(server);
            } else if (showMode == ShowMode.NORMAL) {
                //发送通知,切换到目标server
                LiveEventBus.get(Constants.HOME_CHANGE_MODE, CircleServer.class).post(server);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (count % 2 == 0) {
                finish();
                return super.onKeyDown(keyCode, event);
            }
            ToastUtils.showShort(getString(R.string.circle_exit_hint));
            count++;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    count = 1;
                }
            }, 2000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onGenerateRTCToken(String channelName, CircleRTCTokenCallback callback) {
        this.circleRTCTokenCallback = callback;
        viewModel.getRTCToken(channelName, "0", AppUserInfoManager.getInstance().getCurrentUserName());
    }
}