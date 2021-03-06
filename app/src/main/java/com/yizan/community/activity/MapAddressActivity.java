package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.PointInfo;
import com.fanwe.seallibrary.model.result.map.GeoCoderResult;
import com.fanwe.seallibrary.model.result.map.POIListResult;
import com.fanwe.seallibrary.model.result.map.POILocation;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.param.SearchParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.lbssearch.object.result.SearchResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.GeoPoint;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.tencentmap.mapsdk.map.MapController;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.OnMapCameraChangeListener;
import com.tencent.tencentmap.mapsdk.map.OverlayItem;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.ToastUtils;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class MapAddressActivity extends BaseActivity implements View.OnClickListener, TencentLocationListener, BaseActivity.TitleListener {
    private static final String TAG = MapAddressActivity.class.getName();
    public static final int REQUEST_CODE = 0x301;
    private EditText mEdit;
    String str;

    //地图
    MapView mapView;
    //地图定位管理
    TencentLocationManager locationManager;
    //地图控制器
    MapController mapController;

    private String region;
    private double latdouble, lngdouble;
    private PointInfo mSelectedPoint = new PointInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_address);
        setTitleListener(this);

        mViewFinder.find(R.id.btn_search).setOnClickListener(this);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getController().setZoom(15);
        mapController = mapView.getController();

        locationManager = TencentLocationManager.getInstance(this);
        locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);

        startLocation();

        mEdit = (EditText) findViewById(R.id.choose_community);

        mapController.setOnMapCameraChangeListener(new OnMapCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                addMarker(cameraPosition.getTarget().getLatitude(), cameraPosition.getTarget().getLongitude());
            }
        });

        mViewFinder.onClick(R.id.btn_save, this);
    }

    private void searchAddressPoiByWebService(String regions, String keyword) {
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, TAG);
        HashMap<String, String> paras = new HashMap<>(2);
        try {
            paras.put("region", URLEncoder.encode(regions,"utf-8"));
            paras.put("keyword", URLEncoder.encode(keyword,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        paras.put("key", BuildConfig.MAP_ID);
        ApiUtils.get(URLConstants.POI_SEARCH, paras, POIListResult.class, new Response.Listener<POIListResult>() {
            @Override
            public void onResponse(POIListResult response) {
                CustomDialogFragment.dismissDialog();
                if (response != null && response.status == 0 && response.count > 0 && !ArraysUtils.isEmpty(response.data)) {
                    mSelectedPoint = new PointInfo(response.data.get(0));
                    final POILocation location = response.data.get(0).location;
                    if (null != location) {
                        mapView.getController().setZoom(18);
                        mapController.animateTo(new GeoPoint((int) (location.lat * 1e6), (int) (location.lng * 1e6)));
                        addMarker(location.lat, location.lng);
                    }
                } else
                    ToastUtils.show(MapAddressActivity.this, R.string.msg_error_unknow_location);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(MapAddressActivity.this, R.string.msg_error_unknow_location);
            }
        });
    }

    /**
     * geo coder搜索地址
     * @param address
     */
    private void searchAddressGeoCoderByWebService(final String address) {
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, TAG);
        HashMap<String, String> paras = new HashMap<>(2);
        try {
            paras.put("address", URLEncoder.encode(address, "utf-8"));
        } catch (UnsupportedEncodingException e) {
        }
        paras.put("key", BuildConfig.MAP_ID);
        ApiUtils.get(URLConstants.MAP_GEOCODER, paras, GeoCoderResult.class, new Response.Listener<GeoCoderResult>() {
            @Override
            public void onResponse(GeoCoderResult response) {
                CustomDialogFragment.dismissDialog();
                if (response != null && response.status == 0 && response.result!=null) {
                    response.result.title=address;
                    mSelectedPoint = new PointInfo(response.result);
                    final POILocation location = response.result.location;
                    if (null != location) {
                        mapView.getController().setZoom(18);
                        mapController.animateTo(new GeoPoint((int) (location.lat * 1e6), (int) (location.lng * 1e6)));
                        addMarker(location.lat, location.lng);
                    }
                } else
                    ToastUtils.show(MapAddressActivity.this, R.string.msg_error_unknow_location);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(MapAddressActivity.this, R.string.msg_error_unknow_location);
            }
        });
    }
    //关键字搜索
    private void searchAddressPoi(String regions, String keyword) {
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, TAG);
        TencentSearch ts = new TencentSearch(this);
        SearchParam param = new SearchParam().keyword(keyword).boundary(new SearchParam.Region().poi(regions)).page_index(1).page_size(1);
        ts.search(param, new HttpResponseListener() {
            @Override
            public void onSuccess(int i, Header[] headers, BaseObject results) {
                CustomDialogFragment.dismissDialog();
                if (results != null) {
                    SearchResultObject list = (SearchResultObject) results;
                    if (!ArraysUtils.isEmpty(list.data)) {
                        mSelectedPoint = new PointInfo();
                        mSelectedPoint.address = list.data.get(0).address;
                        final Location location = list.data.get(0).location;
                        if (null != location) {
                            mSelectedPoint.x = location.lat;
                            mSelectedPoint.y = location.lng;
                            mapView.getController().setZoom(18);
                            mapController.animateTo(new GeoPoint((int) (location.lat * 1e6), (int) (location.lng * 1e6)));
                            addMarker(location.lat, location.lng);
                        }
                    } else
                        ToastUtils.show(MapAddressActivity.this, R.string.msg_error_unknow_location);
                } else
                    ToastUtils.show(MapAddressActivity.this, R.string.msg_error_unknow_location);
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(MapAddressActivity.this, R.string.msg_error_unknow_location);
            }
        });
    }

    private void startLocation() {
        //定位请求设置
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(5000);
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_POI);
        request.setAllowCache(false);
        //开始定位
        locationManager.requestLocationUpdates(request, this);
    }

    //坐标转地址
    private String geo2Address(double lat, double lng) {
        TencentSearch api = new TencentSearch(MapAddressActivity.this);
        Geo2AddressParam param = new Geo2AddressParam().location(new Location().lat((float) lat).lng((float) lng));
        api.geo2address(param, new HttpResponseListener() {
            @Override
            public void onSuccess(int i, Header[] headers, BaseObject baseObject) {
                if (baseObject != null) {
                    Geo2AddressResultObject oj = (Geo2AddressResultObject) baseObject;

                    if (oj.result != null) {
                        String check = oj.result.address;
                        if (check.contains(getResources().getString(R.string.area_text))) {
                            String[] a = check.split(getResources().getString(R.string.area_text));
                            str = a[1];
                        }
                    }
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                ToastUtils.show(MapAddressActivity.this, R.string.msg_error_invalid_location);
            }
        });
        return str;
    }


    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.choose_address);
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int error, String reason) {
        String msg = null;
        if (error == TencentLocation.ERROR_OK) {
            //定位成功
            StringBuilder sb = new StringBuilder();

            latdouble = tencentLocation.getLatitude();
            lngdouble = tencentLocation.getLongitude();
            String r = tencentLocation.getCity();
            String s[] = r.split(getResources().getString(R.string.city_text));
            region = s[0];
            sb.append(getResources().getString(R.string.now_location_text)).append(tencentLocation.getProvince())
                    .append("，").append(tencentLocation.getCity());
            ToastUtils.show(MapAddressActivity.this, sb.toString());
            LatLng latLng1 = new LatLng(tencentLocation.getLatitude(), tencentLocation.getLongitude());
            mapController.animateTo(new GeoPoint((int) (latLng1.getLatitude() * 1e6), (int) (latLng1.getLongitude() * 1e6)));
            addMarker(latdouble, lngdouble);
            locationManager.removeUpdates(this);
        } else {
            //定位失败
            ToastUtils.show(MapAddressActivity.this, reason);
            locationManager.removeUpdates(this);
        }
    }

    //添加标志物
    private void addMarker(final double lats, final double lngs) {
        mapView.clearAllOverlays();
        GeoPoint poi = new GeoPoint((int) (lats * 1e6), (int) (lngs * 1e6));
        Drawable markerImg = getResources().getDrawable(R.drawable.ic_marker);
        OverlayItem marker = new OverlayItem(poi, getResources().getString(R.string.position_text), getResources().getString(R.string.label_text), markerImg);
        mapView.add(marker);
        TencentSearch api = new TencentSearch(this);
        Geo2AddressParam param = new Geo2AddressParam().location(new Location()
                .lat((float) lats).lng((float) lngs));
        api.geo2address(param, new HttpResponseListener() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, BaseObject object) {
                if (object != null) {
                    Geo2AddressResultObject oj = (Geo2AddressResultObject) object;
                    if (oj.result != null) {
                        mSelectedPoint.x = lats;
                        mSelectedPoint.y = lngs;
                        mSelectedPoint.address = oj.result.address;
                        ToastUtils.show(MapAddressActivity.this, mSelectedPoint.address);
                    } else
                        ToastUtils.show(MapAddressActivity.this, R.string.msg_error_address_error);
                } else ToastUtils.show(MapAddressActivity.this, R.string.msg_error_address_error);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
                ToastUtils.show(MapAddressActivity.this, R.string.msg_error_address_error);
            }
        });
    }

    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        String message = "{name=" + name + ", new status=" + status + ", desc="
                + desc + "}";

        if (status == STATUS_DENIED) {
            /* 检测到定位权限被内置或第三方的权限管理或安全软件禁用, 导致当前应用**很可能无法定位**
             * 必要时可对这种情况进行特殊处理, 比如弹出提示或引导
			 */
            Toast.makeText(this, R.string.msg_error_lovation_jurisdiction, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        AppUtils.hideSoftInput(this);
        switch (v.getId()) {
            case R.id.btn_search:
                if (TextUtils.isEmpty(mEdit.getText().toString().trim())) {
                    ToastUtils.show(this, R.string.msg_error_search_no_null);
                    return;
                } else {
//                    searchAddressPoiByWebService(region, mEdit.getText().toString());
                    searchAddressGeoCoderByWebService(mEdit.getText().toString());
                }

                break;
            case R.id.btn_save:
                saveAddress();
                break;
        }
    }

    private void saveAddress() {
        if (mSelectedPoint.x == 0 || mSelectedPoint.y == 0) {// || TextUtils.isEmpty(mSelectedPoint.address)) {
            ToastUtils.show(MapAddressActivity.this, R.string.msg_error_nofound_lovation_hint);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_DATA, mSelectedPoint);
        setResult(Activity.RESULT_OK, intent);
        finishActivity();
    }
}
