package com.yizan.community.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.zongyou.library.util.storage.PreferenceUtils;

public class PayResultActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {
    private boolean mPayOk = false;
    private String mSn;
    private MyCount mc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_result);
        mPayOk = this.getIntent().getBooleanExtra(Constants.EXTRA_PAY, false);
        mSn = this.getIntent().getStringExtra(Constants.EXTRA_DATA);

        setTitleListener(this);
        initView();
    }

    private void initView() {
        UserInfo userInfo = PreferenceUtils.getObject(this, UserInfo.class);
        mViewFinder.setText(R.id.tv_order, userInfo.name + getResources().getString(R.string.msg_order_hint) + mSn);
        Button btn = mViewFinder.find(R.id.btn_ok);
        btn.setOnClickListener(this);
        if (mPayOk) {
            mViewFinder.setDrawable(R.id.iv_image, R.drawable.ic_pay_ok);
            mViewFinder.setText(R.id.tv_pay_status, R.string.msg_pay_succeed_hint);
            btn.setText(R.string.msg_go_stroll);
        } else {
            mViewFinder.setDrawable(R.id.iv_image, R.drawable.ic_pay_err);
            mViewFinder.setText(R.id.tv_pay_status, R.string.msg_pay_error);
            btn.setText(R.string.msg_error_retry);
        }
        mc = new MyCount(11000, 1000);
        mc.start();
    }


    @Override
    public void setTitle(TextView title, ImageButton left, View right) {

        if (mPayOk) {
            title.setText(R.string.msg_pay_success);
        } else {
            title.setText(R.string.msg_pay_error);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (mPayOk) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mc.cancel();
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {

            finish();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            TextView tv = mViewFinder.find(R.id.tv_count_down);
            tv.setText(String.valueOf(millisUntilFinished / 1000));
        }
    }
}
