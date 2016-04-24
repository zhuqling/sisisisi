package com.yizan.community.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.InitInfo;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.PaymentInfo;
import com.fanwe.seallibrary.model.req.WeixinPayRequest;
import com.fanwe.seallibrary.model.result.AliPayResult;
import com.fanwe.seallibrary.model.result.PayLogResult;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.adapter.PayWayListAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;


public class PayWayActivity extends BaseActivity implements View.OnClickListener, BaseActivity.TitleListener {
    private PayWayListAdapter mPayWayListAdapter;
    private OrderInfo mOrderInfo;
    private UpdateBroadCastReceiver mUpdateBroadCastReceiver;
    public final static int REQUEST_CODE = 0x201;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_way);
        setTitleListener(this);
        mOrderInfo = (OrderInfo)getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        mViewFinder.onClick(R.id.btn_pay, this);
        initView();
        registerBoradcastReceiver();
    }

    private void initView(){
        InitInfo initInfo = PreferenceUtils.getObject(this, InitInfo.class);
        if (null != initInfo && !ArraysUtils.isEmpty(initInfo.payments)) {
            ListView listView = mViewFinder.find(R.id.lv_list);
            mPayWayListAdapter = new PayWayListAdapter(this, initInfo.payments);
            listView.setAdapter(mPayWayListAdapter);
            mPayWayListAdapter.selectItem(0);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mPayWayListAdapter.selectItem(position);
                }
            });

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pay:
                PaymentInfo paymentInfo = mPayWayListAdapter.getSelItem();
                if(paymentInfo == null){
                    ToastUtils.show(this, R.string.msg_select_pay_way);
                    return;
                }
                buyGoods(paymentInfo.code);
                break;
        }
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_pay_way);
    }

    private void buyGoods(final String payWay) {
        if (TextUtils.isEmpty(payWay)) {
            ToastUtils.show(this, R.string.msg_select_pay_way);
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
            return;
        }
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(PayWayActivity.this, BuildConfig.WX_APP_ID);
        if (Constants.PAY_TYPE_WEICHAT.equals(payWay) && !iwxapi.isWXAppInstalled()) {
            ToastUtils.show(getApplicationContext(), getString(R.string.msg_weixin_un_installed));
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, OrderDetailActivity.class.getName());
        HashMap<String, String> data = new HashMap<String, String>(2);
        data.put("id", String.valueOf(mOrderInfo.id));
        data.put("payment", payWay);
        ApiUtils.post(this, URLConstants.ORDER_PAY, data, PayLogResult.class, new Response.Listener<PayLogResult>() {

            @Override
            public void onResponse(PayLogResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(PayWayActivity.this, response)) {
                    if (null != response.data && null != response.data.payRequest)
                        if (Constants.PAY_TYPE_WEICHAT.equals(payWay))
                            weichatPay(response.data.payRequest);
                        else {
                            //  支付宝支付
                            try {
                                if (!TextUtils.isEmpty(response.data.payRequest.packages)) {
                                    alipay(PayWayActivity.this, mHandler, response.data.payRequest.packages);
                                }
                            } catch (Exception e) {
                                ToastUtils.show(PayWayActivity.this, R.string.msg_pay_error);
                            }
                        }
                    else {
                        ToastUtils.show(PayWayActivity.this, R.string.msg_pay_error);
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(PayWayActivity.this, R.string.msg_pay_error);
            }
        });

    }

    IWXAPI mIWXAPI;

    private void weichatPay(WeixinPayRequest info) {

        mIWXAPI = WXAPIFactory.createWXAPI(this, BuildConfig.WX_APP_ID);
        mIWXAPI.registerApp(BuildConfig.WX_APP_ID);
//        if(mIWXAPI.isWXAppInstalled()){
//
//        }
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
                closeSelf();
            }
        }
    }

    private void registerBoradcastReceiver() {
        if (mUpdateBroadCastReceiver == null) {
            mUpdateBroadCastReceiver = new UpdateBroadCastReceiver();
        }
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constants.ACTION_NAME);
        registerReceiver(mUpdateBroadCastReceiver, myIntentFilter);
    }

    class PayBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final int code = intent.getIntExtra("code", BaseResp.ErrCode.ERR_COMM);
            Log.e("WXPay", "broadcast receiver: " + code + "");
            switch (code) {
                case BaseResp.ErrCode.ERR_OK:
                    ToastUtils.show(PayWayActivity.this, R.string.msg_pay_success);
                    Intent intents = new Intent(PayWayActivity.this, PayResultActivity.class);
                    intents.putExtra(Constants.EXTRA_DATA, mOrderInfo.sn);
                    intents.putExtra(Constants.EXTRA_PAY, true);
                    startActivity(intents);
                    closeSelf();
                    break;
                case BaseResp.ErrCode.ERR_COMM:
                    ToastUtils.show(PayWayActivity.this, R.string.msg_pay_error);
                    Intent intents1 = new Intent(PayWayActivity.this, PayResultActivity.class);
                    intents1.putExtra(Constants.EXTRA_DATA, mOrderInfo.sn);
                    intents1.putExtra(Constants.EXTRA_PAY, false);
                    startActivity(intents1);
                    closeSelf();
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    ToastUtils.show(PayWayActivity.this, R.string.msg_pay_error);
                    break;
            }
        }

    }

    private void closeSelf(){
        setResult(Activity.RESULT_OK);
    }
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == AliPayResult.ALIPAY_FLAG) {
                AliPayResult aliPayResult = (AliPayResult) msg.obj;

                if (AliPayResult.ALI_PAY_RESULT_OK.equals(aliPayResult.getResultStatus())) {
                    ToastUtils.show(PayWayActivity.this, R.string.msg_pay_success);
                    Intent intents = new Intent(PayWayActivity.this, PayResultActivity.class);
                    intents.putExtra(Constants.EXTRA_DATA, mOrderInfo.sn);
                    intents.putExtra(Constants.EXTRA_PAY, true);
                    startActivity(intents);
                } else {
                    ToastUtils.show(PayWayActivity.this, aliPayResult.getResult());
                }
                closeSelf();
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (mUpdateBroadCastReceiver != null) {
            unregisterReceiver(mUpdateBroadCastReceiver);
        }
        unregisterPayBroadCast();
        super.onDestroy();

    }
}

