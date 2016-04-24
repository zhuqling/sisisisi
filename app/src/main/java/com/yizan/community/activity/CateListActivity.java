package com.yizan.community.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.yizan.community.R;
import com.yizan.community.adapter.CateListAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.fanwe.seallibrary.model.result.SellerCatesResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品分类
 */
public class CateListActivity extends BaseActivity implements BaseActivity.TitleListener {
    private CateListAdapter mCateListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cate_list);
        setTitleListener(this);
        initView();
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.all_classify);
    }

    private void initView() {
        ListView lv = mViewFinder.find(R.id.lv_list);
        mCateListAdapter = new CateListAdapter(this, new ArrayList<SellerCatesInfo>());
        lv.setAdapter(mCateListAdapter);
        lv.setEmptyView(mViewFinder.find(android.R.id.empty));
        loadData();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CateListActivity.this, BusinessClassificationActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, String.valueOf(mCateListAdapter.getItem(position).id));
                intent.putExtra(Constants.EXTRA_TYPE, mCateListAdapter.getItem(position).type);
                intent.putExtra(Constants.EXTRA_TITLE, mCateListAdapter.getItem(position).name);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
            return;
        }
        Map<String, String> data = new HashMap<>();
        data.put("id", "0");
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.loading, CateListActivity.class.getName());
        ApiUtils.post(this,
                URLConstants.SELLERCATELISTS, data, SellerCatesResult.class, new Response.Listener<SellerCatesResult>() {
                    @Override
                    public void onResponse(SellerCatesResult response) {
                        if(O2OUtils.checkResponse(CateListActivity.this, response)){
                            if(ArraysUtils.isEmpty(response.data)){

                            }else{
                                mCateListAdapter.setList(response.data);
                            }
                            CustomDialogFragment.dismissDialog();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(CateListActivity.this, R.string.msg_error);
                        CustomDialogFragment.dismissDialog();
                    }
                });

    }
}
