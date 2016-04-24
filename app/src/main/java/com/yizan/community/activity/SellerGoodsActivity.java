package com.yizan.community.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.SellerObjectResult;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.fragment.SellerCommentsFragment;
import com.yizan.community.fragment.SellerDetailFragment;
import com.yizan.community.fragment.SellerGoodsFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.PagerSlidingTabStrip;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 商家详情Activity
 */
public class SellerGoodsActivity extends BaseActivity implements BaseActivity.TitleListener {
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private SellerInfo mSellerInfo;
    private int mSellerId;
    private ImageButton mRightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_goods);
        mSellerId = this.getIntent().getIntExtra(Constants.EXTRA_DATA, -1);
        if (mSellerId == -1) {
            ToastUtils.show(this, R.string.err_bad_param);
            finish();
            return;
        }
        loadDate();

    }

    private void loadDate() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, this.getClass().getName());
        Map<String, Integer> data = new HashMap<>();
        data.put("id", mSellerId);
        ApiUtils.post(this, URLConstants.SELLERDETAIL, data, SellerObjectResult.class, new Response.Listener<SellerObjectResult>() {
            @Override
            public void onResponse(SellerObjectResult response) {
                CustomDialogFragment.dismissDialog();
                if (response.data != null) {
                    mSellerInfo = response.data;
                    initViewData();
                }else{
                    ToastUtils.show(getApplicationContext(), R.string.msg_error_load_data);
                    finishActivity();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(getApplicationContext(), R.string.msg_error);
            }
        });
    }

    private void initViewData(){
        if(mSellerInfo == null){
            return;
        }

        mViewPager = mViewFinder.find(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.margin_small));
        //向ViewPager绑定PagerSlidingTabStrip
        mPagerSlidingTabStrip = mViewFinder.find(R.id.tabs);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
//        mPagerSlidingTabStrip.setIndicatorColorResource(R.color.theme_red_background);
//        mPagerSlidingTabStrip.setIndicatorHeight(6);
        setTitleListener_RightImage(this);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        mRightButton = (ImageButton)right;
        if(mSellerInfo != null){
            title.setText(mSellerInfo.name);
            initCollectState(mSellerInfo.isCollect > 0);
            mRightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doCollect();
                }
            });
        }else{
            initCollectState(false);
        }

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        String[] title = {getResources().getString(R.string.goods), getResources().getString(R.string.evaluate), getResources().getString(R.string.business)};
        SellerGoodsFragment mFragment1;
        SellerCommentsFragment mFragment2;
        SellerDetailFragment mFragment3;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
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


    private boolean isCollected() {
        if(mSellerInfo == null){
            return false;
        }
        return mSellerInfo.isCollect>0;
    }

    private void doCollect() {
        if (mSellerInfo == null) {
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            return;
        }
        initCollectState(!(mSellerInfo.isCollect>0));
        String url = URLConstants.COLLECT_CREATE;
        if (isCollected()) {
            url = URLConstants.COLLECTIONDELETE;
        }
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(mSellerInfo.id));
        data.put("type", String.valueOf(2));
        ApiUtils.post(SellerGoodsActivity.this, url
                , data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
                boolean isCollect = isCollected();
                if (O2OUtils.checkResponse(SellerGoodsActivity.this, response)) {
                    isCollect = !isCollect;
                }
                initCollectState(isCollect);
                mSellerInfo.isCollect = isCollect ? 1 : 0;
                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(SellerGoodsActivity.this, R.string.msg_error);
                initCollectState(isCollected());
            }
        });
    }

    private void initCollectState(boolean isCollect) {
        if (mRightButton == null) {
            return;
        }

        if (isCollect) {
            mRightButton.setImageResource(R.drawable.heart_red);
        } else {
            mRightButton.setImageResource(R.drawable.heart);
        }
    }
}
