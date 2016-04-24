package com.yizan.community.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import com.android.volley.Response;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.event.OrderEvent;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.OrderResult;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.IntentUtils;
import com.zongyou.library.util.LogUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;

/**
 * 订单Service
 * Created by atlas on 15/9/25.
 * Email:atlas.tufei@gmail.com
 */
public class OrderService {
    /**
     * 删除订单
     *
     * @param context
     * @param id
     */
    public static void orderDel(final Object viewTag, final Context context, int id) {
        HashMap<String, String> params = new HashMap<>(1);
        params.put("id", String.valueOf(id));
        ApiUtils.post(context, URLConstants.ORDER_DEL,
                params,
                BaseResult.class, new Response.Listener<BaseResult>() {
                    @Override
                    public void onResponse(BaseResult response) {
                        if (O2OUtils.checkResponse(context, response)) {
                            OrderEvent orderEvent = new OrderEvent(viewTag);
                            EventBus.getDefault().post(orderEvent);
                        }
                    }
                });
    }

    public static void cancelOrder(final FragmentActivity context, OrderInfo orderInfo, Response.Listener response, Response.ErrorListener errorListener) {
        if (orderInfo.status >= 102 ) {

            // 请电话联系商家取消订单，商家电话XXXXX
            final String tel = orderInfo.sellerTel.replace("-", "");
            AlertDialog dialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT)
                    .setTitle(R.string.order_d_cancel_order)
                    .setMessage(context.getString(R.string.order_cancel_contact_business, tel))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    try {
                        IntentUtils.dial(context, tel);
                    } catch (Exception e) {
                        LogUtils.e("dial error", e);
                    }
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
            dialog.show();
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(context)) {
            ToastUtils.show(context, R.string.loading_err_net);
            return;
        }
        String reason = "";

        CustomDialogFragment.show(context.getSupportFragmentManager(), R.string.loading, context.getLocalClassName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(orderInfo.id));
        params.put("cancelRemark", reason);
        ApiUtils.post(context, URLConstants.ORDER_CANCEL,
                params,
                OrderResult.class, response, errorListener);
    }

    public static void confirmOrder(FragmentActivity context, OrderInfo orderInfo, Response.Listener response, Response.ErrorListener errorListener) {

        if (!NetworkUtils.isNetworkAvaiable(context)) {
            ToastUtils.show(context, R.string.loading_err_net);
            return;
        }

        CustomDialogFragment.show(context.getSupportFragmentManager(), R.string.loading, context.getLocalClassName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(orderInfo.id));
        ApiUtils.post(context, URLConstants.ORDER_CONFIRM,
                params,
                OrderResult.class, response, errorListener);
    }


}
