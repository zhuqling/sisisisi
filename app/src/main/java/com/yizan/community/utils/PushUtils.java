package com.yizan.community.utils;

import android.content.Context;

import com.yizan.community.BuildConfig;
import com.fanwe.seallibrary.model.UserInfo;
import com.zongyou.library.platform.ZYPushConfig;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Administrator on 2015/11/16.
 */
public class PushUtils {
    private static int num = 0;

    public static void init(final Context context) {
//        JPushInterface.setLatestNotificationNumber(context, 1);
//        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(context);
////        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
//        builder.notificationDefaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;  // 设置为铃声与震动都要
//        JPushInterface.setDefaultPushNotificationBuilder(builder);
        ZYPushConfig.setDebugMode(BuildConfig.DEBUG);
        ZYPushConfig.init(context);
        ZYPushConfig.setLatestNotificationNumber(context, 1);
        ZYPushConfig.setDefaultNotificationSound(context, false, true);
        initTagsAndAlias(context);
    }

    public static void initTagsAndAlias(Context context) {
        initTags(context);
        initAlias(context);
    }

    public static void initTags(Context context) {
        /**
         * 标签
         */
        Set<String> list = new HashSet<String>();
        list.add("buyer");
        list.add("android");
        ZYPushConfig.initTags(context, list);
    }

    /**
     * 别名
     */
    public static void initAlias(final Context context) {
        UserInfo info = PreferenceUtils.getObject(context, UserInfo.class);
        String str = "";
        if (O2OUtils.isLogin(context) && info != null) {
            str = "buyer_" + info.id;
        } else {
            ZYPushConfig.clearAllNotifications(context);
        }
        ZYPushConfig.initAlias(context, str);
    }
    public static void isPush(Context context,boolean parameter){
        if(parameter) {
            ZYPushConfig.resumePush(context);
        }else {
            ZYPushConfig.stopPush(context);
        }
    }
    public static Boolean isPushStopped(Context context){
        return ZYPushConfig.isPushStopped(context);
    }

}
