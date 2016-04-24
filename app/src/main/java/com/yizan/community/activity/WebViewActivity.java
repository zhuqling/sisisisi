package com.yizan.community.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.comm.Constants;
import com.zongyou.library.util.storage.PreferenceUtils;

/**
 * 通用加载webviewActivity
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-17
 * Time: 11:11
 */
public class WebViewActivity extends BaseActivity implements BaseActivity.TitleListener {

    private WebView mWebView;
    private TextView mTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        setTitleListener(this);
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mTitleTextView.setText(title);
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
        });
        String url = getIntent().getStringExtra(Constants.EXTRA_URL);

        if (null != url) {
            String[] us = url.split("\\?");
            if (us.length == 1) {
                url += "?";
            } else
                url += "&";
            url += "agent=m&token="+ PreferenceUtils.getValue(WebViewActivity.this,Constants.LOGIN_TOKEN,"");
        }
        mWebView.loadUrl(url);
        initViewTitle();
    }


    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        mTitleTextView = title;
        initViewTitle();
    }

    private void initViewTitle() {
        if (mTitleTextView == null) {
            return;
        }
        String title = this.getIntent().getStringExtra(Constants.EXTRA_TITLE);
        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        }
    }

//    private ProgressWebView mWebView;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_webview);
//        setTitleListener(this);
//        mWebView = (ProgressWebView) findViewById(R.id.webview);
//        WebSettings webSettings = mWebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        mWebView.setOnTitleChange(new com.zongyou.library.widget.ProgressWebView.OnTitleChange() {
//
//            @Override
//            public void updateTitle(String title) {
//                mTitleTextView.setText(title);
//            }
//        });
//        mWebView.setWebViewClient(new WebViewClient(){
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }});
//        String url = getIntent().getStringExtra(Constants.EXTRA_URL);
//        if (null != url) {
//            String[] us = url.split("\\?");
//            if (us.length == 1) {
//                url += "?";
//            } else
//                url += "&";
//            url += "agent=m";
//        }
//        mWebView.loadUrl(url);
//        initViewTitle();
//    }
//
//    private TextView mTitleTextView;
//
//    @Override
//    public void setTitle(TextView title, ImageButton left, View right) {
//        mTitleTextView = title;
//        initViewTitle();
//    }
//
//    private void initViewTitle() {
//        if(mTitleTextView == null){
//            return;
//        }
//        String title = this.getIntent().getStringExtra(Constants.EXTRA_TITLE);
//        if(!TextUtils.isEmpty(title)){
//            mTitleTextView.setText(title);
//        }
//    }

}
