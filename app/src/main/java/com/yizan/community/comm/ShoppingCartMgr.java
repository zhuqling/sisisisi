package com.yizan.community.comm;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.InterFace;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.CartSellerInfo;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;
import com.fanwe.seallibrary.model.result.CartSellerListResult;
import com.fanwe.seallibrary.model.result.SellerGoodsResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 购物车
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-24
 * Time: 15:15
 */
public class ShoppingCartMgr {
    private static ShoppingCartMgr sShoppingCartMgr;
    private List<CartSellerInfo> mCartSellerInfos;
    private boolean mIsLoading = false;
    private boolean mInitOk = false;

    private ShoppingCartMgr() {
        mCartSellerInfos = new ArrayList<CartSellerInfo>();
    }

    public static ShoppingCartMgr getInstance() {
        if (sShoppingCartMgr == null) {
            sShoppingCartMgr = new ShoppingCartMgr();
        }
        return sShoppingCartMgr;
    }

    public void setCart(List<CartSellerInfo> cartSellerInfoList) {
        mCartSellerInfos = cartSellerInfoList;
    }

    public void clearCart() {
        mCartSellerInfos = new ArrayList<CartSellerInfo>();
    }

    public List<CartSellerInfo> getCart() {
        return mCartSellerInfos;
    }

    public boolean isInited() {
        return mInitOk;
    }

    public boolean loadCart(Context context) {
        if (!O2OUtils.isLogin(context)) {
            return false;
        }
        if (!NetworkUtils.isNetworkAvaiable(context)) {
            return false;
        }
        if (mIsLoading) {
            return true;
        }
        mIsLoading = true;
        HashMap<String, String> params = new HashMap<String, String>();
        ApiUtils.post(context, URLConstants.CART_LISTS,
                params,
                CartSellerListResult.class, responseListener(), errorListener());

        return true;
    }

    private Response.Listener<CartSellerListResult> responseListener() {
        return new Response.Listener<CartSellerListResult>() {
            @Override
            public void onResponse(final CartSellerListResult response) {
                if (response != null && response.data != null) {
                    mCartSellerInfos = response.data;
                }
                if (response.code == InterFace.ResponseCode.SUCCESS.code) {
                    mInitOk = true;
                }
                mIsLoading = false;
            }
        };

    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mIsLoading = false;
            }
        };
    }

    public int getTotalCount(int sellerId) {
        if (ArraysUtils.isEmpty(mCartSellerInfos)) {
            return 0;
        }
        int count = 0;
        for (CartSellerInfo item : mCartSellerInfos) {
            if (ArraysUtils.isEmpty(item.goods)) {
                continue;
            }
            if (sellerId != 0 && sellerId != item.id) {
                continue;
            }
            for (CartGoodsInfo good : item.goods) {
                count += good.num;
            }
        }
        return count;
    }

    public double getTotalPrice(int sellerId) {
        if (ArraysUtils.isEmpty(mCartSellerInfos)) {
            return 0.0;
        }
        double price = 0.0;
        for (CartSellerInfo item : mCartSellerInfos) {
            if (sellerId != 0 && item.id != sellerId) {
                continue;
            }
            price += item.price;
        }
        long l1 = Math.round(price * 100);
        return l1 / 100.0;
    }

    public CartGoodsInfo getCartGood(int goodId, int numsId) {
        if (ArraysUtils.isEmpty(mCartSellerInfos)) {
            return null;
        }

        for (CartSellerInfo item : mCartSellerInfos) {

            if (ArraysUtils.isEmpty(item.goods)) {
                continue;
            }
            for (CartGoodsInfo good : item.goods) {
                if (good.goodsId == goodId && good.normsId == numsId) {
                    return good;
                }
            }
        }
        return null;
    }

    public CartSellerInfo getCartSellerById(int goodId) {
        if (ArraysUtils.isEmpty(mCartSellerInfos)) {
            return null;
        }

        for (CartSellerInfo item : mCartSellerInfos) {

            if (ArraysUtils.isEmpty(item.goods)) {
                continue;
            }
            for (CartGoodsInfo good : item.goods) {
                if (good.goodsId == goodId) {
                    return item;
                }
            }
        }
        return null;
    }

    public boolean saveShopping(final FragmentActivity context, final int goodsId, String serviceTime, int num) {
        return saveShopping(context, goodsId, 0, serviceTime, num, "shopping_cat_event");
    }

    public boolean saveShopping(final FragmentActivity context, final int goodsId, int normsId, String serviceTime, int num) {
        return saveShopping(context, goodsId, normsId, serviceTime, num, "shopping_cat_event");
    }

    public boolean saveShopping(final FragmentActivity context, final int goodsId, int normsId, int num) {
        return saveShopping(context, goodsId, normsId, "", num, "shopping_cat_event");
    }

    public boolean saveShopping(final FragmentActivity context, final int goodsId, int normsId, String serviceTime, final int num, final String tag) {
        if (O2OUtils.turnLogin(context)) {
            return false;
        }
        if (!NetworkUtils.isNetworkAvaiable(context)) {
            ToastUtils.show(context, R.string.loading_err_net);
            return false;
        }

        if (num < 0) {
            return false;
        }
        CustomDialogFragment.show(context.getSupportFragmentManager(), R.string.loading, context.getLocalClassName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("goodsId", String.valueOf(goodsId));
        params.put("normsId", String.valueOf(normsId));
        params.put("num", String.valueOf(num));
        if (!TextUtils.isEmpty(serviceTime)) {
            params.put("serviceTime", serviceTime);
        }
        ApiUtils.post(context, URLConstants.SHOPPING_SAVE,
                params,
                CartSellerListResult.class, new Response.Listener<CartSellerListResult>() {
                    @Override
                    public void onResponse(final CartSellerListResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(context, response)) {
                            setCart(response.data);
                            EventBus.getDefault().post(new ShoppingCartEvent(goodsId, true, tag, num));
                        } else {
                            EventBus.getDefault().post(new ShoppingCartEvent(goodsId, false, tag, num));
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(context, R.string.loading_err_nor);
                        EventBus.getDefault().post(new ShoppingCartEvent(goodsId, false, tag, num));
                    }
                });
        return true;
    }

    /**
     * 删除购物车
     * @param context
     * @param tag
     * @return
     */
    public boolean deleteShopping(final FragmentActivity context, final String tag) {
        if (!NetworkUtils.isNetworkAvaiable(context)) {
            ToastUtils.show(context, R.string.loading_err_net);
            return false;
        }
        CustomDialogFragment.show(context.getSupportFragmentManager(), R.string.loading, context.getLocalClassName());
        HashMap<String, String> params = new HashMap<String, String>();
        ApiUtils.post(context, URLConstants.SHOPPING_DELETE,
                params,
                CartSellerListResult.class, new Response.Listener<CartSellerListResult>() {
                    @Override
                    public void onResponse(final CartSellerListResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(context, response)) {
                            setCart(response.data);
                            EventBus.getDefault().post(new ShoppingCartEvent(0, true, tag, 0));
                        } else {
                            EventBus.getDefault().post(new ShoppingCartEvent(0, false, tag, 0));
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(context, R.string.loading_err_nor);
                        EventBus.getDefault().post(new ShoppingCartEvent(0, false, tag, 0));
                    }
                });
        return true;
    }

}
