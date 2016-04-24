package com.yizan.community.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.UserInfo;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.Date;

public class WebMessageActivity extends BaseActivity implements BaseActivity.TitleListener {
    private WebView mWebView;
    private Button mRightButton;
    public static final int REQUEST_CODE = 0x401;
    private boolean mIsLoadOK = false;
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_range);
        setContentView(R.layout.activity_webview);
        setTitleListener(this);
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

        };
        // 设置setWebChromeClient对象
        mWebView.setWebChromeClient(wvcc);


        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CustomDialogFragment.dismissDialog();
                mIsLoadOK = true;
            }
        });
        UserInfo info = PreferenceUtils.getObject(this,
                UserInfo.class);
        String token = PreferenceUtils.getValue(this, Constants.LOGIN_TOKEN, "");
        String url = String.format(URLConstants.SYS_MESSAGE, token, info.id);

        if (null != url) {
            String[] us = url.split("\\?");
            if (us.length == 1) {
                url += "?";
            } else
                url += "&";
            url += "agent=m";
            url += "&tx=";
            url += (new Date()).getTime();
        }
        mJsToJava = new JsToJava(this);
        mWebView.addJavascriptInterface(mJsToJava, "stub");

        CustomDialogFragment.show(getSupportFragmentManager(), R.string.loading, this.getClass().getName());
        mWebView.loadUrl(url);


    }
    private JsToJava mJsToJava;

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(getResources().getString(R.string.service_msg_title));
        if(left != null){
            left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goBack();
                }
            });
        }
    }

    protected void openOrderDetail(int orderId){
        try {
            if(orderId <= 0){
                return;
            }
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra(Constants.EXTRA_DATA, orderId);
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class JsToJava
    {
        private WebMessageActivity mContext;
        public JsToJava(WebMessageActivity context){
            mContext = context;
        }

        @JavascriptInterface
        public void callmsgnative(int orderId)
        {
            openOrderDetail(orderId);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) ) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected boolean goBack(){
        if (null != mWebView && mWebView.canGoBack()) {
            mWebView.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        finishActivity();
        return true;
    }
}
