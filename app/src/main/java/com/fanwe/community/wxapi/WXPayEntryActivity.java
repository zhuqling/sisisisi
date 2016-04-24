package com.fanwe.community.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yizan.community.BuildConfig;
import com.fanwe.seallibrary.comm.Constants;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    public static final String TAG = WXPayEntryActivity.class.getSimpleName();
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, BuildConfig.WX_APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq arg0) {
        Log.e("WXPay", "onReq: call");
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.e("WXPay", "type: " + resp.getType());
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Log.e("WXPay", "broadcast: " + resp.errCode + "");
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_PAY_RESULT);
            // 要发送的内容
            intent.putExtra("code", resp.errCode);
            sendBroadcast(intent);

            finish();
        }

    }

    @Override
    protected void onDestroy() {
        api.unregisterApp();
        super.onDestroy();
    }

}
