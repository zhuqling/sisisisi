package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.result.AddressResult;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址编辑Activity，通用
 * Created by ztl on 2015/9/22.
 */
public class AddressAddCommonActivity extends BaseActivity implements BaseActivity.TitleListener {

    private EditText mReciver, mMobile, mDetail;
    private TextView mChooseArea;
    private String mapPoint, address;
    private UserAddressInfo mAddrInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address_common);
        setTitleListener_RightImage(this);
        mAddrInfo = (UserAddressInfo) getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        initViews();
    }

    private void initViews() {
        mReciver = (EditText) findViewById(R.id.add_addr_reciver);
        mMobile = (EditText) findViewById(R.id.add_addr_mobile);
        mChooseArea = (TextView) findViewById(R.id.add_addr_choose_area);
        mDetail = (EditText) findViewById(R.id.add_addr_detail);
        if (mAddrInfo!=null){
            mReciver.setText(mAddrInfo.name);
            mMobile.setText(mAddrInfo.mobile);
            mChooseArea.setText(mAddrInfo.detailAddress);
            mDetail.setText(mAddrInfo.doorplate);
            mapPoint=mAddrInfo.mapPoint.toString();
        }
        mChooseArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressAddCommonActivity.this, AddressChooseCommonActivity.class);
                startActivityForResult(intent, 1001);
            }
        });
    }

    private void addAddr() {
        String mobileStr = mMobile.getText().toString();
        if (TextUtils.isEmpty(mReciver.getText().toString().trim())) {
            ToastUtils.show(AddressAddCommonActivity.this, R.string.hint_name);
            mReciver.requestFocus();
            return;
        }
        if (!CheckUtils.isMobileNO(mobileStr)) {
            ToastUtils.show(AddressAddCommonActivity.this, R.string.label_legal_mobile);
            mMobile.setSelection(mobileStr.length());
            mMobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mDetail.getText().toString().trim())) {
            ToastUtils.show(AddressAddCommonActivity.this, R.string.hint_detail);
            mDetail.requestFocus();
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, AddressAddCommonActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        data.put("mapPoint", mapPoint);
        data.put("provinceId", "0");
        data.put("cityId", "0");
        data.put("areaId", "0");
        data.put("name", mReciver.getText().toString().trim());
        data.put("mobile", mMobile.getText().toString().trim());
        data.put("detailAddress", mChooseArea.getText().toString().trim());
        data.put("doorplate", mDetail.getText().toString().trim());
        ApiUtils.post(AddressAddCommonActivity.this, URLConstants.USERADDRESSCREATE, data, AddressResult.class, new Response.Listener<AddressResult>() {
            @Override
            public void onResponse(AddressResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(AddressAddCommonActivity.this, response))
                    finishActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(AddressAddCommonActivity.this, R.string.msg_failed_update);
            }
        });

    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.add_address);
        ((ImageButton) right).setImageResource(R.drawable.hook);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddrInfo == null) {
                    addAddr();
                } else {
                    CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, AddressAddCommonActivity.class.getName());
                    Map<String, String> data = new HashMap<>();
                    data.put("id",String.valueOf(mAddrInfo.id));
                    data.put("mapPoint", mapPoint);
                    data.put("provinceId", "0");
                    data.put("cityId", "0");
                    data.put("areaId", "0");
                    data.put("name", mReciver.getText().toString().trim());
                    data.put("mobile", mMobile.getText().toString().trim());
                    data.put("detailAddress", mChooseArea.getText().toString().trim());
                    data.put("doorplate", mDetail.getText().toString().trim());
                    ApiUtils.post(AddressAddCommonActivity.this, URLConstants.USERADDRESSCREATE, data, AddressResult.class, new Response.Listener<AddressResult>() {
                        @Override
                        public void onResponse(AddressResult response) {
                            CustomDialogFragment.dismissDialog();
                            if (O2OUtils.checkResponse(AddressAddCommonActivity.this, response))
                                ToastUtils.show(AddressAddCommonActivity.this,response.msg);
                                setResult(Activity.RESULT_OK);
                                finishActivity();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            CustomDialogFragment.dismissDialog();
                            ToastUtils.show(AddressAddCommonActivity.this, R.string.msg_failed_update);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            mapPoint = data.getExtras().getString("mapPoint");
            address = data.getExtras().getString("address");
            mChooseArea.setText(address);
        }
    }

}
