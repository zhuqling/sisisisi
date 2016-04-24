package com.yizan.community.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.yizan.community.R;
import com.yizan.community.adapter.GoodNormsAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.yizan.community.comm.ShoppingCartMgr;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.GoodInfo;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;
import com.fanwe.seallibrary.model.result.CartSellerListResult;
import com.fanwe.seallibrary.model.result.GoodResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.flow.FlowLayout;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 商品型号Activity
 */
public class GoodNormsActivity extends FragmentActivity implements OnClickListener {
    private int mGoodId;
    private GoodInfo mGoodInfo;
    private GoodNormsAdapter mGoodNormsAdapter;
    private TextView mTvNums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_good_norms);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(ShoppingCartEvent event) {
        if (event.goodsId == mGoodInfo.id && event.success) {
            closeSelf();
        }
    }

    /**
     * 收起型号
     */
    private void closeSelf() {
        findViewById(R.id.rl_root).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        finish();
        overridePendingTransition(R.anim.activity_open_in_anim, R.anim.activity_zoom_exit);
    }

    private void initView() {
        mGoodId = this.getIntent().getIntExtra(Constants.EXTRA_ID, 0);
        mGoodInfo = (GoodInfo) this.getIntent().getSerializableExtra(Constants.EXTRA_DATA);

        if (mGoodInfo != null) {
            initViewData(mGoodInfo);
        } else if (mGoodId != 0) {
            loadData();
        } else {
            finish();
        }
    }

    private void initViewData(GoodInfo goodInfo) {
        mGoodInfo = goodInfo;
        if (mGoodInfo == null) {
            return;
        }
        NetworkImageView iv = (NetworkImageView) findViewById(R.id.iv_image);
        if (!ArraysUtils.isEmpty(mGoodInfo.images)) {
            iv.setImageUrl(mGoodInfo.images.get(0), RequestManager.getImageLoader());
        }
        final TextView tv = (TextView) findViewById(R.id.tv_price);
        tv.setText("¥" + mGoodInfo.norms.get(0).price);

        final TextView stockNum = (TextView) findViewById(R.id.tv_stock);
        stockNum.setText(String.format(getString(R.string.good_stock_nums), mGoodInfo.norms.get(0).stock));
//        CartGoodsInfo cartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodInfo.id);
        if (!ArraysUtils.isEmpty(mGoodInfo.norms)) {
            mGoodNormsAdapter = new GoodNormsAdapter(this, mGoodInfo.norms);
//            if (cartGoodsInfo != null) {
//                mGoodNormsAdapter.setSelectedById(cartGoodsInfo.id);
//            } else {
//                mGoodNormsAdapter.setSelected(0);
//            }
            mGoodNormsAdapter.setSelected(0);
            FlowLayout flowLayout = (FlowLayout) findViewById(R.id.fl_norms);
            flowLayout.setAdapter(mGoodNormsAdapter);
            flowLayout.setItemClickListener(new FlowLayout.TagItemClickListener() {
                @Override
                public void itemClick(int position) {
                    mGoodNormsAdapter.setSelected(position);
                    mTvNums = (TextView) findViewById(R.id.tv_num);
                    CartGoodsInfo cartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodInfo.id, mGoodNormsAdapter.getItem(position).id);
                    if (cartGoodsInfo != null) {
                        mTvNums.setText(String.valueOf(cartGoodsInfo.num));
                        tv.setText("¥" + cartGoodsInfo.price);
                    } else {
                        mTvNums.setText(String.valueOf(0));
                    }
                    tv.setText("¥" + mGoodInfo.norms.get(position).price);
                    stockNum.setText(String.format(getString(R.string.good_stock_nums), mGoodInfo.norms.get(position).stock));
                }
            });
        } else {
            findViewById(R.id.fl_norms).setVisibility(View.GONE);
            findViewById(R.id.line_norms).setVisibility(View.GONE);
            findViewById(R.id.tv_norms_desc).setVisibility(View.GONE);
        }

        findViewById(R.id.iv_close).setOnClickListener(this);
        findViewById(R.id.iv_add).setOnClickListener(this);
        findViewById(R.id.iv_sub).setOnClickListener(this);
        findViewById(R.id.tv_add_cart).setOnClickListener(this);
        findViewById(R.id.tv_buy).setOnClickListener(this);

        mTvNums = (TextView) findViewById(R.id.tv_num);
        int normsId = 0;
        if (!ArraysUtils.isEmpty(mGoodInfo.norms)) {
            normsId = mGoodInfo.norms.get(0).id;
        }
        CartGoodsInfo cartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodInfo.id, normsId);

        if (cartGoodsInfo != null) {
            mTvNums.setText(String.valueOf(cartGoodsInfo.num));
        } else {
            mTvNums.setText(String.valueOf(0));
        }


    }

    private void loadData() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.loading, GoodNormsActivity.class.getName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("goodsId", String.valueOf(mGoodId));
        ApiUtils.post(this, URLConstants.GOODS_DETAIL,
                params,
                GoodResult.class, responseListener(), errorListener());
    }

    private Response.Listener<GoodResult> responseListener() {
        return new Response.Listener<GoodResult>() {
            @Override
            public void onResponse(final GoodResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(GoodNormsActivity.this, response) && null != response.data) {
                    initViewData(response.data);
                }

            }
        };

    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(GoodNormsActivity.this, R.string.loading_err_nor);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                closeSelf();
                break;
            case R.id.iv_add:
                addGoodNum(1);
                break;
            case R.id.iv_sub:
                addGoodNum(-1);
                break;
            case R.id.tv_buy:
                buyGoods();
                break;
            case R.id.tv_add_cart:
                addCart();
                break;
        }
    }

    private boolean buyGoods() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.loading_err_net);
            return false;
        }
        final int num = Integer.parseInt(mTvNums.getText().toString());
        if (num <= 0) {
            ToastUtils.show(this, R.string.err_sel_goods);
            return false;
        }
        int normsId = -1;
        if (mGoodNormsAdapter != null && mGoodNormsAdapter.getCount() > 0) {
            normsId = mGoodNormsAdapter.getSelectedId();
            if (-1 == normsId) {
                ToastUtils.show(this, R.string.err_sel_norms);
                return false;
            }
        }
        CustomDialogFragment.show(this.getSupportFragmentManager(), R.string.loading, this.getLocalClassName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("goodsId", String.valueOf(mGoodInfo.id));
        params.put("normsId", String.valueOf(normsId));
        params.put("num", String.valueOf(num));
        final int finalNormsId = normsId;
        ApiUtils.post(this, URLConstants.SHOPPING_SAVE,
                params,
                CartSellerListResult.class, new Response.Listener<CartSellerListResult>() {
                    @Override
                    public void onResponse(final CartSellerListResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(GoodNormsActivity.this, response)) {
                            ShoppingCartMgr.getInstance().setCart(response.data);
                            if (ArraysUtils.isEmpty(response.data)) {
                                return;
                            }
                            EventBus.getDefault().post(new ShoppingCartEvent(mGoodInfo.id, true, getResources().getString(R.string.shopping_cat_event), num));
                            Intent intent = new Intent(GoodNormsActivity.this, OrderConfirmActivity.class);
                            ArrayList<CartGoodsInfo> list = new ArrayList<CartGoodsInfo>();

                            CartGoodsInfo cartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodInfo.id, finalNormsId);
                            list.add(cartGoodsInfo);

                            intent.putExtra(Constants.EXTRA_DATA, list);
                            intent.putExtra(Constants.EXTRA_SELLER, ShoppingCartMgr.getInstance().getCartSellerById(mGoodInfo.id));
                            startActivity(intent);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(GoodNormsActivity.this, R.string.loading_err_nor);
                    }
                });
        return true;
    }

    private void addCart() {
        int normsId = 0;
        int nums = 0;

        if (mGoodNormsAdapter != null && mGoodNormsAdapter.getCount() > 0) {
            normsId = mGoodNormsAdapter.getSelectedId();
            if (-1 == normsId) {
                ToastUtils.show(this, R.string.err_sel_norms);
                return;
            }
        }
        nums = Integer.parseInt(mTvNums.getText().toString());
        ShoppingCartMgr.getInstance().saveShopping(this, mGoodInfo.id, normsId, nums);

    }

    private void addGoodNum(int value) {
        TextView tv = (TextView) findViewById(R.id.tv_num);
        int num = Integer.parseInt(tv.getText().toString());
        num += value;
        if (num < 0) {
            num = 0;
        }

        tv.setText(String.valueOf(num));

    }
}
