package io.agora.contacts.adapter;

import android.content.Context;
import android.widget.RadioButton;

import java.util.List;

import io.agora.common.base.BaseAdapter;
import io.agora.contacts.R;
import io.agora.contacts.bean.CircleCategoryData;


public class CategoryListAdapter extends BaseAdapter<CircleCategoryData> {

    public CategoryListAdapter(Context context, int layoutId) {
        super(context, layoutId);
    }

    public CategoryListAdapter(Context context, List<CircleCategoryData> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    @Override
    public void convert(ViewHolder holder, List<CircleCategoryData> datas, int position) {
        RadioButton rbCategory = holder.getView(R.id.rb_category);
        CircleCategoryData categoryData = datas.get(position);
        if(rbCategory!=null&&categoryData!=null) {
            rbCategory.setChecked(categoryData.checked);
            rbCategory.setText(categoryData.categoryName);
        }
    }
}
