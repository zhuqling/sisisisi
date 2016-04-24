package com.yizan.community.activity;

import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.BoolResult;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;
import java.util.Map;

public class ChangePwdActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);
        setTitleListener(this);
        mViewFinder.onClick(R.id.btn_commit, this);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_change_pwd);
    }

    @Override
    public void onClick(View v) {
        uploadData();
    }

    protected void uploadData(){
        HashMap<String,String> params = new HashMap<>();
        EditText et = mViewFinder.find(R.id.et_old_pwd);
        if(et.length()<6){
            ToastUtils.show(this, R.string.err_pwd_invalid);
            et.requestFocus();
            return;
        }
        params.put("oldPwd", et.getText().toString());

        et = mViewFinder.find(R.id.et_new_pwd);
        if(et.length()<6){
            ToastUtils.show(this, R.string.err_pwd_invalid);
            et.requestFocus();
            return;
        }
        String newPwd = et.getText().toString();
        et = mViewFinder.find(R.id.et_new_pwd2);
        if(et.length()<6){
            ToastUtils.show(this, R.string.err_pwd_invalid);
            et.requestFocus();
            return;
        }

        if(et.getText().toString().compareTo(newPwd) != 0){
            ToastUtils.show(this, R.string.err_new_pwd_invalid);
            return;
        }
        params.put("pwd", et.getText().toString());

        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(getActivity(), R.string.label_check_mobile);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, UserBindPhoneActivity.class.getName());
        ApiUtils.post(getApplicationContext(), URLConstants.USER_INFO_CHG_PWD, params, UserResultInfo.class, new Response.Listener<UserResultInfo>() {

            @Override
            public void onResponse(UserResultInfo response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(getActivity(), response)) {
                    O2OUtils.reflashLoginToken(getActivity(), response.token);
                    setResult(RESULT_OK);
                    finishActivity();
                    ToastUtils.show(getActivity(), R.string.chg_pwd_ok);
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
}
