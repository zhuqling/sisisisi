package com.yizan.community.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.R;
import com.yizan.community.adapter.SearchHotListAdapter;
import com.yizan.community.adapter.SearchHistoryAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.SearchHistoryInfo;
import com.fanwe.seallibrary.model.SearchHistoryList;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.result.SellerResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.widget.MyListView;
import com.yizan.community.widget.flow.FlowLayout;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ztl on 2015/9/24.
 */
public class SearchActivity extends BaseActivity implements BaseActivity.TitleListener,View.OnClickListener {

    private SearchHotListAdapter mSearchHotListAdapter;
    private MyListView mListView;
    private RelativeLayout mSearchError;
    private RelativeLayout mSearch;
    private EditText mEdt;
    private List<SearchHistoryInfo> listHistory = new ArrayList<SearchHistoryInfo>();
    private SearchHistoryAdapter historyAdapter;
    private LinearLayout clearHistory;
    private boolean isAdd = true;
    SearchHistoryList localList1 = new SearchHistoryList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitleListener(this);
        initViews();
        getSearch();
    }

    private void initViews() {
//        localList1.searchHistoryList = new ArrayList<SearchHistoryInfo>();
        mSearchError = mViewFinder.find(R.id.search_error_view);
        mListView = mViewFinder.find(R.id.search_lv);
        historyAdapter = new SearchHistoryAdapter(SearchActivity.this,listHistory);
        mListView.setAdapter(historyAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchActivity.this, SearchBusinessActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRA_DATA, listHistory.get(position).name);
                intent.putExtras(bundle);
                startActivityForResult(intent, 102);
            }
        });


        mSearchHotListAdapter = new SearchHotListAdapter(SearchActivity.this, new ArrayList<SellerInfo>());

        FlowLayout flowLayout = mViewFinder.find(R.id.fl_hots);
        flowLayout.setAdapter(mSearchHotListAdapter);
        flowLayout.setItemClickListener(new FlowLayout.TagItemClickListener() {
            @Override
            public void itemClick(int position) {
                try {
                    Intent intent = new Intent(SearchActivity.this, SearchBusinessActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.EXTRA_DATA, mSearchHotListAdapter.getItem(position).name);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        mEdt = mViewFinder.find(R.id.choose_community);
        mSearch = mViewFinder.find(R.id.btn_search_rl);
        mSearch.setOnClickListener(this);

        clearHistory = mViewFinder.find(R.id.clear_history);
        clearHistory.setOnClickListener(this);
        SearchHistoryList historyList1 = PreferenceUtils.getObject(SearchActivity.this,SearchHistoryList.class);
        if(historyList1.searchHistoryList != null){
            listHistory.addAll(historyList1.searchHistoryList);
            historyAdapter.notifyDataSetChanged();
            clearHistory.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.VISIBLE);
        }else{
            clearHistory.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_search_rl:
                SearchHistoryInfo history = new SearchHistoryInfo();
                history.name = mEdt.getText().toString().trim();
                if(listHistory.size()<=2){
                    for (int i=0;i<listHistory.size();i++){
                        if ((history.name).equals(listHistory.get(i).name)){
                            isAdd = false;
                        }
                    }
                    if (isAdd == true){
                        localList1.searchHistoryList = listHistory;
                        listHistory.add(history);
                        PreferenceUtils.setObject(SearchActivity.this, localList1);
                    }
                    historyAdapter.notifyDataSetChanged();
                    isAdd = true;
                }else if (listHistory.size()>2){
                    for (int i=0;i<listHistory.size();i++){
                        if ((history.name).equals(listHistory.get(i).name)){
                            isAdd = false;
                        }
                    }
                    if (isAdd == true){
                        listHistory.remove(0);
                        listHistory.add(history);
                        localList1.searchHistoryList = listHistory;
                        PreferenceUtils.setObject(SearchActivity.this, localList1);
                    }

                    historyAdapter.notifyDataSetChanged();
                    isAdd = true;
                }
                if(history.name != null && !("").equals(history.name)){
                    clearHistory.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                }

                Intent intent = new Intent(SearchActivity.this,SearchBusinessActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRA_DATA,mEdt.getText().toString().trim());
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            case R.id.clear_history:
                PreferenceUtils.clearObject(SearchActivity.this,SearchHistoryList.class);
                listHistory.clear();
                historyAdapter.notifyDataSetChanged();
                clearHistory.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                break;
        }
    }

    private void getSearch(){
        CustomDialogFragment.show(getSupportFragmentManager(),R.string.msg_loading,SearchActivity.class.getName());
        Map<String,String> data = new HashMap<>();
        ApiUtils.post(SearchActivity.this, URLConstants.SELLER_HOTLISTS, data, SellerResult.class, new Response.Listener<SellerResult>() {
            @Override
            public void onResponse(SellerResult response) {
                if(response != null && response.data != null){
                    mSearchHotListAdapter.clear();
                    mSearchHotListAdapter.addAll(response.data);
                    mSearchHotListAdapter.notifyDataSetChanged();
                }
                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(SearchActivity.this,R.string.msg_error);
            }
        });
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.search);
    }

}
