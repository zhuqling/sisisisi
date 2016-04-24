package com.yizan.community;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.yizan.community.utils.FileCache;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.utils.PushUtils;
import com.zongyou.library.platform.ZYStatConfig;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.Date;
import java.util.List;

/**
 * @author Altas
 * @email Altas.Tutu@gmail.com
 * @time 2015-3-19 上午11:26:44
 */
public class YizanApp extends Application implements Application.ActivityLifecycleCallbacks {
    private static YizanApp sMe;
    private long mBackgroundTime;
    private boolean mNeedReload;
    private boolean mIsInBack;

    public static YizanApp getInstance() {
        return sMe;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sMe = this;
        init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RequestManager.getRequestQueue().stop();
//		DBManager.getDBManager(this).closeDB();
        FileCache.clear(this);
        sMe = null;
    }

    private void init() {

        // volley
        RequestManager.init(this);

        PushUtils.init(getApplicationContext());

        ZYStatConfig.setDebugMode(BuildConfig.DEBUG);
        ZYStatConfig.setAppKey(getApplicationContext(), BuildConfig.STAT_KEY);
        ZYStatConfig.setInstallChannel(getApplicationContext(), BuildConfig.APP_CHANNEL);
        ZYStatConfig.setAutoExceptionCaught(true);

        this.registerActivityLifecycleCallbacks(this);
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(!mIsInBack){
            return;
        }
//        long nowTime = (new Date()).getTime();
//        if((nowTime - mBackgroundTime) > 60 * 000){
//            mNeedReload = true;
//        }
        mNeedReload = true;
        mIsInBack = false;
    }

    public boolean isNeedReload(){
        return mNeedReload;
    }
    public void unNeedReload(){
        mNeedReload = false;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if(!isAppOnForeground()){
            mIsInBack = true;
            mBackgroundTime = (new Date()).getTime();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isBackground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(getApplicationContext().getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }


    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }
}
