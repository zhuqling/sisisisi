package com.yizan.community.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.zongyou.library.util.base64.AESUtils;
import com.zongyou.library.util.json.JSONHelper;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.VolleyUtils;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-17
 * Time: 11:22
 * FIXME
 */
public class ApiUtils {

    public static <T> void post(Context context, String url, Object data, final Class<T> clazz, Response.Listener<T> listener) {
        VolleyUtils.post(context,url, getParams(context, data, url), clazz, listener, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO toast error

            }

        },URLConstants.ENUM == URLConstants.URLEnum.Encryption ? true : false);
    }

    public static <T> void get(String url, Map<String, String> data, final Class<T> clazz, Response.Listener<T> listener,Response.ErrorListener errorListener) {
        VolleyUtils.get(url, data, clazz, listener, errorListener);
    }

    public static <T> void post(Context context, String url, Object data, final Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener el) {

        VolleyUtils.post(context, url, getParams(context, data, url), clazz, listener, el, URLConstants.ENUM == URLConstants.URLEnum.Encryption ? true : false);
        // FIXME url debug
        StringBuilder sb = new StringBuilder(url);
        HashMap<String, String> pa = getLightParams(context, data, url);
        Set<Map.Entry<String, String>> en = pa.entrySet();
        Iterator<Map.Entry<String, String>> i = en.iterator();
        int index = 0;
        if (i.hasNext()) {
            sb.append("?");
            index++;
            while (i.hasNext()) {
                Map.Entry<String, String> e = i.next();
                sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
            }
        }
        Log.e("http", index > 0 ? sb.replace(sb.length() - 1, sb.length(), "").toString() : sb.toString());
    }

    private static HashMap<String, String> getLightParams(Context context,
                                                     Object data, String url) {
        HashMap<String, String> params = new HashMap<String, String>(3);
        final UserInfo info = PreferenceUtils.getObject(context,
                UserInfo.class);
        String token = PreferenceUtils.getValue(context, Constants.LOGIN_TOKEN, "");

        if (null != info) {
            if (!TextUtils.isEmpty(token))
                params.put(Constants.TOKEN, token);
            else {
                params.put(Constants.TOKEN,
                        PreferenceUtils.getValue(context, Constants.TOKEN, ""));
            }
            params.put(Constants.USER_ID, String.valueOf(info.id));

        }
        UserAddressInfo addressInfo = PreferenceUtils.getObject(context, UserAddressInfo.class);
        if(addressInfo != null && null != addressInfo.mapPoint){
            params.put(Constants.MAP_POINT, addressInfo.mapPoint.toString());
        }
        if(null!= data){
            String paramData = JSONHelper.toJSON(data);
            params.put(Constants.DATA, paramData);
        }
        return params;
    }

    private static HashMap<String, String> getParams(Context context,
                                                     Object data, String url) {
//        HashMap<String, String> params = new HashMap<String, String>(3);
//        final UserInfo info = PreferenceUtils.getObject(context,
//                UserInfo.class);
//        String token = PreferenceUtils.getValue(context, Constants.TOKEN, "");
//
//        if (null != info) {
//            if (!TextUtils.isEmpty(token))
//                params.put(Constants.TOKEN, token);
//            else {
//                params.put(Constants.TOKEN,
//                        PreferenceUtils.getValue(context, Constants.TOKEN, ""));
//            }
//            params.put(Constants.USER_ID, String.valueOf(info.id));
//
//        }
//        UserAddressInfo addressInfo = PreferenceUtils.getObject(context, UserAddressInfo.class);
//        if(addressInfo != null && !TextUtils.isEmpty(addressInfo.mapPoint)){
//            params.put(Constants.MAP_POINT, addressInfo.mapPoint);
//        }
//        if(null!= data){
//            String paramData = JSONHelper.toJSON(data);
//		if (!url.equals(URLConstants.LOGIN)) {
//			AESUtils aesUtils = new AESUtils(token, PreferenceUtils.getValue(
//					context, Constants.KEY, ""), info.user.id);
//			paramData = aesUtils.encrypt(JSONHelper.toJSON(data));
//		}
//            params.put(Constants.DATA, paramData);
//        }
//        return params;

        HashMap<String, String> params = new HashMap<String, String>(2);
        final UserInfo info = PreferenceUtils.getObject(context, UserInfo.class);

        String token = PreferenceUtils.getValue(context, Constants.LOGIN_TOKEN, "");
//        City city = PreferenceUtils.getObject(context, City.class);
//        if (null != city && city.id != 0)
//            params.put(ParamConstants.CITY_ID, String.valueOf(city.id));
        if (null != info) {
            if (!TextUtils.isEmpty(token))
                params.put(Constants.TOKEN, token);
            else
                params.put(Constants.TOKEN, PreferenceUtils.getValue(context, Constants.TOKEN, ""));

            if (null != info && 0 != info.id)
                params.put(Constants.USER_ID, String.valueOf(info.id));
        }
        UserAddressInfo addressInfo = PreferenceUtils.getObject(context, UserAddressInfo.class);
        if (addressInfo != null && null !=addressInfo.mapPoint) {
            params.put(Constants.MAP_POINT, addressInfo.mapPoint.toString());
        }
        if (data != null) {
            String paramData = JSONHelper.toJSON(data);
            if (URLConstants.ENUM == URLConstants.URLEnum.Encryption) {
                if (!url.equals(URLConstants.INIT)) {
                    AESUtils aesUtils = new AESUtils(token, PreferenceUtils.getValue(context, Constants.KEY, ""), info.id);
                    paramData = aesUtils.encrypt(JSONHelper.toJSON(data));
                }
            }
            params.put(Constants.DATA, paramData);
        }
        return params;
    }
}
