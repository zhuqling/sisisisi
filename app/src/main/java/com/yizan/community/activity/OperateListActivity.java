package com.yizan.community.activity;

import android.app.Activity;
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
import com.yizan.community.adapter.OperateListAdapter;
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
import java.util.List;
import java.util.Map;

/**
 * 经营类型ListActivity
 */
public class OperateListActivity extends BaseActivity implements BaseActivity.TitleListener {
    private OperateListAdapter mCateListAdapter;
    private List<SellerCatesInfo> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate_list);
        setTitleListener(this);
        initView();
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.type_jy);
    }

    private void initView() {
        ListView lv = mViewFinder.find(android.R.id.list);
        mCateListAdapter = new OperateListAdapter(this, mDatas);
        lv.setAdapter(mCateListAdapter);
        lv.setEmptyView(mViewFinder.find(android.R.id.empty));
        loadData();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                SellerCatesInfo item = mDatas.get(position);
//                item.checked = !item.checked;
//
//                if (!ArraysUtils.isEmpty(item.childs)) {
//                    for (SellerCatesInfo info : item.childs) {
//                        info.checked = item.checked;
//                    }
//                }
//                mCateListAdapter.notifyDataSetChanged();
//                if(ArraysUtils.isEmpty(item.childs)){
//                    mCateListAdapter.selCateId(item.id);
//                }
                mCateListAdapter.selCateByPosition(position);
            }
        });
        mViewFinder.find(R.id.operate_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sbIds = new StringBuilder();
                StringBuilder sbNames = new StringBuilder();
                List<SellerCatesInfo> cate = mCateListAdapter.getSelCateList();
                if (ArraysUtils.isEmpty(cate)) {
                    ToastUtils.show(OperateListActivity.this, R.string.msg_error_select_error);
                    return;
                }
                for (SellerCatesInfo item: cate){
                    if(sbIds.length() > 0){
                        sbIds.append(",");
                    }
                    sbIds.append(String.valueOf(item.id));
                    if(sbNames.length() > 0){
                        sbNames.append(",");
                    }
                    sbNames.append(String.valueOf(item.name));
                }

                Intent data = new Intent();
                data.putExtra("data", sbIds.toString());
                data.putExtra("dataName", sbNames.toString());
                setResult(Activity.RESULT_OK, data);
                finishActivity();
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
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, OperateListActivity.class.getName());
        ApiUtils.post(this,
                URLConstants.SELLERCATELISTS, data, SellerCatesResult.class, new Response.Listener<SellerCatesResult>() {
                    @Override
                    public void onResponse(SellerCatesResult response) {
                        CustomDialogFragment.dismissDialog();

                        if (O2OUtils.checkResponse(OperateListActivity.this, response)) {
                            if (ArraysUtils.isEmpty(response.data)) {

                            } else {
                                mDatas.addAll(response.data);
                                mCateListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(OperateListActivity.this, R.string.msg_error);
                    }
                });

    }
}
