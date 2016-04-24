package com.yizan.community.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.DistrictInfo;
import com.fanwe.seallibrary.model.DoorKeysInfo;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.event.DoorUpdateEvent;
import com.fanwe.seallibrary.model.result.DistrictListResult;
import com.fanwe.seallibrary.model.result.DoorKeyResult;
import com.yizan.community.R;
import com.yizan.community.adapter.DistrictListAdapter;
import com.yizan.community.adapter.DoorListAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistrictListActivity extends BaseActivity implements BaseActivity.TitleListener, OnClickListener {
    private DistrictListAdapter mListAdapter;
    public static final int REQUEST_CODE = 0x12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district_list);
        setTitleListener(this);
        ListView lv = mViewFinder.find(R.id.lv_list);
        lv.setEmptyView(mViewFinder.find(android.R.id.empty));
        mListAdapter = new DistrictListAdapter(this, new ArrayList<DistrictInfo>());
        lv.setAdapter(mListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DistrictInfo info = mListAdapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_DATA, info);
                setResult(Activity.RESULT_OK, intent);
                finishActivity();
            }
        });
        mViewFinder.onClick(R.id.tv_search, this);
        loadData();
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_district_list);
    }

    private void initViewData(List<DistrictInfo> list) {
        mListAdapter.setList(list);
    }

    private void loadData() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
        }
        EditText et = mViewFinder.find(R.id.et_keys);
        String name = et.getText().toString().trim();

        CustomDialogFragment.show(getSupportFragmentManager(), R.string.loading, getClass().getName());
        Map<String, String> data = new HashMap<>();
        String url = URLConstants.DISTRICT_NEAREST;
        if (!TextUtils.isEmpty(name)) {
            data.put("keywords", name);
            url = URLConstants.DISTRICT_SEARCH;
        } else {
            LocAddressInfo locAddressInfo = PreferenceUtils.getObject(this, LocAddressInfo.class);
            if (locAddressInfo != null && locAddressInfo.mapPoint != null) {
                data.put("location", locAddressInfo.mapPoint.toString());
            } else {
                data.put("location", "0.0,0.0");
            }
        }
        ApiUtils.post(this,
                url, data, DistrictListResult.class, new Response.Listener<DistrictListResult>() {
                    @Override
                    public void onResponse(DistrictListResult response) {
                        mListAdapter.clear();
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(DistrictListActivity.this, response)) {
                            initViewData(response.data);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(getActivity(), R.string.msg_error);
                        CustomDialogFragment.dismissDialog();
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search:
                loadData();
                break;
        }
    }
}
