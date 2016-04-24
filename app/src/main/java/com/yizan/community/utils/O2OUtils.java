package com.yizan.community.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.yizan.community.R;
import com.yizan.community.activity.LoginActivity;
import com.yizan.community.activity.ViewImageActivity;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.InterFace;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.PointInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.ArrayList;
import java.util.List;

public final class O2OUtils {
    /**
     * 是否已登录
     *
     * @param context
     * @return
     */
    public static boolean isLogin(Context context) {
        UserInfo user = PreferenceUtils.getObject(context, UserInfo.class);
        return null != user && 0 != user.id;
    }

    public static boolean turnLogin(Context context){
        UserInfo user = PreferenceUtils.getObject(context, UserInfo.class);
        if(null != user && 0 != user.id){
            return false;
        }
        PreferenceUtils.clearSettings(context, UserInfo.class.getName());
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.EXTRA_BACK_ABLE, true);
        context.startActivity(intent);
        return true;
    }

    /**
     * 重新登录
     *
     * @param context
     */
    public static void reidrectLogin(Context context) {

        ApiUtils.post(context, URLConstants.LOGOUT, null, BaseResult.class, new Listener() {

            @Override
            public void onResponse(Object response) {

            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        clearUserData(context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Constants.EXTRA_BACK_ABLE, true);
        context.startActivity(intent);

    }
    public static void reflashLoginToken(Context context, String token){
        if(!TextUtils.isEmpty(token)){
            PreferenceUtils.setValue(context, Constants.LOGIN_TOKEN, token);
        }
    }
    public static void cacheUserData(Context context, UserResultInfo info) {
        try {
            if (null != info && null != info.data) {
                PreferenceUtils.setObject(context, info.data);
//                PreferenceUtils.setValue(context, Constants.TOKEN, info.token);
                PreferenceUtils.setValue(context, Constants.LOGIN_TOKEN, info.token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void clearUserData(Context context) {
        PreferenceUtils.clearObject(context, UserInfo.class);
        PreferenceUtils.setValue(context, Constants.LOGIN_TOKEN, "");
        PreferenceUtils.setValue(context, Constants.USER_ID, 0);
    }

    public static void logout(Context context) {
        clearUserData(context);
        ApiUtils.post(context, URLConstants.LOGOUT, null, BaseResult.class, new Listener() {

            @Override
            public void onResponse(Object response) {

            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
    /**
     * 检查response，并提示
     *
     * @param context
     * @param response
     * @return true ,false
     */
    public static boolean checkResponse(Context context, BaseResult response) {
        if (null != response) {
            if (InterFace.ResponseCode.SUCCESS.code != response.code) {
                switch (response.code) {
                    case 99996:
                        ToastUtils.show(context, InterFace.ResponseCode.ERROR_UNLOGIN.msg);
                        // reidrectLogin(context);
                        break;
                    case 99997:
                        if(TextUtils.isEmpty(response.msg)) {
                            ToastUtils.show(context, InterFace.ResponseCode.ERROR_TOKEN.msg);
                        }else{
                            ToastUtils.show(context, response.msg);
                        }
                        reidrectLogin(context);
                        break;
                    case 99998:
                    case 99999:
                        ToastUtils.show(context, R.string.msg_error);
                        break;
                    default:
                        if (!TextUtils.isEmpty(response.msg))
                            ToastUtils.show(context, response.msg);
                        break;
                }
            } else
                return true;

        } else
            ToastUtils.show(context, R.string.msg_error);
        return false;
    }

    public static void setImageurl(final Context context, final List<String> images, NetworkImageView... imageViews) {
        int index = 0;
        for (NetworkImageView image : imageViews) {
            if (images.size() == 0 || images.size() == 1 && TextUtils.isEmpty(images.get(0))) {
                image.setVisibility(View.GONE);
                index++;
                continue;
            }
            if (images.size() > index && !TextUtils.isEmpty(images.get(index))) {
                image.setDefaultImageResId(R.drawable.ic_default_square);
                image.setErrorImageResId(R.drawable.ic_default_square);
                image.setImageUrl(images.get(index), RequestManager.getImageLoader());
                image.setVisibility(View.VISIBLE);
                final int i = index;
                image.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ViewImageActivity.class);
                        intent.putStringArrayListExtra(Constants.EXTRA_DATA, (ArrayList<String>) images);
                        intent.putExtra(Constants.EXTRA_INDEX, i);
                        context.startActivity(intent);
                    }
                });
            } else
                image.setVisibility(View.INVISIBLE);
            index++;
        }
    }

    public static String numberLengthFormat(String source, int length) {
        if (null != source) {
            int offset = length - source.length();
            if (offset > 0) {
                StringBuilder sb = new StringBuilder(length);
                while (offset > 0) {
                    sb.append("  ");
                    offset--;
                }
                sb.append(" ");
                sb.append(source);
                return sb.toString();
            }
        }
        return source;
    }

    public static void openSysMap(Activity activity, PointInfo point) {

        if (point == null) {
            ToastUtils.show(activity, activity.getResources().getString(R.string.err_map_info));
            return;
        }
        try {
            Uri mUri = Uri.parse("geo:" + point.x + "," + point.y + "?q=" + point.address);
            Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
            activity.startActivity(intent);
        } catch (Exception e) {
            ToastUtils.show(activity, activity.getResources().getString(R.string.install_map_app));
        }
    }
}
