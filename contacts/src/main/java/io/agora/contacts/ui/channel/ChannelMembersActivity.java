package io.agora.contacts.ui.channel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityChannelMembersBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.db.entity.CircleChannel;
import io.agora.service.global.Constants;

public class ChannelMembersActivity extends BaseInitActivity<ActivityChannelMembersBinding> {

    public static void actionStart(Context context, CircleChannel channel) {
        Intent intent = new Intent(context, ChannelMembersActivity.class);
        intent.putExtra(Constants.CHANNEL, (Serializable) channel);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_channel_members;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mBinding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        CircleChannel channel = (CircleChannel) getIntent().getSerializableExtra(Constants.CHANNEL);
        ChannelMembersFragment fragment = new ChannelMembersFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("channel", channel);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_channel_members, fragment, "channel_members").commit();

    }
}