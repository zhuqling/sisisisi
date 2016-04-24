package com.yizan.community.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.yizan.community.R;
import com.yizan.community.YizanApp;
import com.zongyou.library.util.LogUtils;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-12-07
 * Time: 11:34
 * FIXME
 */
public class ImgUrl {
    protected static int sScreenWidth = 0;
    protected static int sScreenHeight = 0;
    protected static float sDensity = 0.0f;

    public static void init(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        sScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        sScreenHeight = dm.heightPixels;
        sDensity = dm.density;
    }

    public static int getScreenWidth(){
        return sScreenWidth;
    }
    public static int getScreenHeight(){
        return sScreenHeight;
    }
    public static String formatUrl(int width, int height, String url) {
        String fmtUrl = url;

        width = (width == -1) ? sScreenWidth : width;
        height = (height == -1) ? sScreenHeight : height;

        try {
            if (width > 0 && height > 0) {
                return url + String.format("@%dw_%dh_1e_1c.jpg", width, height);
            }
            if (width > 0) {
                return url + String.format("@%dw_1e_1c.jpg", width);
            }
            if (height > 0) {
                return url + String.format("@%dh_1e_1c.jpg", height);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("ImgUrl", e.getMessage());
        }

        return fmtUrl;
    }

    public static String scaleUrl(int wDimenId, int hDimenId, String url) {
        int width = (int) YizanApp.getInstance().getResources().getDimension(wDimenId);
        int height = (int) YizanApp.getInstance().getResources().getDimension(hDimenId);
        return formatUrl(width, height, url);
    }

    public static String heightUrl(int dimenId, String url) {
        int height = (int) YizanApp.getInstance().getResources().getDimension(dimenId);
        return formatUrl(-1, height, url);
    }

    public static String widthUrl(int dimenId, String url) {
        int width = (int) YizanApp.getInstance().getResources().getDimension(dimenId);
        return formatUrl(width, -1, url);
    }

    public static String squareUrl(int dimenId, String url) {
        int width = (int) YizanApp.getInstance().getResources().getDimension(dimenId);
        return formatUrl(width, width, url);
    }

    public static String square4Url(String url) {
        int width = sScreenWidth / 4;
        return formatUrl(width, width, url);
    }
}
