package com.yizan.community.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.HomeConfig;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.event.OrderListCommentsEvent;
import com.fanwe.seallibrary.model.result.HomeConfigResult;
import com.tencent.map.geolocation.TencentLocationUtils;
import com.yizan.community.R;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.PagerSlidingTabStrip;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;
import java.util.List;

public class Display_item extends BaseFragment implements View.OnClickListener{

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private SellerInfo mSellerInfo;
    private HomeConfig mHomeConfig;
    private String[] title;
    private LocAddressInfo mAddressInfo;
    private boolean mIsLoading = false;

    public static Display_item newInstance() {
        Display_item fragment = new Display_item();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        title = new String[]{getActivity().getResources().getString(R.string.msg_all_order), getActivity().getResources().getString(R.string.msg_wait_evaluate)};
        return inflater.inflate(R.layout.fragment_order_page, container, false);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Override
    protected void initView() {
        //添加
        mAddressInfo = PreferenceUtils.getObject(getActivity(), LocAddressInfo.class);
        loadData();
    }

    private void initRetryState(int state) {
       /* View container = mViewFinder.find(R.id.ll_retry_container);//数据加载失败的布局
        View btnRetry = mViewFinder.find(R.id.btn_retry);
        if (BuildConfig.USER_PROPERTY_GUARD) {
            mViewFinder.find(R.id.tv_shake_open_door).setVisibility(View.VISIBLE);
        }
        btnRetry.setOnClickListener(this);
        switch (state) {
            case 0:
                container.setVisibility(View.GONE);
                break;
            case 1:
                container.setVisibility(View.VISIBLE);
                btnRetry.setEnabled(true);
                break;
            case 2:
                btnRetry.setEnabled(false);
                break;
        }*/
    }

    private void initViewData(HomeConfig homeConfig) {
        mHomeConfig = homeConfig;
        if (mHomeConfig == null) {
            mViewFinder.find(R.id.sv_container).setVisibility(View.GONE);
           // initRetryState(1);
            return;
        }
        List<SellerInfo> list_seller=mHomeConfig.sellers;
        for (SellerInfo sellinfo:list_seller) {
            double distance = TencentLocationUtils.distanceBetween(sellinfo.mapPoint.x,
                    sellinfo.mapPoint.y, mAddressInfo.mapPoint.x, mAddressInfo.mapPoint.y);
            if (distance<20000){//距離小於20千米---success,获得距离内的商品
                Log.e("看一看123",sellinfo.name);

            }
        }
        mSellerInfo=list_seller.get(list_seller.size()-1);
       // initRetryState(0);
        mViewPager = mViewFinder.find(R.id.viewpager);
        mViewPagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(),mSellerInfo);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.margin_small));
        //向ViewPager绑定PagerSlidingTabStrip
        mPagerSlidingTabStrip = mViewFinder.find(R.id.tabs);
        mPagerSlidingTabStrip.setViewPager(mViewPager);

        mPagerSlidingTabStrip.setUnderlineColorResource(R.color.theme_divide_line);
        mPagerSlidingTabStrip.setUnderlineHeight(1);

        //EventBus.getDefault().register(this);
    }


    private void loadData() {
        if (mIsLoading) {
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(getActivity())) {
            ToastUtils.show(getActivity(), R.string.loading_err_net);
           // initRetryState(1);
            return;
        }
        mIsLoading = true;
       // initRetryState(2);
        CustomDialogFragment.show(getFragmentManager(), R.string.loading, Display_item.this.getTag());
        ApiUtils.post(getActivity(), URLConstants.HOME_CONFIG,
                new HashMap<String, String>(),
                HomeConfigResult.class, new Response.Listener<HomeConfigResult>() {

                    @Override
                    public void onResponse(final HomeConfigResult response) {
                        mIsLoading = false;
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {

                        } else {

                        }
                        initViewData(response.data);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mIsLoading = false;
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(getActivity(), R.string.loading_err_nor);
                        initViewData(null);
                    }
                });
    }

    @Override
    public void onClick(View v) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        String[] title = {getResources().getString(R.string.goods), getResources().getString(R.string.evaluate), getResources().getString(R.string.business)};
        SellerGoodsFragment mFragment1;
        SellerCommentsFragment mFragment2;
        SellerDetailFragment mFragment3;
         SellerInfo mSellerInfo;

        public ViewPagerAdapter(FragmentManager fm,SellerInfo mSellerInfo) {
            super(fm);
            this.mSellerInfo=mSellerInfo;
            Log.e("wcai",mSellerInfo.name);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFragment1 = SellerGoodsFragment.newInstance(mSellerInfo);
                    return mFragment1;
                case 1:
                    mFragment2 = SellerCommentsFragment.newInstance(mSellerInfo);
                    return mFragment2;
                case 2:
                    mFragment3 = SellerDetailFragment.newInstance(mSellerInfo, false);
                    return mFragment3;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {

            return title.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    public void onEventMainThread(OrderListCommentsEvent event) {

        title[1] = getActivity().getResources().getString(R.string.msg_wait_evaluate)+"("+ event.comments + ")";
        mPagerSlidingTabStrip.notifyDataSetChanged();
    }


}
