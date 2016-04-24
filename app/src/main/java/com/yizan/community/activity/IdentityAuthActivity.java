package com.yizan.community.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.BuildingInfo;
import com.fanwe.seallibrary.model.DistrictAuthInfo;
import com.fanwe.seallibrary.model.DistrictInfo;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.RoomInfo;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.BuildingListResult;
import com.fanwe.seallibrary.model.result.DistrictListResult;
import com.fanwe.seallibrary.model.result.RoomListResult;
import com.yizan.community.R;
import com.yizan.community.adapter.PopAllCatesAdapter;
import com.yizan.community.adapter.PopBuildingListAdapter;
import com.yizan.community.adapter.PopRoomListAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityAuthActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {
    private DistrictInfo mDistrictInfo;
    private BuildingInfo mBuildingInfo;
    private RoomInfo mRoomInfo;
    private Map<Integer, List<BuildingInfo>> mBuildingMap;
    private Map<Integer, List<RoomInfo>> mRoomMap;

    private PopupWindow mBuildingWindow, mRoomWindow;
    private TextView mSelBuildingView, mSelRoomView;

    private DistrictAuthInfo mDistrictAuthInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_auth);
        setTitleListener(this);
        setViewClickListener(R.id.tv_district, this);
        setViewClickListener(R.id.tv_building_num, this);
        setViewClickListener(R.id.tv_room_num, this);
        setViewClickListener(R.id.btn_submit, this);
        mSelBuildingView = mViewFinder.find(R.id.tv_building_num);
        mSelRoomView = mViewFinder.find(R.id.tv_room_num);
        mBuildingMap = new HashMap<>();
        mRoomMap = new HashMap<>();

        initView();

    }

    private void initView() {
        mDistrictAuthInfo = (DistrictAuthInfo)getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        if(mDistrictAuthInfo == null){
            return;
        }
        mDistrictInfo = mDistrictAuthInfo.district;
        mBuildingInfo = mDistrictAuthInfo.build;
        mRoomInfo = mDistrictAuthInfo.room;

        if(mDistrictInfo != null){
            mViewFinder.setText(R.id.tv_district, mDistrictInfo.name);
        }
        if(mBuildingInfo != null){
            mViewFinder.setText(R.id.tv_building_num, mBuildingInfo.name);
        }
        if(mRoomInfo != null){
            mViewFinder.setText(R.id.tv_room_num, mRoomInfo.roomNum);
        }
        EditText et = mViewFinder.find(R.id.et_name);
        if(!TextUtils.isEmpty(mDistrictAuthInfo.name)) {
            et.setText(mDistrictAuthInfo.name);
        }
        et = mViewFinder.find(R.id.et_tel);
        if(!TextUtils.isEmpty(mDistrictAuthInfo.mobile)) {
            et.setText(mDistrictAuthInfo.mobile);
        }
    }
    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_identity_auth);
    }

    private void initViewData() {
        if (mDistrictInfo == null) {
            return;
        }
        mViewFinder.setText(R.id.tv_district, mDistrictInfo.name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_district:
                Intent intent = new Intent(this, DistrictListActivity.class);
                startActivityForResult(intent, DistrictListActivity.REQUEST_CODE);
                break;
            case R.id.tv_building_num:
                loadBuildingData();
                break;
            case R.id.tv_room_num:
                loadRoomData();
                break;
            case R.id.btn_submit:
                submitData();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case DistrictListActivity.REQUEST_CODE:
                if (data == null) {
                    return;
                }
                DistrictInfo info = (DistrictInfo) data.getSerializableExtra(Constants.EXTRA_DATA);
                if (info != null) {
                    if (mDistrictInfo != null && mDistrictInfo.id != info.id) {
                        resetBuidlingInfo();
                    }
                    mDistrictInfo = info;
                    initViewData();
                }
                break;
        }
    }


    private void showBuildingList() {
        if (mDistrictInfo == null) {
            return;
        }
        final List<BuildingInfo> list = mBuildingMap.get(mDistrictInfo.id);

        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_list_layout, null);
        ListView listView = (ListView) contentView.findViewById(R.id.lv_list);
        PopBuildingListAdapter popAdapter = new PopBuildingListAdapter(getApplicationContext(), list);
        listView.setAdapter(popAdapter);
        contentView.setBackgroundColor(Color.WHITE);
        contentView.setBackgroundResource(R.drawable.style_edt_boder);
        int width = mSelBuildingView.getWidth();
        if(width < 150){
            width = 150;
        }
        mBuildingWindow = new PopupWindow(contentView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mBuildingWindow.setOutsideTouchable(true);
        mBuildingWindow.setFocusable(true);
        mBuildingWindow.setTouchable(true);
        mBuildingWindow.setBackgroundDrawable(new BitmapDrawable());
        int xoffset = 0;
        int yoffset = 2;
        mBuildingWindow.showAsDropDown(mSelBuildingView, xoffset, yoffset);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BuildingInfo item = list.get(position);
                if (mBuildingInfo != null && mBuildingInfo.id != item.id) {
                    resetRoomInfo();
                }
                mBuildingInfo = item;
                mSelBuildingView.setText(item.name);
                mBuildingWindow.dismiss();
            }
        });
        mBuildingWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                selPopState(mSelBuildingView, false);
            }
        });
        selPopState(mSelBuildingView, true);

    }

    private void selPopState(TextView tv, boolean popShow) {
        if (popShow) {
            tv.setTextColor(getResources().getColor(R.color.theme_main_text));
            Drawable arrowDown = getResources().getDrawable(R.drawable.ic_menu_arrow_up);
            arrowDown.setBounds(0, 0, arrowDown.getMinimumWidth(), arrowDown.getMinimumHeight());
            tv.setCompoundDrawables(null, null, arrowDown, null);
        } else {

            tv.setTextColor(getResources().getColor(R.color.theme_black_text));
            Drawable arrowDown = getResources().getDrawable(R.drawable.down_arrow);
            arrowDown.setBounds(0, 0, arrowDown.getMinimumWidth(), arrowDown.getMinimumHeight());
            tv.setCompoundDrawables(null, null, arrowDown, null);
        }
    }

    private void showRoomList() {
        if (mBuildingInfo == null) {
            return;
        }
        final List<RoomInfo> list = mRoomMap.get(mBuildingInfo.id);
        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_list_layout, null);
        ListView listView = (ListView) contentView.findViewById(R.id.lv_list);
        PopRoomListAdapter popAdapter = new PopRoomListAdapter(getApplicationContext(), list);
        listView.setAdapter(popAdapter);
        contentView.setBackgroundColor(Color.WHITE);
        contentView.setBackgroundResource(R.drawable.style_edt_boder);
        int width = mSelRoomView.getWidth();
        mRoomWindow = new PopupWindow(contentView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mRoomWindow.setOutsideTouchable(true);
        mRoomWindow.setFocusable(true);
        mRoomWindow.setTouchable(true);
        mRoomWindow.setBackgroundDrawable(new BitmapDrawable());
        int xoffset = 0;
        int yoffset = 2;
        mRoomWindow.showAsDropDown(mSelRoomView, xoffset, yoffset);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomInfo item = list.get(position);
                mSelRoomView.setText(item.roomNum);
                mRoomWindow.dismiss();
                mRoomInfo = item;
            }
        });
        mRoomWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                selPopState(mSelRoomView, false);
            }
        });
        selPopState(mSelRoomView, true);
    }

    private void loadBuildingData() {
        if (mDistrictInfo == null) {
            ToastUtils.show(this, R.string.msg_select_district);
            return;
        }
        if (mBuildingMap.containsKey(mDistrictInfo.id)) {
            showBuildingList();
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.loading, getClass().getName());
        Map<String, String> data = new HashMap<>();
        data.put("villagesid", String.valueOf(mDistrictInfo.id));
        ApiUtils.post(this,
                URLConstants.DISTRICT_BUILDING, data, BuildingListResult.class, new Response.Listener<BuildingListResult>() {
                    @Override
                    public void onResponse(BuildingListResult response) {

                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {
                            if (ArraysUtils.isEmpty(response.data)) {
                                ToastUtils.show(getApplicationContext(), R.string.footer_load_end);
                            } else {
                                mBuildingMap.put(mDistrictInfo.id, response.data);
                                showBuildingList();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(getActivity(), R.string.msg_error);
                        CustomDialogFragment.dismissDialog();
                    }
                });

    }

    private void loadRoomData() {
        if (mBuildingInfo == null) {
            ToastUtils.show(this, R.string.msg_select_building);
            return;
        }
        if (mRoomMap.containsKey(mBuildingInfo.id)) {
            showRoomList();
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
        }
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.loading, getClass().getName());
        Map<String, String> data = new HashMap<>();
        data.put("buildingid", String.valueOf(mBuildingInfo.id));
        ApiUtils.post(this,
                URLConstants.DISTRICT_ROOMS, data, RoomListResult.class, new Response.Listener<RoomListResult>() {
                    @Override
                    public void onResponse(RoomListResult response) {

                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {
                            if (ArraysUtils.isEmpty(response.data)) {
                                ToastUtils.show(getApplicationContext(), R.string.footer_load_end);
                            } else {
                                mRoomMap.put(mBuildingInfo.id, response.data);
                                showRoomList();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(getActivity(), R.string.msg_error);
                        CustomDialogFragment.dismissDialog();
                    }
                });

    }

    private void resetBuidlingInfo() {
        resetRoomInfo();
        mBuildingInfo = null;
        mSelBuildingView.setText("");
    }

    private void resetRoomInfo() {
        mRoomInfo = null;
        mSelRoomView.setText("");
    }

    private void submitData() {
        if (mDistrictInfo == null) {
            ToastUtils.show(this, R.string.msg_select_district);
            return;
        }
        if (mBuildingInfo == null) {
            ToastUtils.show(this, R.string.msg_select_building);
            return;
        }
        if (mRoomInfo == null) {
            ToastUtils.show(this, R.string.msg_select_room);
            return;
        }

        EditText et = mViewFinder.find(R.id.et_name);
        String name = et.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.show(this, R.string.msg_name_empty);
            et.requestFocus();
            return;
        }
        et = mViewFinder.find(R.id.et_tel);
        String tel = et.getText().toString().trim();
        if (!CheckUtils.isMobileNO(tel)) {
            ToastUtils.show(this, R.string.label_legal_mobile);
            et.setSelection(tel.length());
            et.requestFocus();
            return;
        }

        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
        }

        CustomDialogFragment.show(getSupportFragmentManager(), R.string.loading, getClass().getName());
        Map<String, String> data = new HashMap<>();
        data.put("villagesid", String.valueOf(mDistrictInfo.id));
        data.put("buildingid", String.valueOf(mBuildingInfo.id));
        data.put("roomid", String.valueOf(mRoomInfo.id));
        data.put("username", name);
        data.put("usertel", tel);
        ApiUtils.post(this,
                URLConstants.USER_VILLAGES_AUTH, data, BaseResult.class, new Response.Listener<BaseResult>() {
                    @Override
                    public void onResponse(BaseResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {
                            ToastUtils.show(getActivity(), R.string.msg_submit_ok);
                            finishActivity();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(getActivity(), R.string.msg_error);
                        CustomDialogFragment.dismissDialog();
                    }
                });
    }
}
