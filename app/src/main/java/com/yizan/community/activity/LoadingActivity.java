package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.model.PointInfo;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.yizan.community.R;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.req.InitRequest;
import com.fanwe.seallibrary.model.result.InitResultInfo;
import com.yizan.community.utils.ApiUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

public class LoadingActivity extends Activity implements TencentLocationListener {
    private boolean mInitOk = false;
    private boolean mCountDownOver = false;
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mHandler.removeMessages(0);
            if (!PreferenceUtils.getValue(LoadingActivity.this, Constants.PREFERENCE_CONFIG, false)) {
                ToastUtils.show(LoadingActivity.this, R.string.loading_err_init);
            } else {
                mInitOk = true;
                finishLoading();
            }
        }

    };
    private TencentLocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setBackgroundResource(R.drawable.bg_fullscreen);
        setContentView(iv);
        initLocation();
        loadConfig();

        new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mCountDownOver = true;
                finishLoading();
            }
        }.start();
    }

    private void finishLoading() {
        if (!mInitOk) {
            return;
        }
        if (!mCountDownOver) {
            return;
        }
        if (PreferenceUtils.getValue(LoadingActivity.this, "isfirst",
                true)) {
            startActivity(new Intent(LoadingActivity.this,
                    NewbieGuideActivity.class));
            return;
        } else {
            startActivity(new Intent(LoadingActivity.this,
                    MainActivity.class));
        }
        finish();
    }

    private void initLocation() {
        // init location
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        mLocationManager = TencentLocationManager.getInstance(getApplicationContext());
        final int error = mLocationManager.requestLocationUpdates(request, this);
        switch (error) {
            case 0:
                break;
            default:
                ToastUtils.show(getApplicationContext(), R.string.msg_error_location);
                mLocationManager.removeUpdates(this);
                break;

        }
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        mLocationManager.removeUpdates(this);
        if (TencentLocation.ERROR_OK == i) {

            LocAddressInfo locAddressInfo = new LocAddressInfo();
            locAddressInfo.address = tencentLocation.getAddress()+tencentLocation.getName();
            locAddressInfo.city = tencentLocation.getCity();
            locAddressInfo.province = tencentLocation.getProvince();
            locAddressInfo.streetNo = tencentLocation.getStreetNo();
            locAddressInfo.street = tencentLocation.getStreet();
            locAddressInfo.mapPoint = new PointInfo(tencentLocation.getLatitude(), tencentLocation.getLongitude());

            PreferenceUtils.setObject(getApplicationContext(), locAddressInfo);
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    private void loadConfig() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            mHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }
        ApiUtils.post(this, URLConstants.INIT,
                new InitRequest(this),
                InitResultInfo.class, new Response.Listener<InitResultInfo>() {

                    @Override
                    public void onResponse(final InitResultInfo response) {
                        new Thread(new Runnable() {
                            public void run() {
                                if (null != response && null != response.data) {
                                    PreferenceUtils.setObject(LoadingActivity.this, response.data);
                                    PreferenceUtils.setValue(LoadingActivity.this, Constants.PREFERENCE_CONFIG, true);
                                    PreferenceUtils.setValue(LoadingActivity.this, Constants.TOKEN, response.token);
                                }
                                mHandler.sendEmptyMessageDelayed(0, 1000);
                            }
                        }).start();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                });
    }

}
