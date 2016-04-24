package com.yizan.community.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;

public class UserNickActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {
    private EditText mEditText;
    private UserInfo mUserInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_nick);
        setTitleListener(this);
        mViewFinder.onClick(R.id.btn_commit, this);
        mEditText = mViewFinder.find(R.id.et_nick);
        mUserInfo = PreferenceUtils.getObject(this, UserInfo.class);
        mEditText.setText(mUserInfo.name);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_user_nick);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_commit:
                uploadData();
                break;
        }
    }

    protected void uploadData() {
        String nickname = mEditText.getText().toString();
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
            return;
        }
        if (TextUtils.isEmpty(nickname) || nickname.length() < 2) {
            ToastUtils.show(this, R.string.err_nick_name);
            return;
        }
        HashMap<String, Object> data = new HashMap<String, Object>();

        if (!TextUtils.isEmpty(nickname)) {
            data.put("name", nickname);
        }

        ApiUtils.post(this, URLConstants.STAFF_UPDATE,
                data, UserResultInfo.class,
                new Response.Listener<UserResultInfo>() {
                    @Override
                    public void onResponse(UserResultInfo response) {
                        if (O2OUtils.checkResponse(getActivity(),
                                response)) {
                            ToastUtils.show(getActivity(),
                                    R.string.msg_success_save);
                            mUserInfo = response.data;
                            PreferenceUtils.setObject(getActivity(), mUserInfo);
                            setResult(RESULT_OK);
                            finishActivity();
                        }
                        CustomDialogFragment.dismissDialog();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(getActivity(),
                                R.string.msg_failed_update);
                    }
                });
    }
}
