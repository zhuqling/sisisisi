package com.yizan.community.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.InitInfo;
import com.fanwe.seallibrary.model.JoinBusinessInfo;
import com.fanwe.seallibrary.model.MessageStatusInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.JoinBusinessResult;
import com.fanwe.seallibrary.model.result.MessageStatusResult;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.activity.JoinBusinessActivity;
import com.yizan.community.activity.LoginActivity;
import com.yizan.community.activity.MyCollectionActivity;
import com.yizan.community.activity.PropertyActivity;
import com.yizan.community.activity.RegistorActivity;
import com.yizan.community.activity.ServerMessageActivity;
import com.yizan.community.activity.SetUpActivity;
import com.yizan.community.activity.SwitchAddressActivity;
import com.yizan.community.activity.UserEditActivity;
import com.yizan.community.activity.WebMessageActivity;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.app.IntentUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.HashMap;

/**
 * 我的Fragment
 * Created by ztl on 2015/9/17.
 */
public class PersonalCenterFragment extends BaseFragment implements View.OnClickListener {

    private final int REQUEST_EDIT_USER = 0x203;

    private NetworkImageView mHeadPortrait;
    private TextView mUserName, mUserMobile, mServiceMsgStatus, mMyCollectionMsgStatus, mAddressMsgStatus, mServiceTell, mServiceTime;
    private LinearLayout mIsLogin, mNoLogin;
    private RelativeLayout mEditHead;
    private TextView mMineRegisotr, mMineLogin;
    private View mJoinBusinessView;

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_mine_personal_center, container, false);
    }

    public void reFlashUI() {
        initView();
    }

    private String serviceTel;

    @Override
    protected void initView() {
        InitInfo configInfo = PreferenceUtils.getObject(mFragmentActivity, InitInfo.class);
        mHeadPortrait = mViewFinder.find(R.id.headportrait);
        mUserName = mViewFinder.find(R.id.user_name);
        mUserMobile = mViewFinder.find(R.id.user_mobile);
        mIsLogin = mViewFinder.find(R.id.is_login);
        mNoLogin = mViewFinder.find(R.id.no_login);

        mServiceTell = mViewFinder.find(R.id.service_tel);
        serviceTel = configInfo.serviceTel;
        mServiceTell.setText(Html.fromHtml("<u>" + serviceTel + "</u>"));
        mServiceTime = mViewFinder.find(R.id.service_time);
        mServiceTime.setText(getResources().getString(R.string.msg_server_time) + configInfo.serviceTime);

        mServiceMsgStatus = mViewFinder.find(R.id.servicer_msg_status);


        mMyCollectionMsgStatus = mViewFinder.find(R.id.my_collection_status);


        mAddressMsgStatus = mViewFinder.find(R.id.address_msg_status);


        mViewFinder.find(R.id.my_collection).setOnClickListener(this);
        mJoinBusinessView =
                mViewFinder.find(R.id.join_business_ll);
        mJoinBusinessView.setOnClickListener(this);
        mViewFinder.find(R.id.service_msg).setOnClickListener(this);

        mViewFinder.find(R.id.addr_management).setOnClickListener(this);

        mViewFinder.find(R.id.set_up).setOnClickListener(this);

        mEditHead = mViewFinder.find(R.id.rl_head_portraint);
        mEditHead.setOnClickListener(this);

        mMineLogin = mViewFinder.find(R.id.mine_login);
        mMineLogin.setOnClickListener(this);

        mMineRegisotr = mViewFinder.find(R.id.mine_registor);
        mMineRegisotr.setOnClickListener(this);
        if (O2OUtils.isLogin(mFragmentActivity)) {
            mIsLogin.setVisibility(View.VISIBLE);
            mNoLogin.setVisibility(View.GONE);
            loadPersonal();
        } else {
            mIsLogin.setVisibility(View.GONE);
            mNoLogin.setVisibility(View.VISIBLE);
            updateMsgCount(new MessageStatusInfo());
            setImageUrl(mHeadPortrait, "http://2tu.github.io/", R.drawable.no_headportaint);
        }
        mViewFinder.find(R.id.personal_tel_container).setOnClickListener(this);
        mViewFinder.find(R.id.share_ll).setOnClickListener(this);

        if (!BuildConfig.USER_PROPERTY_GUARD) {
            mViewFinder.find(R.id.ll_gurad).setVisibility(View.GONE);
        } else {
            mViewFinder.onClick(R.id.ll_gurad, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessageStatus();
    }

    /**
     * 用户是否是服务人员端用户
     */
    private void sellerCheck() {
        if (!NetworkUtils.isNetworkAvaiable(mFragmentActivity)) {
            return;
        }
        CustomDialogFragment.show(getFragmentManager(), R.string.loading, this.getClass().toString());
        HashMap<String, String> params = new HashMap<String, String>();
        UserInfo user = PreferenceUtils.getObject(mFragmentActivity, UserInfo.class);
        params.put("id", String.valueOf(user.id));
        ApiUtils.post(mFragmentActivity, URLConstants.USER_IS_SELLER,
                params,
                JoinBusinessResult.class, new Response.Listener<JoinBusinessResult>() {
                    @Override
                    public void onResponse(JoinBusinessResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {
                            if (response.data == null) {
                                startActivity(new Intent(mFragmentActivity, JoinBusinessActivity.class));
                            } else {
                                Intent data = new Intent(mFragmentActivity, JoinBusinessActivity.class);
                                switch (response.data.isCheck) {
                                    case 1:
                                        openShopOk(response.data.appurl);
                                        break;
                                    default:
                                        data.putExtra(Constants.EXTRA_DATA, response.data);
                                        startActivity(data);
                                    case 0:
                                        ToastUtils.show(getActivity(), R.string.shop_open_checking);
                                        data.putExtra(Constants.EXTRA_DATA, response.data);
                                        startActivity(data);
                                        break;
                                    case -1:
                                        openShopReject(response.data.checkVal, response.data);
                                        break;
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(getActivity(), R.string.loading_err_nor);
                    }
                });
    }

    /**
     * 刷新消息数量控件
     *
     * @param messageStatusInfo
     */
    public void updateMsgCount(MessageStatusInfo messageStatusInfo) {
        if (null == messageStatusInfo) {
            messageStatusInfo = new MessageStatusInfo();
        }
        mServiceMsgStatus.setText(String.format(getResources().getString(R.string.service_msg), String.valueOf(messageStatusInfo.newMsgCount)));
        mMyCollectionMsgStatus.setText(String.format(getResources().getString(R.string.my_collection), String.valueOf(messageStatusInfo.collectCount)));
        mAddressMsgStatus.setText(String.format(getResources().getString(R.string.addr_management), String.valueOf(messageStatusInfo.addressCount)));
    }

    /**
     * 查询消息
     */
    private void loadMessageStatus() {
        if (!NetworkUtils.isNetworkAvaiable(mFragmentActivity)) {
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        ApiUtils.post(mFragmentActivity, URLConstants.MSG_STATUS,
                params,
                MessageStatusResult.class, new Response.Listener<MessageStatusResult>() {
                    @Override
                    public void onResponse(MessageStatusResult response) {
                        if (response != null && response.data != null) {
                            updateMsgCount(response.data);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
    }

    private void loadPersonal() {
        UserInfo user = PreferenceUtils.getObject(mFragmentActivity, UserInfo.class);
        mUserName.setText(user.name);
        mUserMobile.setText(user.mobile);
        setImageUrl(mHeadPortrait, user.avatar, R.drawable.no_headportaint);
    }

    private void setImageUrl(NetworkImageView imageView, String url, int res) {
        imageView.setDefaultImageResId(res);
        imageView.setErrorImageResId(res);
        imageView.setImageUrl(url, RequestManager.getImageLoader());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.personal_tel_container:
                try {
                    IntentUtils.dial(mFragmentActivity, serviceTel.replace("-", ""));
                } catch (Exception e) {
                }
                break;
            case R.id.my_collection:
                if (O2OUtils.isLogin(mFragmentActivity)) {
                    startActivity(new Intent(mFragmentActivity, MyCollectionActivity.class));
                } else {
                    ToastUtils.show(mFragmentActivity, R.string.msg_error_not_login);
                    O2OUtils.turnLogin(mFragmentActivity);
                }

                break;
            case R.id.service_msg:
                if (O2OUtils.isLogin(mFragmentActivity)) {
                    if (BuildConfig.WEB_MSG) {
                        startActivity(new Intent(mFragmentActivity, WebMessageActivity.class));
                    } else {
                        startActivity(new Intent(mFragmentActivity, ServerMessageActivity.class));
                    }

                } else {
                    ToastUtils.show(mFragmentActivity, R.string.msg_error_not_login);
                    O2OUtils.turnLogin(mFragmentActivity);
                }
                break;

            case R.id.addr_management:
                Intent intent1 = new Intent(mFragmentActivity, SwitchAddressActivity.class);
                intent1.putExtra("isLocate", "my");
                startActivity(intent1);
                break;
            case R.id.set_up:
                startActivity(new Intent(mFragmentActivity, SetUpActivity.class));
                break;
            case R.id.rl_head_portraint:
                if (!O2OUtils.turnLogin(mFragmentActivity)) {
                    Intent uInent = new Intent(mFragmentActivity,
                            UserEditActivity.class);
                    startActivityForResult(uInent, REQUEST_EDIT_USER);
                }
                break;
            case R.id.mine_login:
                startActivity(new Intent(mFragmentActivity, LoginActivity.class));
                break;
            case R.id.mine_registor:
                startActivity(new Intent(mFragmentActivity, RegistorActivity.class));
                break;
            case R.id.join_business_ll:
                if (!O2OUtils.turnLogin(mFragmentActivity)) {
                    sellerCheck();
                }

                break;
            case R.id.share_ll:
                InitInfo configInfo = PreferenceUtils.getObject(mFragmentActivity, InitInfo.class);
                IntentUtils.sendto(mFragmentActivity, "", getString(R.string.share_content_arg, getString(R.string.app_name), configInfo.appDownUrl));
                break;
            case R.id.ll_gurad:
//                startActivity(new Intent(getActivity(), PropertyActivity.class));
                PropertyActivity.getInstance(getActivity()).openFuncCheck(4);
                break;

        }
    }

    private void openShopReject(String reason, final JoinBusinessInfo seller) {
        String text = getString(R.string.shop_open_reject);
        if (!TextUtils.isEmpty(reason)) {
            text = String.format(text, reason);
        }
        Dialog alertDialog = new AlertDialog.Builder(getActivity()).
                setMessage(text).
                setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent data = new Intent(mFragmentActivity, JoinBusinessActivity.class);
                        data.putExtra(Constants.EXTRA_DATA, seller);
                        startActivity(data);
                    }
                }).
                setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        alertDialog.show();
    }

    private void openShopOk(String appUrl) {
        String text = getString(R.string.shop_open_ok);
        TextView tv = new TextView(getActivity());
        if (!TextUtils.isEmpty(appUrl)) {
            text = String.format(text, appUrl);
        }
        tv.setAutoLinkMask(Linkify.ALL);
        tv.setText(text);
        tv.setTextSize(18);
        Dialog alertDialog1 = new AlertDialog.Builder(getActivity()).
                setView(tv).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        alertDialog1.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_EDIT_USER:
                UserInfo user = PreferenceUtils.getObject(this.getActivity(),
                        UserInfo.class);
                if (null != user) {
                    mUserName.setText(user.name);
                    mUserMobile.setText(user.mobile);
                    mHeadPortrait.setImageUrl(user.avatar, RequestManager.getImageLoader());
                }

                break;
        }


    }


}
