package com.yizan.community.widget.doublemenu;

import android.view.View;

/**
 * Created by Administrator on 2015/10/30 0030.
 */
public abstract class BaseHolder<T> {

    private View mView;

    public abstract View initView();
    public abstract void refreshView(T info);

    public BaseHolder(){
        mView = initView();
        mView.setTag(this);
    }

    public View getView(){
        return mView;
    }
}
