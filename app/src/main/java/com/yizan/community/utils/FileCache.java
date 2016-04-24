package com.yizan.community.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.zongyou.library.util.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-11-12
 * Time: 14:15
 * FIXME
 */
public class FileCache {
    private static final String DEFAULT_CACHE_DIR = "rdtools";

    private static File getCacheDir(Context context) {
        return new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
    }

    public static synchronized void clear(Context context) {
        File[] files = getCacheDir(context).listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static boolean init(Context context) {
        File rootDir = getCacheDir(context);
        if (!rootDir.exists()) {
            if (!rootDir.mkdirs()) {
                Log.e("FileCache", "Unable to create cache dir: " +
                        rootDir.getAbsolutePath());
                return false;
            }

        }
        return true;
    }

    protected static String randomFileName() {
        return "cache_" + (new Date()).getTime();
    }

    public static Uri createCacheUri(Context context) {
        return Uri.fromFile(getCacheDir(context))
                .buildUpon().appendPath(randomFileName()).build();
    }

    public static Uri cacheBitmap(Context context, Bitmap bmp) {
        Uri uri = createCacheUri(context);
        File f = new File(uri.getPath());
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();
            return uri;
        } catch (FileNotFoundException e) {
            LogUtils.e(e.getMessage());
        } catch (IOException e) {
            LogUtils.e(e.getMessage());
        }
        return null;
    }

}
