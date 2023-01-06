package io.agora.service.widget;

import android.view.View;
import android.view.ViewGroup;


public abstract class FlowlayoutAdapter {
    public abstract int getCount();

    public abstract View getView(int position, ViewGroup parent);
}
