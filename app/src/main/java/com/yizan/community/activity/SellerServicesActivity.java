package com.yizan.community.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.jude.rollviewpager.RollPagerView;
import com.yizan.community.R;
import com.yizan.community.adapter.BannerPagerAdapter;
import com.yizan.community.adapter.ServicesListAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.ServiceInfo;
import com.fanwe.seallibrary.model.result.SellerObjectResult;
import com.fanwe.seallibrary.model.result.ServiceListResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.LoadingFooter;
import com.yizan.community.widget.PageStaggeredGridView;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 服务类商家详情
 */
public class SellerServicesActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseActivity.TitleListener, View.OnClickListener {
    private SellerInfo mSellerInfo;
    private int mSellerId;
    private SwipeRefreshLayout mSwipeLayout;
    private PageStaggeredGridView mGridView;
    private ServicesListAdapter mListAdapter;
    private View mHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_services);

//        mSellerInfo = (SellerInfo) this.getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        mSellerId = this.getIntent().getIntExtra(Constants.EXTRA_DATA, -1);
        if (mSellerId == -1) {
            ToastUtils.show(this, R.string.err_bad_param);
            finish();
            return;
        }
        initView();
        setTitleListener(this);
    }

    private void initView() {
        mSwipeLayout = mViewFinder.find(R.id.sp_container);
        mGridView = mViewFinder.find(R.id.gv_list);
        mListAdapter = new ServicesListAdapter(this, new ArrayList<ServiceInfo>());
        mHeaderView = LayoutInflater.from(this).inflate(R.layout.layout_seller_services_header, null);

        mGridView.addHeaderView(mHeaderView);
        mGridView.setAdapter(mListAdapter);
//        mGridView.setEmptyView(mViewFinder.find(android.R.id.empty));
        initListHeader();

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int goodId = mListAdapter.getItem(position - mGridView.getHeaderViewsCount()).id;
                Intent intent = new Intent(SellerServicesActivity.this, GoodDetailActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, goodId);
                startActivity(intent);
            }
        });

        mSwipeLayout.setOnRefreshListener(this);

        loadSellerDetail();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                loadData();
            }
        }, 100);


    }

    private void initSellerBanner() {
        if (mHeaderView == null || mSellerInfo == null) {
            return;
        }
        RollPagerView rollViewPager = (RollPagerView)mHeaderView.findViewById(R.id.roll_view_pager);
        if(ArraysUtils.isEmpty(mSellerInfo.banner)){
            rollViewPager.setVisibility(View.GONE);
        }else {
            rollViewPager.setVisibility(View.VISIBLE);
            rollViewPager.setAdapter(new BannerPagerAdapter(this, mSellerInfo.banner, null));
        }

    }
    private void initListHeader() {
        if (mHeaderView == null || mSellerInfo == null) {
            return;
        }


        initSellerBanner();
        NetworkImageView iv = (NetworkImageView) mHeaderView.findViewById(R.id.iv_image);
        iv.setImageUrl(mSellerInfo.logo, RequestManager.getImageLoader());

        TextView tv = (TextView) mHeaderView.findViewById(R.id.tv_name);
        tv.setText(mSellerInfo.name);
        tv = (TextView) mHeaderView.findViewById(R.id.tv_time);
        tv.setText(getResources().getString(R.string.home_seller_business_time) + mSellerInfo.businessHours);
        mHeaderView.findViewById(R.id.ll_seller_container).setOnClickListener(this);
    }


    @Override
    public void onRefresh() {
        loadData();
    }

    private void loadData() {
        // 加载数据
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.loading_err_net);
            mSwipeLayout.setRefreshing(false);
            return;
        }
        mSwipeLayout.setRefreshing(true);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(mSellerId));
//        params.put("id", String.valueOf(62));
        ApiUtils.post(this, URLConstants.SERVICE_LIST,
                params,
                ServiceListResult.class, responseListener(), errorListener());
    }

    private void loadSellerDetail() {
        // 加载数据
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.loading_err_net);
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(mSellerId));
//        params.put("id", String.valueOf(62));
        ApiUtils.post(this, URLConstants.SELLERDETAIL,
                params,
                SellerObjectResult.class, new Response.Listener<SellerObjectResult>() {
                    @Override
                    public void onResponse(SellerObjectResult response) {
                        if(O2OUtils.checkResponse(SellerServicesActivity.this, response)){
                            if(response.data != null){
                                mSellerInfo = response.data;
//                                initSellerBanner();
                                initListHeader();
                            }
                        }
                    }
                });
    }

    private Response.Listener<ServiceListResult> responseListener() {
        return new Response.Listener<ServiceListResult>() {
            @Override
            public void onResponse(final ServiceListResult response) {
                if (O2OUtils.checkResponse(getApplicationContext(), response)) {
                    mListAdapter.clear();
                    if (!ArraysUtils.isEmpty(response.data)) {
                        mListAdapter.addAll(response.data);
//                        mViewFinder.find(android.R.id.empty).setVisibility(View.INVISIBLE);
                    }else{
//                        mViewFinder.find(android.R.id.empty).setVisibility(View.VISIBLE);
                    }

                }
                setRefreshing(false);

            }
        };
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(getApplicationContext(), R.string.loading_err_nor);
                setRefreshing(false);
                mGridView.setState(LoadingFooter.State.Idle, 3000);
            }
        };
    }


    private void setRefreshing(boolean refreshing) {
        mSwipeLayout.setRefreshing(refreshing);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        if(mSellerInfo != null){
            title.setText(mSellerInfo.name);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_seller_container:
                Intent intent = new Intent(this, SellerDetailActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, mSellerInfo.id);
                startActivity(intent);
                break;
        }
    }
}
