package com.yizan.community.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.DistrictAuthInfo;
import com.fanwe.seallibrary.model.PropertyFunc;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.DistrictAuthResult;
import com.yizan.community.R;
import com.yizan.community.adapter.PropertyListAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CheckUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物业管理相关功能
 */
public class PropertyActivity extends BaseActivity implements BaseActivity.TitleListener {
    private FragmentActivity mTmpActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);

        setTitleListener(this);
        initView();
    }

    @Override
    public FragmentActivity getActivity() {
        if(mTmpActivity != null){
            return mTmpActivity;
        }
        return super.getActivity();
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_property);
    }

    private void initView() {
        List<PropertyFunc> list = new ArrayList<>();
        list.add(new PropertyFunc(0, R.string.item_property_0, R.drawable.ic_property_0));
        list.add(new PropertyFunc(1, R.string.item_property_1, R.drawable.ic_property_1));
        list.add(new PropertyFunc(2, R.string.item_property_2, R.drawable.ic_property_2));
        list.add(new PropertyFunc(3, R.string.item_property_3, R.drawable.ic_property_3));
        list.add(new PropertyFunc(4, R.string.item_property_4, R.drawable.ic_property_4));
        PropertyListAdapter adapter = new PropertyListAdapter(this, list);
        GridView gv = mViewFinder.find(R.id.gv_list);
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 4) {
                    openFuncCheck(position);
                } else {
                    ToastUtils.show(getActivity(), R.string.func_not_open);
                }
            }
        });
    }

    public static PropertyActivity getInstance(FragmentActivity activity){
        PropertyActivity propertyActivity = new PropertyActivity();
        propertyActivity.mTmpActivity = activity;
        return propertyActivity;
    }

    private void openFunc(int funcId) {
        switch (funcId) {
            case 4:
                openDoor();
                break;
            default:
                ToastUtils.show(getActivity(), R.string.func_not_open);
                break;
        }
    }

    public void openDoor() {
        UserInfo userInfo = PreferenceUtils.getObject(getActivity(), UserInfo.class);
        if (userInfo == null || userInfo.propertyUser == null) {
            return;
        }

        switch (userInfo.propertyUser.accessStatus) {
            case 1: // 通过
                Intent intent = new Intent(getActivity(), OpenDoorActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, userInfo.propertyUser);
                getActivity().startActivity(intent);
                break;
            default:
                chooseAuthOpenDoor();
                break;

        }
    }

    private void chooseAuthOpenDoor(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(R.string.msg_open_door_apply_title);
        builder.setMessage(R.string.msg_open_door_apply_desc);
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        applyOpenDoor();
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        builder.show();
    }

    public boolean openFuncCheck(int funcId) {
        if (O2OUtils.turnLogin(getActivity())) {
            return false;
        }
        UserInfo userInfo = PreferenceUtils.getObject(getActivity(), UserInfo.class);
        if (userInfo.propertyUser != null && userInfo.propertyUser.status == 1) {
            // 审核通过
            openFunc(funcId);
            return true;
        } else {
            checkAuth(funcId);
        }
        return false;
    }

    private void applyOpenDoor() {
        if (!NetworkUtils.isNetworkAvaiable(getActivity())) {
            ToastUtils.show(getActivity(), R.string.msg_error_network);
        }

        CustomDialogFragment.show(getActivity().getSupportFragmentManager(), R.string.loading, getActivity().getClass().getName());
        Map<String, String> data = new HashMap<>();
        ApiUtils.post(getActivity(),
                URLConstants.USER_OPEN_DOOR_APPLY, data, DistrictAuthResult.class, new Response.Listener<DistrictAuthResult>() {
                    @Override
                    public void onResponse(DistrictAuthResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {
                            DistrictAuthInfo info = response.data;
                            if (info != null) {
                                reflashUserInfo(info);
                                ToastUtils.show(getActivity(), R.string.msg_submit_guard_ok);
                            } else {

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

    private void checkAuth(final int funcId) {

        if (!NetworkUtils.isNetworkAvaiable(getActivity())) {
            ToastUtils.show(getActivity(), R.string.msg_error_network);
        }

        CustomDialogFragment.show(getActivity().getSupportFragmentManager(), R.string.loading, getActivity().getClass().getName());
        Map<String, String> data = new HashMap<>();
        ApiUtils.post(getActivity(),
                URLConstants.USER_CHECK_VILLAGESAUTH, data, DistrictAuthResult.class, new Response.Listener<DistrictAuthResult>() {
                    @Override
                    public void onResponse(DistrictAuthResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {
                            DistrictAuthInfo info = response.data;
                            if (info != null) {
                                if (info.status == 1) {
                                    reflashUserInfo(info);
                                    openFunc(funcId);
                                } else if (info.status == -1) {
                                    startAuthActivity(info);
                                } else {
                                    ToastUtils.show(getActivity(), R.string.msg_authing);
                                }
                            } else {
                                startAuthActivity(null);
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

    private void startAuthActivity(DistrictAuthInfo info) {
        Intent intent = new Intent(getActivity(), IdentityAuthActivity.class);
        if (info != null) {
            intent.putExtra(Constants.EXTRA_DATA, info);
        }
        getActivity().startActivity(intent);
    }

    private void reflashUserInfo(DistrictAuthInfo info) {
        UserInfo userInfo = PreferenceUtils.getObject(getActivity(), UserInfo.class);
        userInfo.propertyUser = info;
        PreferenceUtils.setObject(getActivity(), userInfo);
    }
}
