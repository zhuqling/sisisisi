package com.yizan.community.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.yizan.community.R;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.InitInfo;
import com.fanwe.seallibrary.model.event.LoginEvent;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ztl on 2015/9/18.
 */
public class RegistorActivity extends BaseActivity implements View.OnClickListener,BaseActivity.TitleListener {

    public static final int REQUEST_CODE = 0x102;
    private EditText mEtMobileNumber, mEtIdentifyNumber, mEtInputPassword;
    private Button mBtnRegist;
    private TextView mTvGetIndentifyCode,mRegistAgreement;
    private String identifyNumber, mobileNumber, password;
    private MyCount mc;
    private RelativeLayout mEyes;
    private SpannableString spanText;
    private boolean isChick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registor);
        setTitleListener(this);

        initViews();
    }

    private void initViews() {
        mEyes = mViewFinder.find(R.id.registor_pwd_eyes);
        mEyes.setOnClickListener(this);

        mEtMobileNumber = (EditText)findViewById(R.id.regist_mobile);
        mEtIdentifyNumber = (EditText)findViewById(R.id.regist_code_edit);
        mEtInputPassword = (EditText)findViewById(R.id.regist_pwd);

        mBtnRegist = (Button)findViewById(R.id.btn_registor);
        mBtnRegist.setOnClickListener(this);

        mTvGetIndentifyCode = (TextView)findViewById(R.id.regist_get_code);
        mTvGetIndentifyCode.setOnClickListener(this);

        mRegistAgreement = (TextView)findViewById(R.id.regist_agreement);
        final String tempStr = getString(R.string.regist_argument_end);
        spanText = new SpannableString(getString(R.string.regist_argument_start,tempStr));
        spanText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RegistorActivity.this, WebViewActivity.class);
                String url = PreferenceUtils.getObject(RegistorActivity.this, InitInfo.class).protocolUrl;
                intent.putExtra(Constants.EXTRA_URL, url);
                startActivity(intent);
            }
        },spanText.length()-tempStr.length(), spanText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.theme_main_text)), spanText.length()-tempStr.length(), spanText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mRegistAgreement.setText(spanText);
        mRegistAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_registor:
                registor();
                break;
            case R.id.regist_get_code:
                getSecurityCode();
                break;
            case R.id.registor_pwd_eyes:
                if(isChick){
                    mEtInputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isChick = false;
                }else{
                    mEtInputPassword.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isChick = true;
                }
                break;
        }
    }

    private void registor(){
        mobileNumber = mEtMobileNumber.getText().toString();
        identifyNumber = mEtIdentifyNumber.getText().toString();
        password = mEtInputPassword.getText().toString();
        if (("").equals(mobileNumber)&&("").equals(identifyNumber)&&("").equals(password)){
            ToastUtils.show(RegistorActivity.this,R.string.label_legal_mobile_null);
            mEtMobileNumber.setSelection(mobileNumber.length());
            mEtMobileNumber.requestFocus();
            return;
        }
        if (!CheckUtils.isMobileNO(mobileNumber)) {
            if (!TextUtils.isEmpty(mobileNumber)) {
                ToastUtils.show(RegistorActivity.this, R.string.label_legal_mobile);
            }
            mEtMobileNumber.setSelection(mobileNumber.length());
            mEtMobileNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(identifyNumber)) {
            ToastUtils.show(RegistorActivity.this, R.string.label_empty_identify);
            mEtIdentifyNumber.requestFocus();
            return;
        }
        if (identifyNumber.length() != 6) {
            ToastUtils.show(RegistorActivity.this, R.string.label_six_identify);
            mEtIdentifyNumber.setSelection(identifyNumber.length());
            mEtIdentifyNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtils.show(RegistorActivity.this, R.string.label_empty_password);
            mEtInputPassword.requestFocus();
            return;
        }
        if (password.length() < 6 || password.length() > 20) {
            ToastUtils.show(RegistorActivity.this, R.string.label_six_twenty_identify);
            mEtInputPassword.setSelection(password.length());
            mEtInputPassword.requestFocus();
            return;
        }

        if (NetworkUtils.isNetworkAvaiable(RegistorActivity.this)) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, ForgetPasswordActivity.class.getName());
            Map<String, String> map = new HashMap<String, String>();
            map.put("mobile", mobileNumber);
            map.put("verifyCode", identifyNumber);
            map.put("pwd", password);
            map.put("pwdconf", password);
            ApiUtils.post(getApplicationContext(), URLConstants.REGISTER, map, UserResultInfo.class, new Response.Listener<UserResultInfo>() {
                @Override
                public void onResponse(UserResultInfo response) {
                    if (response != null) {
                        if (response.code == 0) {
                            CustomDialogFragment.dismissDialog();
                            ToastUtils.show(RegistorActivity.this, R.string.msg_register_succ);
                            O2OUtils.cacheUserData(RegistorActivity.this, response);
                            LoginEvent loginEvent = new LoginEvent(true);
                            EventBus.getDefault().post(loginEvent);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            CustomDialogFragment.dismissDialog();
                            ToastUtils.show(RegistorActivity.this, response.msg);
                        }
                    } else {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(RegistorActivity.this, R.string.label_regist_fail);
                    }
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CustomDialogFragment.dismissDialog();
                }
            });
        } else {
            ToastUtils.show(RegistorActivity.this, R.string.label_check_mobile);
        }
    }

    private void getSecurityCode(){
        AppUtils.hideSoftInput(RegistorActivity.this);
        mobileNumber = mEtMobileNumber.getText().toString();
        identifyNumber = mEtIdentifyNumber.getText().toString();
        if (mobileNumber == null || mobileNumber.length() == 0) {
            ToastUtils.show(RegistorActivity.this, R.string.label_empty_mobile);
            mEtMobileNumber.requestFocus();
            return;
        }
        if (!CheckUtils.isMobileNO(mobileNumber)) {
            ToastUtils.show(RegistorActivity.this, R.string.label_legal_mobile);
            mEtMobileNumber.setSelection(mobileNumber.length());
            mEtMobileNumber.requestFocus();
            return;
        }
        if (mobileNumber == null || mobileNumber.length() == 0){

        }
        mc = new MyCount(61000, 1000);
        // mc.start();
        if (NetworkUtils.isNetworkAvaiable(RegistorActivity.this)) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, ForgetPasswordActivity.class.getName());
            Map<String, String> map = new HashMap<String, String>();
            map.put("mobile", mobileNumber);
            map.put("type","reg_check");
            ApiUtils.post(getApplicationContext(), URLConstants.INENTIFYING, map, BaseResult.class, new Response.Listener<BaseResult>() {

                @Override
                public void onResponse(BaseResult response) {
                    CustomDialogFragment.dismissDialog();
                    if(O2OUtils.checkResponse(getActivity(), response)) {
                        ToastUtils.show(RegistorActivity.this,response.msg);
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
            ToastUtils.show(RegistorActivity.this, R.string.label_check_mobile);
        }
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.regist);
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
            String value = getString(R.string.afresh_send) + "(" + millisUntilFinished / 1000 + " )";
            mTvGetIndentifyCode.setText(value);
        }
    }
}
