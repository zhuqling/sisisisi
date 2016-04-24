
package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.PointInfo;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.AddressResult;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.adapter.AddressAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.ImageSwitcherPopupWindow;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址选择Activity
 * Created by ztl on 2015/9/18.
 */
public class SwitchAddressActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener, TencentLocationListener {
    private static final String TAG = SwitchAddressActivity.class.getName();
    private static final int EDIT_REQUEST_CODE = 0x102;
    private ListView mListView;
    private List<UserAddressInfo> listData = new ArrayList<UserAddressInfo>();
    private AddressAdapter mAdapter;
    private View mEmptyView;
    private boolean mLoadMore;
    private String mLocateFlag;
    private RelativeLayout mRelativeLayoutLocate;
    private PopupWindow popupWindow;
    private ImageSwitcherPopupWindow mMenu;
    private TextView mDelete;
    private int addressId, mPosition;
    private TencentLocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_addr);
        Intent intent = getIntent();
        mLocateFlag = intent.getStringExtra("isLocate");
        initViews();
        setTitleListener(this);

    }

    private void initViews() {

        mRelativeLayoutLocate = (RelativeLayout) findViewById(R.id.switch_locate);
        mRelativeLayoutLocate.setOnClickListener(this);
        if (("true").equals(mLocateFlag)) {
            mRelativeLayoutLocate.setVisibility(View.VISIBLE);
        } else {
            mRelativeLayoutLocate.setVisibility(View.GONE);
        }

        mListView = (ListView) findViewById(R.id.switch_addr_list);
        mAdapter = new AddressAdapter(SwitchAddressActivity.this, listData);
        mEmptyView = findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!mLoadMore && firstVisibleItem + visibleItemCount >= totalItemCount && mAdapter.getCount() >= Constants.PAGE_SIZE && mAdapter.getCount() % Constants.PAGE_SIZE == 0) {
                    getAddrList(false);

                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                final UserAddressInfo info = listData.get(position);
//                Intent intent = new Intent();
//                if (("true").equals(isLocate) || ("car").equals(isLocate)) {
//                    //选择
//                    PreferenceUtils.setObject(SwitchAddressActivity.this, info);
//                    intent.putExtra(Constants.EXTRA_DATA,info);
//                    setResult(Activity.RESULT_OK,intent);
//                    finishActivity();
//                } else if (("false").equals(isLocate)) {
//                    //编辑
//
//                    Bundle b = new Bundle();
//                    b.putString("detail", info.detailAddress);
//                    b.putString("mapPoint", info.mapPoint.toString());
//                    b.putString("doorplate", info.doorplate);
//                    b.putString("name", info.name);
//                    b.putString("mobile", info.mobile);
//                    b.putInt("id", info.id);
//                    intent.putExtras(b);
//                    addressEdit(intent);
//                }
                mPosition = position;
                switch (mLocateFlag) {
                    case "my":
                        mMenu = new ImageSwitcherPopupWindow(SwitchAddressActivity.this, SwitchAddressActivity.this, TAG, true);
                        mMenu.setMenuText(getString(R.string.msg_setting_default), getString(R.string.edit), getString(R.string.msg_delete));
                        mMenu.show(getWindow().getDecorView());
                        break;
                    case "true":
                        PreferenceUtils.setObject(getApplicationContext(), listData.get(mPosition));
                    case "false":
                        Intent intent = new Intent();
                        intent.putExtra(Constants.EXTRA_DATA, listData.get(mPosition));
                        setResult(Activity.RESULT_OK, intent);
                        finishActivity();
                        break;
                }
            }
        });
    }


    private void deleteAddr(int id) {
        if (!NetworkUtils.isNetworkAvaiable(SwitchAddressActivity.this)) {
            ToastUtils.show(SwitchAddressActivity.this, R.string.msg_error_network);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, SwitchAddressActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(id));
        ApiUtils.post(SwitchAddressActivity.this, URLConstants.USERADDRESSDELETE, data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(SwitchAddressActivity.this, response)) {
                    ToastUtils.show(SwitchAddressActivity.this, response.msg);
                    getAddrList(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(SwitchAddressActivity.this, R.string.addr_delete_msg_error);
            }
        });
    }

    private void setDefaultAddr(int id) {
        if (!NetworkUtils.isNetworkAvaiable(SwitchAddressActivity.this)) {
            ToastUtils.show(SwitchAddressActivity.this, R.string.msg_error_network);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, SwitchAddressActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(id));
        ApiUtils.post(SwitchAddressActivity.this, URLConstants.USERADDRESSSETDEFAULT, data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(SwitchAddressActivity.this, response)) {
                    if (mLocateFlag.equals("my")) {
                        ToastUtils.show(SwitchAddressActivity.this, response.msg);
                    } else {
                        ToastUtils.show(SwitchAddressActivity.this, SwitchAddressActivity.this.getString(R.string.choose_address_succeed));
                    }
                    getAddrList(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(SwitchAddressActivity.this, R.string.addr_delete_msg_error);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (O2OUtils.isLogin(getApplicationContext()))
            getAddrList(true);
    }

    private void getAddrList(final boolean isRefresh) {
        if (mLoadMore)
            return;
        if (!NetworkUtils.isNetworkAvaiable(SwitchAddressActivity.this)) {
            ToastUtils.show(SwitchAddressActivity.this, R.string.msg_error_network);
            return;
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, SwitchAddressActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        ApiUtils.post(this, URLConstants.USERADDRESSLISTS, data, AddressResult.class, new Response.Listener<AddressResult>() {
            @Override
            public void onResponse(AddressResult response) {
                if (isRefresh)
                    listData.clear();
                if (O2OUtils.checkResponse(SwitchAddressActivity.this, response)) {
                    if (!ArraysUtils.isEmpty(response.data))
                        listData.addAll(response.data);
                }
                mAdapter.notifyDataSetChanged();
                CustomDialogFragment.dismissDialog();
                mLoadMore = false;
                //更新本地地址
                UserInfo userinfo = PreferenceUtils.getObject(SwitchAddressActivity.this, UserInfo.class);
                if (userinfo != null) {
                    userinfo.address = listData;
                    PreferenceUtils.setObject(SwitchAddressActivity.this, userinfo);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                mLoadMore = false;
            }
        });
    }

    private void selCurrAddr(){
        PreferenceUtils.setObject(getApplicationContext(), PreferenceUtils.getObject(SwitchAddressActivity.this, LocAddressInfo.class).toUserAddr());
        setResult(Activity.RESULT_OK);
        finishActivity();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_locate:
                startLocation();
                break;
            case R.id.item1://设为默认
                addressId = listData.get(mPosition).id;
                setDefaultAddr(addressId);
                mMenu.dismiss();
                break;
            case R.id.item3://删除
                addressId = listData.get(mPosition).id;
                deleteAddr(addressId);
                mMenu.dismiss();
                break;
            case R.id.item2://编辑
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_DATA, listData.get(mPosition));
                intent.setClass(this, BuildConfig.ADDRESS_SIMPLE ? AddressAddActivity.class : AddressAddCommonActivity.class);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
                mMenu.dismiss();
                break;
        }
    }


    private void startLocation() {
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_location, TAG);
        mLocationManager = TencentLocationManager.getInstance(this);
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        //定位请求设置
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(5000);
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        request.setAllowCache(false);
        //开始定位
        mLocationManager.requestLocationUpdates(request, this);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        if (("true").equals(mLocateFlag)) {
            title.setText(R.string.switch_addr);
        } else if (("false").equals(mLocateFlag)) {
            title.setText(getResources().getString(R.string.addr_management_title));
        }

        ((TextView) right).setText(R.string.add);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (O2OUtils.turnLogin(getApplicationContext()))
                    return;
                addressEdit(new Intent());
            }
        });
    }

    /**
     * 地址编辑
     */
    private void addressEdit(Intent intent) {
        intent.setClass(this, BuildConfig.ADDRESS_SIMPLE ? AddressAddActivity.class : AddressAddCommonActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int error, String reason) {
        if (error == TencentLocation.ERROR_OK) {
            LocAddressInfo locAddressInfo = new LocAddressInfo();
            locAddressInfo.address = tencentLocation.getAddress()+tencentLocation.getName();
            locAddressInfo.city = tencentLocation.getCity();
            locAddressInfo.province = tencentLocation.getProvince();
            locAddressInfo.streetNo = tencentLocation.getStreetNo();
            locAddressInfo.street = tencentLocation.getStreet();
            locAddressInfo.mapPoint = new PointInfo(tencentLocation.getLatitude(), tencentLocation.getLongitude());
            PreferenceUtils.setObject(SwitchAddressActivity.this, locAddressInfo);
            mLocationManager.removeUpdates(this);
            selCurrAddr();
        } else {
            ToastUtils.show(getApplicationContext(), R.string.msg_error_location);
            mLocationManager.removeUpdates(this);
        }
        CustomDialogFragment.dismissDialog();
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == EDIT_REQUEST_CODE) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
