package com.yizan.community.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.InitInfo;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.event.OrderEvent;
import com.fanwe.seallibrary.model.req.WeixinPayRequest;
import com.fanwe.seallibrary.model.result.AliPayResult;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.OrderResult;
import com.fanwe.seallibrary.model.result.PayLogResult;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.adapter.OrderGoodListAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.NumFormat;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.utils.OrderService;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.IntentUtils;
import com.zongyou.library.util.BitmapUtils;
import com.zongyou.library.util.LogUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.HashMap;

/**
 * 订单详情Activity
 */
public class OrderDetailActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {
    private int mOrderId;
    private OrderInfo mOrderInfo;
    private OrderGoodListAdapter mOrderGoodListAdapter;
    private String mDefaultPayWay;
    private boolean mAutoPay;
    private UpdateBroadCastReceiver mUpdateBroadCastReceiver;
    //    private NetworkImageView mFlowImage;
    private ImageView mFlowImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        setTitleListener(this);
        mDefaultPayWay = this.getIntent().getStringExtra(Constants.EXTRA_PAY);
        mOrderId = this.getIntent().getIntExtra(Constants.EXTRA_DATA, 0);
        mAutoPay = this.getIntent().getBooleanExtra(Constants.EXTRA_AUTO, false);
        if (mOrderId == 0) {
            finish();
            return;
        }


