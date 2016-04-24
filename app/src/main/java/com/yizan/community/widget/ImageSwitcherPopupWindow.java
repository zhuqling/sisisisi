package com.yizan.community.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.yizan.community.R;

public class ImageSwitcherPopupWindow extends PopupWindow {

    private View mMenuView;
    private boolean mIsShow=false;
    public ImageSwitcherPopupWindow(final Activity context, OnClickListener onclick, String tag){
        this(context, onclick,tag,false);
    }
    public ImageSwitcherPopupWindow(final Activity context, OnClickListener onclick, String tag,boolean mIsShow) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.image_switcher, null);
        this.mIsShow=mIsShow;
        mMenuView.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        if (!TextUtils.isEmpty(tag)) {
            mMenuView.findViewById(R.id.item2).setTag(tag);
            mMenuView.findViewById(R.id.item1).setTag(tag);
        }
        mMenuView.findViewById(R.id.item1).setOnClickListener(onclick);
        mMenuView.findViewById(R.id.item2).setOnClickListener(onclick);
        if (mIsShow){
            mMenuView.findViewById(R.id.item3).setOnClickListener(onclick);
            mMenuView.findViewById(R.id.item3).setVisibility(View.VISIBLE);
        }else {
            mMenuView.findViewById(R.id.item3).setVisibility(View.GONE);
        }
        this.setContentView(mMenuView);
        this.setWidth(LayoutParams.MATCH_PARENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.AnimBottom);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(dw);
        mMenuView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

    public void setMenuText(String menu1, String menu2, String menu3) {
        ((Button) mMenuView.findViewById(R.id.item1)).setText(menu1);
        ((Button) mMenuView.findViewById(R.id.item2)).setText(menu2);
        if (mIsShow) {
            ((Button) mMenuView.findViewById(R.id.item3)).setText(menu3);
        }

    }


    public void show(View view) {
        // 显示窗口
        showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
    }

}
