package com.yizan.community.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.yizan.community.R;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.platform.ZYStatConfig;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.util.ViewFinder;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-17
 * Time: 10:15
 * FIXME
 */
public class BaseActivity extends FragmentActivity {
    public ImageButton mTitleLeft;
    public TextView mTitle;
    public View mTitleRight, mTitleRight2;
    private TitleListener mTitleSetListener;
    protected ViewFinder mViewFinder;

    protected void onResume() {
        super.onResume();
        ZYStatConfig.onPageResume(this);
    }

    protected void onPause() {
        super.onPause();
        ZYStatConfig.onPagePause(this);
    }

    interface TitleListener {
        public void setTitle(TextView title, ImageButton left, View right);
    }

    public void setTitleListener(TitleListener listener) {
        setTitleListener(listener, R.layout.titlebar);
    }

    public void setTitleListener_RightImage(TitleListener listener) {
        setTitleListener_RightImage(listener, R.layout.titlebar);
    }

    public void setTitleListener(TitleListener listener, int titleLayoutRes) {
        setCustomTitle(titleLayoutRes);
        initTitle();
        mTitleSetListener = listener;
        mTitleSetListener.setTitle(mTitle, mTitleLeft, mTitleRight);
    }

    public void setTitleListener_RightImage(TitleListener listener, int titleLayoutRes) {
        setCustomTitle(titleLayoutRes);
        initTitle();
        mTitleSetListener = listener;
        mTitleRight2.setVisibility(View.VISIBLE);
        mTitleSetListener.setTitle(mTitle, mTitleLeft, mTitleRight2);
    }

    public void setCustomTitle(int titleRes) {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, titleRes);
    }

    public void initTitle() {
        mTitleLeft = (ImageButton) findViewById(R.id.title_left);
        mTitle = (TextView) findViewById(R.id.title);
        mTitleRight = findViewById(R.id.title_right);
        mTitleRight2 = findViewById(R.id.ib_right);
        if (mTitleLeft != null) {
            mTitleLeft.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finishActivity();
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        AppUtils.hideSoftInput(BaseActivity.this);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mViewFinder = new ViewFinder(this);
    }

    @Override
    protected void onDestroy() {
        mViewFinder = null;
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    public void finishActivity() {
        finish();
    }

    public View setViewText(int layoutId, String value) {
        TextView v = (TextView) findViewById(layoutId);
        v.setText(value);
        return v;
    }

    public View setViewText(int layoutId, Spanned value) {
        TextView v = (TextView) findViewById(layoutId);
        v.setText(value);
        return v;
    }

    public View setViewText(int layoutId, int res) {
        TextView v = (TextView) findViewById(layoutId);
        v.setText(res);
        return v;
    }

    public View setViewClickListener(int layoutId, View.OnClickListener listener) {
        View v = findViewById(layoutId);
        v.setOnClickListener(listener);
        return v;
    }

    public View setViewVisible(int layoutId, int visibility) {
        View v = findViewById(layoutId);
        v.setVisibility(visibility);
        return v;
    }

    public View setViewImage(int layoutId, String url, int defaultImage) {
        NetworkImageView v = (NetworkImageView) findViewById(layoutId);
        if (defaultImage > 0) {
            v.setDefaultImageResId(defaultImage);
            v.setErrorImageResId(defaultImage);
        }
        v.setImageUrl(url, RequestManager.getImageLoader());
        return v;
    }

    public View setViewImage(int layoutId, int imageId) {
        ImageView v = (ImageView) findViewById(layoutId);
        if (imageId > 0) {
            v.setImageResource(imageId);
        }
        return v;
    }

    protected void hideSoftInputView() {
        try {
            if (this.getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(this.getCurrentFocus()
                                        .getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if(mTitle != null){
            mTitle.setText(title);
        }
    }

    public FragmentActivity getActivity(){
        return this;
    }
}
