package io.agora.home.adapter;


import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;

import java.util.List;

import io.agora.common.base.BaseAdapter;
import io.agora.home.R;
import io.agora.home.bean.Node;

public class ChannelListAdapter extends BaseAdapter<Node> {


    public ChannelListAdapter(Context context, List<Node> datas, MuitiTypeSupport muitiTypeSupport) {
        super(context, datas, muitiTypeSupport);
    }

    @Override
    public void convert(ViewHolder holder, List<Node> datas, int position) {
        int level = datas.get(position).getLevel();
        Node node = datas.get(position);
        switch (level) {
            case 0:
                if(!node.getName().equals("Default Channel Category")) {
                    processCategoryItem(holder, node, position);
                }
                break;
            case 1:
                processChannelItem(holder, node, position);
                break;
            case 2:
                processThreadHeadView(holder, node, position);
                break;
            case 3:
                processThreadItem(holder, node, position);
                break;
        }
    }

    private void processCategoryItem(ViewHolder holder, Node node, int position) {
        LinearLayout llChannelType = holder.getView(R.id.ll_channel_type);
//        if(node.getName().equals("Default Channel Category")) {
//            llChannelType.setVisibility(View.GONE);
//            return;
//        }else{
//            llChannelType.setVisibility(View.VISIBLE);
//        }

        TextView tvCategoryName = holder.getView(R.id.tv_category_name);
        ImageView ivArrow=holder.getView(R.id.iv_category_list);
        ImageView ivAddChannel=holder.getView(R.id.iv_add_channel);
        if (node != null) {
            tvCategoryName.setText(node.getName());
        } else {
            tvCategoryName.setText("");
        }
        //设置图标
        if(!node.isExpand()) {
            ivArrow.setImageResource(io.agora.service.R.drawable.circle_arrow_right_gray);
        }else{
            ivArrow.setImageResource(io.agora.service.R.drawable.circle_arrow_up);
        }
        ivAddChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener!=null) {
                    clickListener.onClick(v,position);
                }
            }
        });
    }

    private void processThreadItem(ViewHolder holder, Node node, int position) {
        TextView tvThreadName = holder.getView(R.id.tv_thread_name);
        if (node != null) {
            tvThreadName.setText(node.getName());
        } else {
            tvThreadName.setText("");
        }
    }

    private void processThreadHeadView(ViewHolder holder, Node node, int position) {
        ImageView ivThreadHeadIcon = holder.getView(R.id.iv_thread_head_icon);
        if (node != null) {
            try {
                ivThreadHeadIcon.setImageResource(node.getIcon());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processChannelItem(ViewHolder holder, Node node, int position) {
        TextView tvChannelName = holder.getView(R.id.tv_channel_name);
        ImageView ivChannelIcon = holder.getView(R.id.iv_channel_icon);
        Glide.with(mContext).load(node.getIcon()).placeholder(R.drawable.circle_channel_public_icon).into(ivChannelIcon);
        TextView tvUnread = holder.getView(R.id.tv_unread);
        if (node != null) {
            tvChannelName.setText(node.getName());
        } else {
            tvChannelName.setText("");
        }
        int unreadMsgCount = 0;
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(node.getId());
        if (conversation != null) {
            unreadMsgCount = conversation.getUnreadMsgCount();
        }
        if (unreadMsgCount > 0) {
            tvUnread.setText(unreadMsgCount + "");
            tvUnread.setVisibility(View.VISIBLE);
        } else {
            tvUnread.setVisibility(View.GONE);
        }

    }
}
