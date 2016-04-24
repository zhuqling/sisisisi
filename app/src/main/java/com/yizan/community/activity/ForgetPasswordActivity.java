package com.yizan.community.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.yizan.community.R;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ztl on 2015/9/18.
 */
public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener,BaseActivity.TitleListener {

    public static final int REQUEST_CODE = 0x102;
    private EditText mEtMobileNumber, mEtIdentifyNumber, mEtInputPassword;
    private Button mBtnChangePwd;
    private TextView mTvGetIndentifyCode;
    private String identifyNumber, mobileNumber, password;
    private MyCount mc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        setTitleListener(this);

        initViews();
    }

    private void initViews() {
        mEtMobileNumber = (EditText)findViewById(R.id.fg_login_mobile);

        mEtIdentifyNumber = (EditText)findViewById(R.id.fg_login_code_edit);

        mEtInputPassword = (EditText)findViewById(R.id.fg_new_pwd);

        mBtnChangePwd = (Button)findViewById(R.id.fg_sure);
        mBtnChangePwd.setOnClickListener(this);

        mTvGetIndentifyCode = (TextView)findViewById(R.id.fg_login_get_code);
        mTvGetIndentifyCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fg_sure:
                sendNewPassword();
                break;
            case R.id.fg_login_get_code:
                getSecurityCode();
                break;
        }
    }

    private void getSecurityCode(){
        AppUtils.hideSoftInput(ForgetPasswordActivity.this);
        mobileNumber = mEtMobileNumber.getText().toString();
        identifyNumber = mEtIdentifyNumber.getText().toString();
        if (mobileNumber == null || mobileNumber.length() == 0) {
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_empty_mobile);
            mEtMobileNumber.requestFocus();
            return;
        }
        if (!CheckUtils.isMobileNO(mobileNumber)) {
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_legal_mobile);
            mEtMobileNumber.setSelection(mobileNumber.length());
            mEtMobileNumber.requestFocus();
            return;
        }
        mc = new MyCount(61000, 1000);
        // mc.start();
        if (NetworkUtils.isNetworkAvaiable(ForgetPasswordActivity.this)) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, ForgetPasswordActivity.class.getName());
            Map<String, String> map = new HashMap<String, String>();
            map.put("mobile", mobileNumber);
            ApiUtils.post(getApplicationContext(), URLConstants.INENTIFYING, map, BaseResult.class, new Response.Listener<BaseResult>() {

                @Override
                public void onResponse(BaseResult response) {
                    CustomDialogFragment.dismissDialog();
                    if(O2OUtils.checkResponse(getActivity(), response)) {
                        ToastUtils.show(ForgetPasswordActivity.this, R.string.label_send_success);
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
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_check_mobile);
        }
    }

    private void sendNewPassword(){
        mobileNumber = mEtMobileNumber.getText().toString();
        identifyNumber = mEtIdentifyNumber.getText().toString();
        password = mEtInputPassword.getText().toString();
        if (!CheckUtils.isMobileNO(mobileNumber)) {
            if (!TextUtils.isEmpty(mobileNumber)) {
                ToastUtils.show(ForgetPasswordActivity.this, R.string.label_legal_mobile);
            }
            mEtMobileNumber.setSelection(mobileNumber.length());
            mEtMobileNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(identifyNumber)) {
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_empty_identify);
            mEtIdentifyNumber.requestFocus();
            return;
        }
        if (identifyNumber.length() != 6) {
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_six_identify);
            mEtIdentifyNumber.setSelection(identifyNumber.length());
            mEtIdentifyNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_empty_password);
            mEtInputPassword.requestFocus();
            return;
        }
        if (password.length() < 6 || password.length() > 20) {
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_six_twenty_identify);
            mEtInputPassword.setSelection(password.length());
            mEtInputPassword.requestFocus();
            return;
        }

        if (NetworkUtils.isNetworkAvaiable(ForgetPasswordActivity.this)) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, ForgetPasswordActivity.class.getName());
            Map<String, String> map = new HashMap<String, String>();
            map.put("mobile", mobileNumber);
            map.put("verifyCode", identifyNumber);
            map.put("pwd", password);
            ApiUtils.post(getApplicationContext(), URLConstants.USER_RESET, map, UserResultInfo.class, new Response.Listener<UserResultInfo>() {
                @Override
                public void onResponse(UserResultInfo response) {
                    if (response != null) {
                        if (response.code == 0) {
                            CustomDialogFragment.dismissDialog();
                            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_changedpassword_success);
                            O2OUtils.cacheUserData(ForgetPasswordActivity.this, response);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            CustomDialogFragment.dismissDialog();
                            ToastUtils.show(ForgetPasswordActivity.this, response.msg);
                        }
                    } else {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(ForgetPasswordActivity.this, R.string.label_change_fail);
                    }
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CustomDialogFragment.dismissDialog();
                    ToastUtils.show(ForgetPasswordActivity.this, R.string.label_change_fail);
                }
            });
        } else {
            ToastUtils.show(ForgetPasswordActivity.this, R.string.label_check_mobile);
        }
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.get_password);
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
            String value = getResources().getString(R.string.afresh_send)+ "(" + millisUntilFinished / 1000 + " )";
            mTvGetIndentifyCode.setText(value);
        }
    }
}
