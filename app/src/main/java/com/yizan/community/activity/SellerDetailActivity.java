package com.yizan.community.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.fragment.SellerDetailFragment;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.SellerObjectResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 商家详情
 * Created by ztl on 2015/9/23.
 */
public class SellerDetailActivity extends BaseActivity implements BaseActivity.TitleListener {

    private SellerInfo mSellerInfo;
    private boolean mIsCollect = false;
    private int mSellerId;
    private ImageButton mRightButton;
    private SellerDetailFragment mSellerDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_detail);
        setTitleListener_RightImage(this);

        mSellerId = this.getIntent().getIntExtra(Constants.EXTRA_DATA, 0);
        loadData();
    }


    private void loadData() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, SellerDetailActivity.class.getName());
        Map<String, Integer> data = new HashMap<>();
        data.put("id", mSellerId);
        ApiUtils.post(this, URLConstants.SELLERDETAIL, data, SellerObjectResult.class, new Response.Listener<SellerObjectResult>() {
            @Override
            public void onResponse(SellerObjectResult response) {
                if (response.data != null) {
                    mSellerInfo = response.data;
                    mIsCollect = mSellerInfo.isCollect > 0;
                    initCollectState(mIsCollect);
                    try {
                        if (null == mSellerDetailFragment) {
                            mSellerDetailFragment = SellerDetailFragment.newInstance(mSellerInfo, true);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, mSellerDetailFragment).commitAllowingStateLoss();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(SellerDetailActivity.this, R.string.msg_error);
            }
        });
    }

    private void doCollect() {
        if (mSellerInfo == null) {
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            return;
        }
        initCollectState(!mIsCollect);
        String url = URLConstants.COLLECT_CREATE;
        if (mIsCollect) {
            url = URLConstants.COLLECTIONDELETE;
        }
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(mSellerInfo.id));
        data.put("type", String.valueOf(2));
        ApiUtils.post(SellerDetailActivity.this, url
                , data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
                if (O2OUtils.checkResponse(SellerDetailActivity.this, response)) {
                    mIsCollect = !mIsCollect;
                }
                initCollectState(mIsCollect);
                mSellerInfo.isCollect = mIsCollect ? 1:0;
                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(SellerDetailActivity.this, R.string.msg_error);
                initCollectState(mIsCollect);
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

    @Override
    public void setTitle(TextView title, ImageButton left, final View right) {
        title.setText(R.string.pro_detail);
        mRightButton = (ImageButton) right;
        initCollectState(false);
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (O2OUtils.isLogin(SellerDetailActivity.this)) {

                    doCollect();
                } else {
                    O2OUtils.turnLogin(SellerDetailActivity.this);
                }

            }
        });
    }


}
