package io.agora.contacts.ui.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;

import io.agora.contacts.R;
import io.agora.contacts.databinding.ActivityServerMembersBinding;
import io.agora.service.base.BaseInitActivity;
import io.agora.service.db.entity.CircleServer;

public class ServerMembersActivity extends BaseInitActivity<ActivityServerMembersBinding> {

    public static void actionStart(Context context, CircleServer server) {
        Intent intent = new Intent(context, ServerMembersActivity.class);
        intent.putExtra("server", (Serializable) server);
        context.startActivity(intent);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_server_members;
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
        CircleServer server = (CircleServer) getIntent().getSerializableExtra("server");
        ServerMembersFragment fragment = new ServerMembersFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("server", server);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_server_members, fragment, "server_members").commit();

    }
}