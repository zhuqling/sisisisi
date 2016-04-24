package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.yizan.community.R;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.event.LoginEvent;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ztl on 2015/9/17.
 */
public class LoginActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {

    private RelativeLayout mRelativeLoginNormal, mRelativeLoginQucikSms;
    private LinearLayout mLinearLoginNormal, mLinearLoginQuickSms;
    private Button mBtnNormalLogin, mBtnQuickSmsLogin;
    private EditText mEtLoginNormalMobile, mEtLoginNormalPassword, mEdtLoginSmsMobile, mEdtLoginSmsCode;
    private String mStrMobile, mStrPassword, mStrSmsMobile, mStrIndentifyCode;
    private TextView mTvGetIndentifyCode, mLoginNormalForgetPwd;
    private MyCount mc;
    private RelativeLayout mEyes;
    private boolean isChick = false;

    /**
     * 创建时初始化title以及视图
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitleListener(this);//
        initViews();
    }

    //初始化视图
    private void initViews() {
        mEyes = mViewFinder.find(R.id.pwd_eyes);
        mEyes.setOnClickListener(this);
        mRelativeLoginNormal = (RelativeLayout) findViewById(R.id.rl_login_normal);
        mRelativeLoginNormal.setBackgroundResource(R.color.theme_background);
        mRelativeLoginNormal.setOnClickListener(this);
        mRelativeLoginQucikSms = (RelativeLayout) findViewById(R.id.rl_login_quick_sms);
        mRelativeLoginQucikSms.setBackgroundResource(R.color.white);
        mRelativeLoginQucikSms.setOnClickListener(this);
        mLinearLoginNormal = (LinearLayout) findViewById(R.id.ll_login_normal_detail);
        mLinearLoginQuickSms = (LinearLayout) findViewById(R.id.ll_login_quick_sms_detail);
        mBtnNormalLogin = (Button) findViewById(R.id.btn_login_normal);
        mBtnNormalLogin.setOnClickListener(this);
        mBtnQuickSmsLogin = (Button) findViewById(R.id.btn_login_quick_sms);
        mBtnQuickSmsLogin.setOnClickListener(this);
        mEtLoginNormalMobile = (EditText) findViewById(R.id.edt_login_normal_mobile);
        mEtLoginNormalPassword = (EditText) findViewById(R.id.edt_login_normal_pwd);
        mTvGetIndentifyCode = (TextView) findViewById(R.id.tv_login_get_code);
        mTvGetIndentifyCode.setOnClickListener(this);
        mEdtLoginSmsMobile = (EditText) findViewById(R.id.edt_login_quick_sms_mobile);
        mEdtLoginSmsCode = (EditText) findViewById(R.id.edt_login_quick_sms_code);
        mLoginNormalForgetPwd = (TextView) findViewById(R.id.login_normal_forget_password);
        mLoginNormalForgetPwd.setOnClickListener(this);
//        test();
    }

    private void test() {
        mEtLoginNormalMobile.setText("13983104094");
        mEtLoginNormalPassword.setText("123456");
    }

    //设置title
    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.login);
        ((TextView) right).setText(R.string.regist);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(LoginActivity.this, RegistorActivity.class), RegistorActivity.REQUEST_CODE);
            }
        });
    }

    //点击事件响应
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_login_normal:
                mLinearLoginNormal.setVisibility(View.VISIBLE);
                mLinearLoginQuickSms.setVisibility(View.GONE);
                mRelativeLoginNormal.setBackgroundColor(getResources().getColor(R.color.theme_background));
                mRelativeLoginQucikSms.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            case R.id.rl_login_quick_sms:
                mLinearLoginNormal.setVisibility(View.GONE);
                mLinearLoginQuickSms.setVisibility(View.VISIBLE);
                mRelativeLoginNormal.setBackgroundColor(getResources().getColor(R.color.white));
                mRelativeLoginQucikSms.setBackgroundColor(getResources().getColor(R.color.theme_background));
                break;
            //以上是View更新
            case R.id.btn_login_normal:
                loginNormal();
                break;
            case R.id.btn_login_quick_sms:
                loginSms();
                break;
            case R.id.tv_login_get_code:
                getSecurityCode();
                break;
            case R.id.login_normal_forget_password:
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
                break;
            case R.id.pwd_eyes:
                if (isChick) {
                    mEtLoginNormalPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isChick = false;
                } else {
                    mEtLoginNormalPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isChick = true;
                }

                break;
        }
    }

    //得到验证码
    private void getSecurityCode() {
        AppUtils.hideSoftInput(LoginActivity.this);
        String mobileNumber = mEdtLoginSmsMobile.getText().toString();
        if (mobileNumber == null || mobileNumber.length() == 0) {
            ToastUtils.show(LoginActivity.this, R.string.label_empty_mobile);
            mEdtLoginSmsMobile.requestFocus();
            return;
        }
        if (!CheckUtils.isMobileNO(mobileNumber)) {
            ToastUtils.show(LoginActivity.this, R.string.label_legal_mobile);
            mEdtLoginSmsMobile.setSelection(mobileNumber.length());
            mEdtLoginSmsMobile.requestFocus();
            return;
        }
        mc = new MyCount(61000, 1000);//创建倒计时任务
        if (NetworkUtils.isNetworkAvaiable(LoginActivity.this)) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, LoginActivity.class.getName());
            //将手机号发送给服务器
            Map<String, String> map = new HashMap<String, String>();
            map.put("mobile", mobileNumber);
            ApiUtils.post(getApplicationContext(), URLConstants.INENTIFYING, map, BaseResult.class, new Response.Listener<BaseResult>() {

                @Override
                public void onResponse(BaseResult response) {
                    CustomDialogFragment.dismissDialog();
                    if (O2OUtils.checkResponse(getActivity(), response)) {
                        ToastUtils.show(LoginActivity.this, R.string.label_send_success);
                        mTvGetIndentifyCode.setEnabled(false);
                        mc.start();
                    }
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CustomDialogFragment.dismissDialog();
                }
            });
        } else {
            ToastUtils.show(LoginActivity.this, R.string.label_check_mobile);
        }
    }
    //短信登陆
    private void loginSms() {
        mStrSmsMobile = mEdtLoginSmsMobile.getText().toString();
        mStrIndentifyCode = mEdtLoginSmsCode.getText().toString();
        if (TextUtils.isEmpty(mStrSmsMobile)) {
            ToastUtils.show(LoginActivity.this, R.string.hint_mobile);
            mEdtLoginSmsMobile.requestFocus();
            return;
        }
        if (!CheckUtils.isMobileNO(mStrSmsMobile)) {
            ToastUtils.show(LoginActivity.this, R.string.label_legal_mobile);
            mEdtLoginSmsMobile.setSelection(mStrSmsMobile.length());
            mEdtLoginSmsMobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mStrIndentifyCode)) {
            ToastUtils.show(LoginActivity.this, R.string.label_identifying_code);
            mEdtLoginSmsCode.requestFocus();
            return;
        }
        //将手机号和验证码发送到服务器验证
        if (NetworkUtils.isNetworkAvaiable(LoginActivity.this)) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading_login, PhoneLoginActivity.class.getName());
            Map<String, String> map = new HashMap<String, String>(2);
            map.put("mobile", mStrSmsMobile);
            map.put("verifyCode", mStrIndentifyCode);
            ApiUtils.post(getApplicationContext(), URLConstants.LOGIN, map, UserResultInfo.class, new Response.Listener<UserResultInfo>() {
                @Override
                public void onResponse(UserResultInfo response) {
                    CustomDialogFragment.dismissDialog();
                    if (O2OUtils.checkResponse(LoginActivity.this, response)) {
                        ToastUtils.show(LoginActivity.this, R.string.label_loging_success);
                        O2OUtils.cacheUserData(LoginActivity.this, response);//将将用户信息缓存
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(intent);
                        setResult(RESULT_OK);
                        closeSelf();
                    }
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CustomDialogFragment.dismissDialog();
                    ToastUtils.show(LoginActivity.this, R.string.label_login_fail);
                }
            });
        } else {
            ToastUtils.show(LoginActivity.this, R.string.label_check_mobile);
        }
    }
    //普通登录
    private void loginNormal() {
        mStrMobile = mEtLoginNormalMobile.getText().toString();
        mStrPassword = mEtLoginNormalPassword.getText().toString();
        if (TextUtils.isEmpty(mStrMobile)) {
            ToastUtils.show(LoginActivity.this, R.string.hint_mobile);
            mEtLoginNormalMobile.requestFocus();
            return;
        }
        if (!CheckUtils.isMobileNO(mStrMobile)) {
            ToastUtils.show(LoginActivity.this, R.string.label_legal_mobile);
            mEtLoginNormalMobile.setSelection(mStrMobile.length());
            mEtLoginNormalMobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mStrPassword)) {
            ToastUtils.show(LoginActivity.this, R.string.hint_password);
            mEtLoginNormalPassword.requestFocus();
            return;
        }

        if (mStrPassword.length() < 6 || mStrPassword.length() > 20) {
            ToastUtils.show(LoginActivity.this, R.string.label_six_twenty_identify);
            mEtLoginNormalPassword.setSelection(mStrPassword.length());
            mEtLoginNormalPassword.requestFocus();
            return;
        }
        if (NetworkUtils.isNetworkAvaiable(LoginActivity.this)) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading_login, LoginActivity.class.getName());
            Map<String, String> map = new HashMap<String, String>(2);
            map.put("mobile", mStrMobile);
            map.put("pwd", mStrPassword);
            ApiUtils.post(getApplicationContext(), URLConstants.LOGIN, map, UserResultInfo.class, new Response.Listener<UserResultInfo>() {
                @Override
                public void onResponse(UserResultInfo response) {
                    CustomDialogFragment.dismissDialog();
                    if (!O2OUtils.checkResponse(LoginActivity.this, response)) {
                        return;
                    }
                    ToastUtils.show(LoginActivity.this, R.string.label_loging_success);
                    O2OUtils.cacheUserData(LoginActivity.this, response);
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
                    setResult(Activity.RESULT_OK);
                    closeSelf();

                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CustomDialogFragment.dismissDialog();
                    ToastUtils.show(LoginActivity.this, R.string.label_login_fail);
                }
            });
        } else {
            ToastUtils.show(LoginActivity.this, R.string.label_check_mobile);
        }
    }

    class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mTvGetIndentifyCode.setText(R.string.lable_get_identifying);
            mTvGetIndentifyCode.setEnabled(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String value = getResources().getString(R.string.afresh_send) + "(" + millisUntilFinished / 1000 + " )";
            mTvGetIndentifyCode.setText(value);
        }
    }

    private void closeSelf() {
        if (O2OUtils.isLogin(this)) {
            LoginEvent loginEvent = new LoginEvent(true);
            EventBus.getDefault().post(loginEvent);
        }
        finishActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RegistorActivity.REQUEST_CODE:
                if (O2OUtils.isLogin(this)) {
                    finishActivity();
                }
                break;
        }
    }
}
