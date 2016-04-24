package com.yizan.community.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.model.InitInfo;
import com.yizan.community.R;
import com.yizan.community.utils.AppUpdate;
import com.yizan.community.utils.PushUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

/**
 * 设置Activity
 * Created by ztl on 2015/9/22.
 */
public class SetUpActivity extends BaseActivity implements BaseActivity.TitleListener ,View.OnClickListener{

    private LinearLayout mFeedback,mAboutUs,mNewHelp,mVersionDetection;
    private TextView version;
    private InitInfo info;
    private Button msgSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setTitleListener(this);
        info = PreferenceUtils.getObject(SetUpActivity.this, InitInfo.class);
        initViews();
    }

    private void initViews() {
        msgSettings = mViewFinder.find(R.id.message_settings);
        msgSettings.setOnClickListener(this);
        if (PreferenceUtils.getValue(this,"push_open",true)){
            msgSettings.setBackgroundResource(R.drawable.btn_on);
        }else{
            msgSettings.setBackgroundResource(R.drawable.btn_off);
        }
        mFeedback = (LinearLayout)findViewById(R.id.feedback);
        mFeedback.setOnClickListener(this);
        mAboutUs = (LinearLayout)findViewById(R.id.about_us);
        mAboutUs.setOnClickListener(this);
        mNewHelp = (LinearLayout)findViewById(R.id.new_help);
        mNewHelp.setOnClickListener(this);
        mVersionDetection = (LinearLayout)findViewById(R.id.version_detection);
        mVersionDetection.setOnClickListener(this);
        version = mViewFinder.find(R.id.version_code);
        version.setText(getResources().getString(R.string.msg_vertion_hint)+info.appVersion);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.set_up);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.feedback:
                startActivity(new Intent(SetUpActivity.this,FeedbackActivity.class));
                break;
            case R.id.new_help:
                Intent intent1 = new Intent(SetUpActivity.this, WebViewActivity.class);
                intent1.putExtra(Constants.EXTRA_URL, info.helpUrl);
                startActivity(intent1);
                break;
            case R.id.about_us:
                Intent intent = new Intent(SetUpActivity.this, WebViewActivity.class);
                intent.putExtra(Constants.EXTRA_URL, info.aboutUrl);
                startActivity(intent);
                break;
            case R.id.version_detection:
                AppUpdate.checkUpdate(this, false);
                break;
            case R.id.message_settings:
                boolean isPushOpen = isPushOpen();
                PushUtils.isPush(this, !isPushOpen);
                initPushSwitchView(!isPushOpen);


                break;
        }
    }

    /**
     * push开关
     * @param bOpen
     */
    private void initPushSwitchView(boolean bOpen){
        PreferenceUtils.setValue(this, "push_open", bOpen);
        if (!bOpen){
            msgSettings.setBackgroundResource(R.drawable.btn_off);
        }else {
            msgSettings.setBackgroundResource(R.drawable.btn_on);
        }
    }

    private boolean isPushOpen(){
        return PreferenceUtils.getValue(this, "push_open", true);
    }


}
