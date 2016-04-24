package com.yizan.community.activity;

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
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.result.AddressResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 地址编辑Activity，掌管生活点击进入简单定位
 * Created by ztl on 2015/9/22.
 */
public class AddressAddActivity extends BaseActivity implements BaseActivity.TitleListener {

    private EditText mReciver, mMobile, mAddressEditText;
    private String mapPoint;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        setTitleListener_RightImage(this);

        initViews();
    }

    private void initViews() {
        mReciver = (EditText) findViewById(R.id.add_addr_reciver);
        mMobile = (EditText) findViewById(R.id.add_addr_mobile);
        mViewFinder.find(R.id.add_addr_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressAddActivity.this, ChooseAddressActivity.class);
                if(!TextUtils.isEmpty(mapPoint)){
                    intent.putExtra(Constants.EXTRA_DATA, mapPoint);
                }
                startActivityForResult(intent, 1001);
            }
        });
        mAddressEditText = (EditText) findViewById(R.id.address_add);
        UserAddressInfo userAddressInfo = (UserAddressInfo)this.getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        if(userAddressInfo != null){
            mapPoint = userAddressInfo.mapPoint.toString();
            mReciver.setText(userAddressInfo.name);
            mMobile.setText(userAddressInfo.mobile);
            mAddressEditText.setText(userAddressInfo.detailAddress);
            id = userAddressInfo.id;
//            id = bundle.getInt("id");
        }else {
            Bundle bundle = getIntent().getExtras();
            if (null != bundle) {
                mapPoint = bundle.getString("mapPoint");
                mReciver.setText(bundle.getString("name"));
                mMobile.setText(bundle.getString("mobile"));
                mAddressEditText.setText(bundle.getString("detail"));
                id = bundle.getInt("id");
            }
        }
    }

    private void addAddr() {
        if (TextUtils.isEmpty(mReciver.getText().toString().trim())) {
            ToastUtils.show(AddressAddActivity.this, R.string.hint_name);
            mReciver.requestFocus();
            return;
        }
        String mobileStr = mMobile.getText().toString();
        if (!CheckUtils.isMobileNO(mobileStr)) {
            ToastUtils.show(AddressAddActivity.this, R.string.label_legal_mobile);
            mMobile.setSelection(mobileStr.length());
            mMobile.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(mobileStr)) {
            ToastUtils.show(AddressAddActivity.this, R.string.hint_mobile);
            mMobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mAddressEditText.getText().toString().trim())) {
            ToastUtils.show(AddressAddActivity.this, R.string.address_add_hint);
            mAddressEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mapPoint)) {
            ToastUtils.show(AddressAddActivity.this, R.string.msg_error_select_addr);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, AddressAddActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        data.put("mapPoint", mapPoint);
        data.put("provinceId", "0");
        data.put("cityId", "0");
        data.put("areaId", "0");
        data.put("name", mReciver.getText().toString().trim());
        data.put("mobile", mMobile.getText().toString().trim());
        data.put("detailAddress", mAddressEditText.getText().toString().trim());
        if (0 != id)
            data.put("id", String.valueOf(id));
//        data.put("doorplate", mAddressEditText.getText().toString().trim());
        ApiUtils.post(AddressAddActivity.this, URLConstants.USERADDRESSCREATE, data, AddressResult.class, new Response.Listener<AddressResult>() {
            @Override
            public void onResponse(AddressResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(AddressAddActivity.this, response))
                    finishActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(AddressAddActivity.this, R.string.msg_failed_update);
            }
        });

    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.edit_addr);
        ((ImageButton) right).setImageResource(R.drawable.hook);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAddr();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            mapPoint = data.getExtras().getString("mapPoint");
//            mChooseArea.setText(address);
        }
    }
}
