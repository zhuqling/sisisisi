package com.yizan.community.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;

import com.fanwe.seallibrary.model.InitInfo;
import com.yizan.community.R;
import com.zongyou.library.app.DeviceUtils;
import com.zongyou.library.app.upgrade.DownloadCompleteReceiver;
import com.zongyou.library.app.upgrade.UpgradeManager;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-11-24
 * Time: 15:05
 * FIXME
 */
public class AppUpdate {
    private static DownloadCompleteReceiver sDownloadCompleteReceiver;

    public static void checkUpdate(final Activity context, boolean silence) {
        final InitInfo initInfo = PreferenceUtils.getObject(context, InitInfo.class);
        if (null != initInfo
                && !TextUtils.isEmpty(initInfo.appVersion)
                && !TextUtils.isEmpty(initInfo.appDownUrl)
                && needUpdate(context, initInfo.appVersion)) {
            AlertDialog.Builder db = new AlertDialog.Builder(context,
                    AlertDialog.THEME_HOLO_LIGHT)
                    .setTitle(R.string.upgrade)
                    .setMessage(initInfo.upgradeInfo)
                    .setPositiveButton(R.string.upgrade_now,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    long downloadId = UpgradeManager.updgrade(
                                            context,
                                            Uri.parse(initInfo.appDownUrl),
                                            context.getString(R.string.app_name)
                                                    + initInfo.appVersion,
                                            initInfo.upgradeInfo);
                                    sDownloadCompleteReceiver = new DownloadCompleteReceiver(
                                            downloadId);
                                    context
                                            .registerReceiver(
                                                    sDownloadCompleteReceiver,
                                                    new IntentFilter(
                                                            DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                }
                            });
            if (!initInfo.forceUpgrade)
                db.setNegativeButton(R.string.upgrade_ignore,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });
            db.setCancelable(initInfo.forceUpgrade);
            db.create().show();
        }else{
            if(!silence) {
                ToastUtils.show(context, R.string.msg_success_version_last);
            }
        }
    }

    private static boolean needUpdate(Context context, String appVersion) {
        boolean bNeed = false;
        try {
            if (TextUtils.isEmpty(appVersion)) {
                return bNeed;
            }
            float newVersion = Float.parseFloat(appVersion);
            float currVersion = Float.parseFloat(DeviceUtils.getPackageInfo(context).versionName);
            if(newVersion > currVersion){
                bNeed = true;
            }

        } catch (Exception e) {

        }
        return bNeed;
    }
}
