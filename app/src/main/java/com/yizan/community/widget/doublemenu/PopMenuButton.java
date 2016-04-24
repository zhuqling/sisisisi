package com.yizan.community.widget.doublemenu;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yizan.community.R;


/**
 * Created by Administrator on 2015/10/29 0029.
 */
public class PopMenuButton extends Button  implements View.OnClickListener {

    private Context mCtx;
    private PopupWindow mPopupWindow;
    private LinearLayout mPopupWindowView;
    private View mRootView;
    private View mShodowView;
    private LinearLayout mContentView;

    private int[] mLocation = new int[2];
    private int mStartY;
    private int mScreenHeight;

    private IPopMenu mIPopMenu;

    public PopMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        mRootView = this;
        //initPopWindow();
        //initView();
    }

    public PopMenuButton(Context context) {
        super(context);
        mCtx = context;
        mRootView = this;
        //initPopWindow();
        //initView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initPopWindow();
    }

    /**
     * 设置父布局
     * @param view
     */
//    public void setRootView(View rootView){
//        this.mRootView = rootView;
//    }

    public void setIPopMenu(IPopMenu listener){
        mIPopMenu = listener;
    }
    public void setContentView(View view){

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mContentView.addView(view, params);
    }

    private void initView(){
        mPopupWindowView = (LinearLayout) View.inflate(mCtx, R.layout.popmenu_layout, null);
        mContentView = (LinearLayout) mPopupWindowView.findViewById(R.id.rl_content);
        mShodowView = mPopupWindowView.findViewById(R.id.rl_shodow);

        mShodowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWindow();

            }
        });

        setOnClickListener(this);
    }

    private void initPopWindow(){
        if(isInEditMode()){
            return;
        }
        initView();
        //获取按键的位置
        mRootView.getLocationOnScreen(mLocation);
        mStartY = mLocation[1] + mRootView.getHeight();
        mScreenHeight = getScreenHeight((Activity)mCtx);

//        int[] location = new int[2];
//        this.getLocationOnScreen(location);
//        //y轴起始位置
//        int start = location[1]+this.getHeight()+1;
//        int screenHeight = ((Activity)mCtx).getWindowManager().getDefaultDisplay().getHeight();
//
//        //设置弹框的大小
//        mPopupWindow = new PopupWindow(mPopupWindowView,  ViewGroup.LayoutParams.MATCH_PARENT,500,false);
//        // mPopupWindow = new PopupWindow(mPopupWindowView,  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,false);
//        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));
//        //mPopupWindow.setAnimationStyle(R.style.AnimationFade);
//        mPopupWindow.setAnimationStyle(R.style.popupAnimation);
//        mPopupWindow.setFocusable(true);
//        mPopupWindow.setOutsideTouchable(true);
    }

    @Override
    public void onClick(View v) {

        if(mPopupWindow == null) {
            //initPopWindow();

            int[] location = new int[2];
            this.getLocationOnScreen(location);
            //y轴起始位置
            int start = location[1] + this.getHeight() + 1;
            //测量屏幕的高度
            int screenHeight = ((Activity) mCtx).getWindowManager().getDefaultDisplay().getHeight();

            //设置弹框的大小  弹框位置在按钮以下，占据所有屏幕
            mPopupWindow = new PopupWindow(mPopupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, screenHeight - start, false);
            // mPopupWindow = new PopupWindow(mPopupWindowView,  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,false);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x30000000));
            //mPopupWindow.setAnimationStyle(R.style.AnimationFade);
            mPopupWindow.setAnimationStyle(R.style.popupAnimation);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    showPopWindow(false);
                }
            });
        }

        if (mPopupWindow.isShowing()) {
            closeWindow();
        } else {

            int[] location1 = new int[2];
            this.getLocationOnScreen(location1);
            //设置弹框的位置
            mPopupWindow.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0,  location1[1]+this.getHeight()+1);
            showPopWindow(true);
        }
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    public int getScreenHeight(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getHeight();
    }

    /**
     * 获取状态栏高度
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void closeWindow(){
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();

        }
    }

    protected void showPopWindow(boolean bShow){
        if(bShow){
            if(mIPopMenu != null){
                mIPopMenu.showPopMenu(true);
            }
        }else{
            if(mIPopMenu != null){
                mIPopMenu.showPopMenu(false);
            }
        }
    }

    public interface IPopMenu {
        void showPopMenu(boolean bShow);
    }
}
