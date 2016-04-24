package com.yizan.community.utils;

import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.Date;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-10-12
 * Time: 14:06
 * FIXME
 */
public class CacheUtils {
    private final static String DIR_TAG = "yzo2o_tmp";
    public static boolean checkCacheDir() {
        try {
            File sdcardDir = Environment.getExternalStorageDirectory();

            String path = sdcardDir.getPath() + "/" + DIR_TAG;
            File path1 = new File(path);
            if (!path1.exists()) {
                //若不存在，创建目录，可以在应用启动的时候创建
                path1.mkdirs();
            }
            return  true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String getCacheDir() {
        checkCacheDir();
        File sdcardDir = Environment.getExternalStorageDirectory();

        return sdcardDir.getPath() + "/" + DIR_TAG + "/";
    }
    public static String createTmpJpg() {
        return getCacheDir() + new Date().getTime() + ".jpg";
    }

    public static Uri createTmpJpgURI() {
        return Uri.fromFile(new File(createTmpJpg()));
    }

    private static void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }
    public static void clear() {
        deleteAllFiles(new File(getCacheDir()));
    }
}
