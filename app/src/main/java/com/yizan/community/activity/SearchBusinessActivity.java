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
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.R;
import com.yizan.community.adapter.RecommentSellerListAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.result.SellerResult;
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
 * 商户搜索ListActivity
 * Created by ztl on 2015/9/25.
 */
public class SearchBusinessActivity extends BaseActivity implements BaseActivity.TitleListener,View.OnClickListener {

    private ListView mListView;
    private List<SellerInfo> listDatas=null;
    private RecommentSellerListAdapter adapter;
    private String keyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_business);
        setTitleListener(this);
        keyword = this.getIntent().getStringExtra(Constants.EXTRA_DATA);
        initViews();

    }

    private void initViews() {
        listDatas = new ArrayList<SellerInfo>();
        mListView = mViewFinder.find(R.id.search_business_lv);
        mListView.setEmptyView(mViewFinder.find(android.R.id.empty));
        adapter = new RecommentSellerListAdapter(SearchBusinessActivity.this,listDatas);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchBusinessActivity.this,SellerGoodsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.EXTRA_DATA,listDatas.get(position).id);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSellerLists();
    }

    private void getSellerLists(){
        if (!NetworkUtils.isNetworkAvaiable(SearchBusinessActivity.this)) {
            ToastUtils.show(SearchBusinessActivity.this, R.string.msg_error_network);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, SearchBusinessActivity.class.getName());
        Map<String,String> data = new HashMap<>();
        data.put("sort",String.valueOf(0));
        data.put("page",String.valueOf(listDatas.size()/ Constants.PAGE_SIZE+1));
        data.put("id","");
        data.put("keyword",keyword);
        ApiUtils.post(this, URLConstants.SELLERLISTS, data, SellerResult.class, new Response.Listener<SellerResult>() {
            @Override
            public void onResponse(SellerResult response) {
                if (O2OUtils.checkResponse(SearchBusinessActivity.this, response)) {
                    if (!ArraysUtils.isEmpty(response.data)){
                        listDatas.clear();
                        listDatas.addAll(response.data);
                        adapter.notifyDataSetChanged();
                    }else{
                        ToastUtils.show(SearchBusinessActivity.this,R.string.msg_no_search);
                    }

                }

                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
            }
        });
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.search_business);
    }
}