        loadData();
        registerBoradcastReceiver();
    }


    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_order_detail);
    }

    private void registerBoradcastReceiver() {
        if (mUpdateBroadCastReceiver == null) {
            mUpdateBroadCastReceiver = new UpdateBroadCastReceiver();
        }
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constants.ACTION_NAME);
        registerReceiver(mUpdateBroadCastReceiver, myIntentFilter);
    }


    @Override
    protected void onDestroy() {
        if (mUpdateBroadCastReceiver != null) {
            unregisterReceiver(mUpdateBroadCastReceiver);
        }
        unregisterPayBroadCast();
        super.onDestroy();

    }

    private void loadData() {

        // 加载数据
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.loading_err_net);
            return;
        }
        CustomDialogFragment.show(this.getSupportFragmentManager(), R.string.loading, this.getLocalClassName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(mOrderId));
        ApiUtils.post(this, URLConstants.ORDER_DETAIL,
                params,
                OrderResult.class, responseListener(), errorListener());
    }

    private Response.Listener<OrderResult> responseListener() {
        return new Response.Listener<OrderResult>() {
            @Override
            public void onResponse(final OrderResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(getApplicationContext(), response)) {
                    try {
                        initViewData(response.data);
                    } catch (Exception e) {
                        LogUtils.e("order", e);
                        ToastUtils.show(OrderDetailActivity.this, R.string.msg_error_array);
                    }
                }
            }
        };
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(getApplicationContext(), R.string.loading_err_nor);
                CustomDialogFragment.dismissDialog();
            }
        };
    }

    private void initViewData(OrderInfo orderInfo) {
        if (orderInfo == null) {
            return;
        }
        mOrderInfo = orderInfo;
        initOrderFlowImage();

        mViewFinder.setText(R.id.tv_order_sn, mOrderInfo.sn);
        Log.e("订单状态", "--" + mOrderInfo.orderStatusStr);

        mViewFinder.setText(R.id.tv_order_state, getString(R.string.msg_order_detail) + mOrderInfo.orderStatusStr);
        mViewFinder.setText(R.id.tv_order_time, mOrderInfo.createTime);
        mViewFinder.setText(R.id.tv_user_name, mOrderInfo.name);
        mViewFinder.setText(R.id.tv_user_tel, mOrderInfo.mobile);
        mViewFinder.setText(R.id.tv_addr, mOrderInfo.address);
        mViewFinder.setText(R.id.tv_deliver_time, mOrderInfo.appTime);
//        mViewFinder.setText(R.id.tv_deliver_way, TextUtils.isEmpty(mOrderInfo.freType) ? "暂无" : mOrderInfo.freType);
        mViewFinder.setText(R.id.tv_remark, TextUtils.isEmpty(mOrderInfo.buyRemark) ? getResources().getString(R.string.msg_not_available) : mOrderInfo.buyRemark);
        mViewFinder.setText(R.id.tv_seller_name, mOrderInfo.sellerName);

        mViewFinder.setText(R.id.tv_deliver_man, TextUtils.isEmpty(mOrderInfo.staffName) ? getResources().getString(R.string.msg_not_available) : mOrderInfo.staffName);
        mViewFinder.setText(R.id.tv_pay_way, TextUtils.isEmpty(mOrderInfo.payType) ? getResources().getString(R.string.msg_not_available) : mOrderInfo.payType);
        mViewFinder.setText(R.id.tv_carriage, String.format(getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(mOrderInfo.freight)));
        mViewFinder.setText(R.id.tv_total, String.format(getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(mOrderInfo.totalFee)));
        mViewFinder.onClick(R.id.ll_seller_container, this);

        if (mOrderInfo.orderType == 2) {
            mViewFinder.setText(R.id.tv_deliver_title, R.string.msg_server_staff);
            mViewFinder.find(R.id.ll_deliver_price).setVisibility(View.GONE);
            mViewFinder.find(R.id.line_deliver_price).setVisibility(View.GONE);
            mViewFinder.find(R.id.ll_deliver_time).setVisibility(View.GONE);
        }

//        initPayWay();
        initGoodsList();
//        initCancelOrder();
//        initRefundStatus();
        initButtomStatus();

        if (mAutoPay) {
            mAutoPay = false;
            mViewFinder.find(R.id.tv_right_button).performClick();
        }
        initOrderQuick();
    }

    private void initOrderFlowImage() {
        if (mOrderInfo == null || TextUtils.isEmpty(mOrderInfo.orderStatusStr)) {
            return;
        }
        mFlowImage = mViewFinder.find(R.id.order_flow_image);
        ImageRequest imageRequest = new ImageRequest(
                mOrderInfo.statusFlowImage,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        try {
                            int width = mFlowImage.getWidth();
                            int bmpWidth = response.getWidth();
                            if (bmpWidth < width) {
                                mFlowImage.setMinimumHeight(response.getHeight() * width / bmpWidth);
                                mFlowImage.setImageBitmap(response);
                            } else {
                                mFlowImage.setImageBitmap(BitmapUtils.zoomBitmap(response, width, response.getHeight() * width / bmpWidth));
                            }


                        } catch (Exception e) {

                        }
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestManager.getRequestQueue().add(imageRequest);

//        if (!TextUtils.isEmpty(mOrderInfo.statusFlowImage))
//            mFlowImage.setImageUrl(mOrderInfo.statusFlowImage, RequestManager.getImageLoader());
    }

    /**
     * 催单
     */
    private void initOrderQuick() {
        if (mOrderInfo.isCanReminder) {
            mViewFinder.find(R.id.order_quik).setVisibility(View.VISIBLE);
            mViewFinder.find(R.id.order_quik).setOnClickListener(this);

        } else {
            mViewFinder.find(R.id.order_quik).setVisibility(View.GONE);
        }
    }

    private void initGoodsList() {
        ListView listView = mViewFinder.find(R.id.lv_goods);
        mOrderGoodListAdapter = new OrderGoodListAdapter(this, mOrderInfo);
        listView.setAdapter(mOrderGoodListAdapter);
    }

    private void initButtomStatus() {
        // left button
        mViewFinder.find(R.id.tv_left_button).setVisibility(View.VISIBLE);
        if (mOrderInfo.isCanCancel) {
            mViewFinder.setText(R.id.tv_left_button, R.string.msg_order_cancel);
        } else if (mOrderInfo.isCanRate) {
            mViewFinder.setText(R.id.tv_left_button, R.string.msg_go_evaluate);
        } else if (mOrderInfo.isCanDelete) {
            mViewFinder.setText(R.id.tv_left_button, R.string.msg_order_delete);
        } else {
            mViewFinder.find(R.id.tv_left_button).setVisibility(View.GONE);
        }
        // right button
        if (mOrderInfo.isCanPay) {
            mViewFinder.setText(R.id.tv_right_button, R.string.msg_go_pay);

        } else if (mOrderInfo.isCanConfirm) {
            mViewFinder.setText(R.id.tv_right_button, R.string.msg_affirm_goods);
        } else {
            mViewFinder.setText(R.id.tv_right_button, R.string.msg_go_stroll);
        }
        mViewFinder.onClick(R.id.tv_right_button, this);
        mViewFinder.onClick(R.id.tv_left_button, this);
        mViewFinder.onClick(R.id.rl_call_customer, this);
        mViewFinder.onClick(R.id.rl_call_seller, this);
    }

    private void onLeftButtonClick() {
        if (mOrderInfo.isCanCancel) {
            OrderService.cancelOrder(this, mOrderInfo, responseListener(), errorListener());
        } else if (mOrderInfo.isCanRate) {
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra(Constants.EXTRA_DATA, mOrderInfo);
            startActivityForResult(intent, AddCommentActivity.REQUEST_CODE);
        } else if (mOrderInfo.isCanDelete) {
            orderDel();
        }
    }


    private void onRightButtonClick() {
        if (mOrderInfo.isCanPay) {
            if (TextUtils.isEmpty(mDefaultPayWay)) {
                gotoPayActivity();
            } else {
                buyGoods(mDefaultPayWay);
            }

        } else if (mOrderInfo.isCanConfirm) {
            OrderService.confirmOrder(this, mOrderInfo, responseListener(), errorListener());
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        if (mOrderInfo == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.tv_left_button:
                onLeftButtonClick();
                break;
            case R.id.tv_right_button:
                onRightButtonClick();
                break;
            case R.id.rl_call_customer:
                try {
                    InitInfo initInfo = PreferenceUtils.getObject(getApplicationContext(), InitInfo.class);
                    String tel = initInfo.serviceTel.replace("-", "");
                    IntentUtils.dial(OrderDetailActivity.this, tel);
                } catch (Exception e) {
                    LogUtils.e("dial", e);
                }
                break;
            case R.id.rl_call_seller:
                try {
                    String tel = mOrderInfo.sellerTel.replace("-", "");
                    IntentUtils.dial(OrderDetailActivity.this, tel);
                } catch (Exception e) {
                    LogUtils.e("dial", e);
                }
                break;
            case R.id.order_quik:
                // 15/11/14  催单
                reminder();
                break;
            case R.id.ll_seller_container:
                if (mOrderInfo.orderType == 2) {
                    Intent intent = new Intent(this, SellerServicesActivity.class);
                    intent.putExtra(Constants.EXTRA_DATA, mOrderInfo.sellerId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, SellerGoodsActivity.class);
                    intent.putExtra(Constants.EXTRA_DATA, mOrderInfo.sellerId);
                    startActivity(intent);
                }
                break;

        }
    }

    private void reminder() {
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, OrderDetailActivity.class.getName());
        HashMap<String, String> data = new HashMap<String, String>(1);
        data.put("id", String.valueOf(mOrderInfo.id));
        ApiUtils.post(this, URLConstants.ORDER_REMINDER,
                data,
                OrderResult.class, new Response.Listener<OrderResult>() {
                    @Override
                    public void onResponse(final OrderResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getApplicationContext(), response)) {
                            ToastUtils.show(OrderDetailActivity.this, R.string.msg_success_reminder);
                            try {
                                initViewData(response.data);
                            } catch (Exception e) {
                                LogUtils.e("order", e);
                                ToastUtils.show(OrderDetailActivity.this, R.string.loading_err_nor);
                            }
                        }
                    }
                }, errorListener());
    }

    private void gotoPayActivity() {
        Intent intent = new Intent(this, PayWayActivity.class);
        intent.putExtra(Constants.EXTRA_DATA, mOrderInfo);
        startActivityForResult(intent, PayWayActivity.REQUEST_CODE);
    }

    private void buyGoods(final String payWay) {
        if (TextUtils.isEmpty(payWay)) {
            ToastUtils.show(OrderDetailActivity.this, R.string.msg_select_pay_way);
            return;
        }

        IWXAPI iwxapi = WXAPIFactory.createWXAPI(OrderDetailActivity.this, BuildConfig.WX_APP_ID);
        if (Constants.PAY_TYPE_WEICHAT.equals(payWay) && !iwxapi.isWXAppInstalled()) {
            ToastUtils.show(getApplicationContext(), getString(R.string.msg_weixin_un_installed));
            return;
        }

        if (!NetworkUtils.isNetworkAvaiable(OrderDetailActivity.this)) {
            ToastUtils.show(OrderDetailActivity.this, R.string.msg_error_network);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, OrderDetailActivity.class.getName());
        HashMap<String, String> data = new HashMap<String, String>(2);
        data.put("id", String.valueOf(mOrderInfo.id));
        data.put("payment", payWay);
        ApiUtils.post(OrderDetailActivity.this, URLConstants.ORDER_PAY, data, PayLogResult.class, new Response.Listener<PayLogResult>() {

            @Override
            public void onResponse(PayLogResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(OrderDetailActivity.this, response)) {
                    if (null != response.data && null != response.data.payRequest)
                        if (Constants.PAY_TYPE_WEICHAT.equals(payWay)) {
                            //微信支付
                            weichatPay(response.data.payRequest);
                        } else {
                            //  支付宝支付
                            try {
                                if (!TextUtils.isEmpty(response.data.payRequest.packages)) {
                                    alipay(OrderDetailActivity.this, mHandler, response.data.payRequest.packages);
                                }
                            } catch (Exception e) {
                                ToastUtils.show(OrderDetailActivity.this, R.string.msg_pay_error);
                            }
                        }
                    else {
                        ToastUtils.show(OrderDetailActivity.this, R.string.msg_pay_error);
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(OrderDetailActivity.this, R.string.msg_pay_error);
            }
        });

    }


    IWXAPI mIWXAPI;

    /**
     * 微信支付
     * @param info
     */
    private void weichatPay(WeixinPayRequest info) {
        mIWXAPI = WXAPIFactory.createWXAPI(this, BuildConfig.WX_APP_ID);
        mIWXAPI.registerApp(BuildConfig.WX_APP_ID);
        registerPayBroadCast();
        PayReq request = new PayReq();
        request.appId = BuildConfig.WX_APP_ID;
        request.partnerId = info.partnerid;
        request.prepayId = info.prepayid;
        request.packageValue = info.packages;
        request.nonceStr = info.noncestr;
        request.timeStamp = info.timestamp;
        request.sign = info.sign;
        mIWXAPI.registerApp(BuildConfig.WX_APP_ID);
        mIWXAPI.sendReq(request);

        // 暂时解决方法
//        mbNeedReloadForWx = true;
    }

    /**
     * 支付宝支付
     *
     * @param activity
     * @param handler
     * @param orderInfo
     */
    public static void alipay(final Activity activity, final Handler handler, final String orderInfo) {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(activity);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(orderInfo);
                Message msg = new Message();
                msg.what = AliPayResult.ALIPAY_FLAG;
                msg.obj = new AliPayResult(result);
                handler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public void orderDel() {
        if (mOrderInfo == null) {
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.loading_err_net);
            return;
        }
        CustomDialogFragment.show(this.getSupportFragmentManager(), R.string.loading, this.getLocalClassName());
        HashMap<String, String> params = new HashMap<>(1);
        params.put("id", String.valueOf(mOrderInfo.id));
        ApiUtils.post(this, URLConstants.ORDER_DEL,
                params,
                BaseResult.class, new Response.Listener<BaseResult>() {
                    @Override
                    public void onResponse(BaseResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(OrderDetailActivity.this, response)) {
                            OrderEvent orderEvent = new OrderEvent(null);
                            EventBus.getDefault().post(orderEvent);
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    finishActivity();
                                }
                            }, 200);

                        }
                    }
                }, errorListener());
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == AliPayResult.ALIPAY_FLAG) {
                AliPayResult aliPayResult = (AliPayResult) msg.obj;

                if (AliPayResult.ALI_PAY_RESULT_OK.equals(aliPayResult.getResultStatus())) {
                    ToastUtils.show(OrderDetailActivity.this, R.string.msg_pay_success);
                    Intent intents = new Intent(OrderDetailActivity.this, PayResultActivity.class);
                    intents.putExtra(Constants.EXTRA_DATA, mOrderInfo.sn);
                    intents.putExtra(Constants.EXTRA_PAY, true);
                    startActivity(intents);
                } else {
                    ToastUtils.show(OrderDetailActivity.this, aliPayResult.getResult());
                }
                loadData();
            }
        }
    };

    PayBroadCastReceiver mPayBroadCastReceiver;

    private void registerPayBroadCast() {
        if (mPayBroadCastReceiver != null) {
            return;
        }
        // 生成广播处理
        mPayBroadCastReceiver = new PayBroadCastReceiver();
        // 实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_PAY_RESULT);
        // 注册广播
        registerReceiver(mPayBroadCastReceiver, intentFilter);
    }

    private void unregisterPayBroadCast() {
        if (mPayBroadCastReceiver != null) {
            unregisterReceiver(mPayBroadCastReceiver);
        }
    }

    class UpdateBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_NAME)) {
                loadData();
            }
        }
    }

    class PayBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final int code = intent.getIntExtra("code", BaseResp.ErrCode.ERR_COMM);
            Log.e("WXPay", "broadcast receiver: " + code + "");
            switch (code) {
                case BaseResp.ErrCode.ERR_OK:
                    ToastUtils.show(OrderDetailActivity.this, R.string.msg_pay_success);
                    Intent intents = new Intent(OrderDetailActivity.this, PayResultActivity.class);
                    intents.putExtra(Constants.EXTRA_DATA, mOrderInfo.sn);
                    intents.putExtra(Constants.EXTRA_PAY, true);
                    startActivity(intents);
                    loadData();
                    break;
                case BaseResp.ErrCode.ERR_COMM:
                    ToastUtils.show(OrderDetailActivity.this, R.string.msg_pay_error);
                    Intent intents1 = new Intent(OrderDetailActivity.this, PayResultActivity.class);
                    intents1.putExtra(Constants.EXTRA_DATA, mOrderInfo.sn);
                    intents1.putExtra(Constants.EXTRA_PAY, false);
                    startActivity(intents1);
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    ToastUtils.show(OrderDetailActivity.this, R.string.msg_pay_error);
                    break;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case AddCommentActivity.REQUEST_CODE:
                if (data != null) {
                    initViewData((OrderInfo) data.getSerializableExtra(Constants.EXTRA_DATA));
                }
                break;
            case PayWayActivity.REQUEST_CODE:
                if (data != null) {
//                    buyGoods(((PaymentInfo) data.getSerializableExtra(Constants.EXTRA_DATA)).code);
                    loadData();
                }
                break;
        }
    }


}
