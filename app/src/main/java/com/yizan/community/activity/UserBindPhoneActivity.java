package com.yizan.community.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.BoolResult;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;
import java.util.Map;

public class UserBindPhoneActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {
    private Button mBtnGet1, mBtnGet2;
    private MyCount mCountDown;
    private UserInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bind_phone);
        setTitleListener(this);
        mViewFinder.onClick(R.id.btn_next, this);
        mViewFinder.onClick(R.id.btn_ok, this);
        mViewFinder.onClick(R.id.btn_get_identifying, this);
        mViewFinder.onClick(R.id.btn_get_identifying2, this);
        mBtnGet1 = mViewFinder.find(R.id.btn_get_identifying);
        mBtnGet2 = mViewFinder.find(R.id.btn_get_identifying2);
        mUserInfo =  PreferenceUtils.getObject(this, UserInfo.class);
        mViewFinder.setText(R.id.tv_mobile, mUserInfo.mobile);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_user_bind_phone);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_identifying:
                loadCode(mUserInfo.mobile, mBtnGet1);
                break;
            case R.id.btn_get_identifying2:
                EditText et = mViewFinder.find(R.id.et_mobile);
                loadCode(et.getText().toString(), mBtnGet2);
                break;
            case R.id.btn_next:
                updateMobileVerify();
                break;
            case R.id.btn_ok:
                updateMobile();
                break;
        }
    }

    private void loadCode(String mobile, final Button btn) {
        AppUtils.hideSoftInput(this);
        if (!CheckUtils.isMobileNO(mobile)) {
            ToastUtils.show(this, R.string.err_bad_mobile);
            return;
        }

        mCountDown = new MyCount(61000, 1000, btn);
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(getActivity(), R.string.label_check_mobile);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, PhoneLoginActivity.class.getName());
        Map<String, String> map = new HashMap<String, String>();
        map.put("mobile", mobile);
        if(btn.getId() == R.id.btn_get_identifying2){
            map.put("type", "reg_check");
        }
        ApiUtils.post(getApplicationContext(), URLConstants.INENTIFYING, map, BaseResult.class, new Response.Listener<BaseResult>() {

            @Override
            public void onResponse(BaseResult response) {
                CustomDialogFragment.dismissDialog();
                if(O2OUtils.checkResponse(getActivity(), response)) {
                    ToastUtils.show(getActivity(), R.string.label_send_success);
                    btn.setEnabled(false);
                    mCountDown.start();
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
            }
        });
    }

    private void updateMobile() {
        AppUtils.hideSoftInput(this);
        EditText et = mViewFinder.find(R.id.et_mobile);
        String mobile = et.getText().toString();
        if (!CheckUtils.isMobileNO(mobile)) {
            ToastUtils.show(this, R.string.err_bad_mobile);
            return;
        }
        et = mViewFinder.find(R.id.et_identify_code2);
        String verifyCode = et.getText().toString();

        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(getActivity(), R.string.label_check_mobile);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, UserBindPhoneActivity.class.getName());
        Map<String, String> map = new HashMap<String, String>();
        map.put("mobile", mobile);
        map.put("oldMobile", mUserInfo.mobile);
        map.put("verifyCode", verifyCode);
        if(verifyCode.length() < 6){
            ToastUtils.show(this, R.string.label_six_identify);
            et.requestFocus();
            return;
        }
        ApiUtils.post(getApplicationContext(), URLConstants.USER_UPDATE_MOBILE, map, UserResultInfo.class, new Response.Listener<UserResultInfo>() {

            @Override
            public void onResponse(UserResultInfo response) {
                CustomDialogFragment.dismissDialog();
                if(O2OUtils.checkResponse(getActivity(), response)) {
                    ToastUtils.show(getActivity(),
                            R.string.msg_success_save);
                    mUserInfo = response.data;
                    PreferenceUtils.setObject(getActivity(), mUserInfo);
                    setResult(RESULT_OK);
                    finishActivity();
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(getActivity(), R.string.loading_err_nor);
            }
        });
    }

    private void updateMobileVerify(){
        AppUtils.hideSoftInput(this);
        EditText et = mViewFinder.find(R.id.et_identify_code);
        String verifyCode = et.getText().toString();
        if(verifyCode.length() < 6){
            ToastUtils.show(this, R.string.label_six_identify);
            et.requestFocus();
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(getActivity(), R.string.label_check_mobile);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, UserBindPhoneActivity.class.getName());
        Map<String, String> map = new HashMap<String, String>();
        map.put("mobile", mUserInfo.mobile);
        map.put("verifyCode", verifyCode);
        ApiUtils.post(getApplicationContext(), URLConstants.USER_INFO_VERIFY_MOBILE, map, BoolResult.class, new Response.Listener<BoolResult>() {

            @Override
            public void onResponse(BoolResult response) {
                CustomDialogFragment.dismissDialog();
                if(O2OUtils.checkResponse(getActivity(), response)){
                    if(!TextUtils.isEmpty(response.data) && response.data.compareTo("true") == 0){
                        mViewFinder.find(R.id.ll_container).setVisibility(View.GONE);
                        mViewFinder.find(R.id.ll_container2).setVisibility(View.VISIBLE);
                        ToastUtils.show(getActivity(), R.string.err_valid_ok);
                    }else{
                        ToastUtils.show(getActivity(), R.string.err_valid_failed);
                    }
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(getActivity(), R.string.loading_err_nor);
            }
        });
    }
    class MyCount extends CountDownTimer {
        private Button mButton;
        public MyCount(long millisInFuture, long countDownInterval, Button btn) {
            super(millisInFuture, countDownInterval);
            mButton = btn;
        }

        @Override
        public void onFinish() {

            mButton.setText(R.string.lable_get_identifying);
            mButton.setEnabled(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            String value = getString(R.string.afresh_send) + "(" + millisUntilFinished / 1000 + " )";
            mButton.setText(value);
        }
    }
}
