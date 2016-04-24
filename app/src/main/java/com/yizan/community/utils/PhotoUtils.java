package com.yizan.community.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.zongyou.library.util.LogUtils;

import java.io.File;
import java.util.Date;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-11-30
 * Time: 14:08
 * FIXME
 */
public class PhotoUtils {
    /**
     * request code of Activities or Fragments You will have to change the
     * values of the request codes below if they conflict with your own.
     */
    public static final int REQUEST_GALLERY = 0x7101;
    public static final int REQUEST_CAMERA = 0x7102;


    public static Intent buildGalleryIntent() {
        return new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
    }


    public static Intent buildCaptureIntent(Uri uri) {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                MediaStore.EXTRA_OUTPUT, uri);
    }


    private static Uri createUri(final Context context) {
        File file = context.getExternalCacheDir();
        if (file == null)
            file = context.getCacheDir();
        String path = file.getPath() + File.separatorChar + new Date().getTime() + ".jpg";
        file = null;
        return Uri.fromFile(new File(path));
    }

    public static void handleResult(PhotoHandler handler, int requestCode,
                                    int resultCode, Intent data) {
        if (handler == null)
            return;

        if (resultCode == Activity.RESULT_CANCELED) {
            handler.onPhotoCancel();
        } else if (resultCode == Activity.RESULT_OK) {
            PhotoParams cropParams = handler.getPhotoParams();
            if (cropParams == null) {
                handler.onPhotoFailed("PhotoHandler's params MUST NOT be null!");
                return;
            }
            Intent intent = null;
            PhotoParams params = null;
            String path = "";
            switch (requestCode) {

                case PhotoUtils.REQUEST_GALLERY:
                    handler.getPhotoParams().uri = data.getData();
                    handler.getPhotoParams().uri = PhotoUtils.uriFormat(handler.getContext(), handler.getPhotoParams().uri);

                    params = handler.getPhotoParams();
                    path = BitmapUtils.compressBitmap(handler.getContext(), params.uri.getPath(), params.outputX, params.outputY, false);
                    handler.getPhotoParams().takeResult = Uri.fromFile(new File(path));
                    handler.onPhotoTaked(handler.getPhotoParams().takeResult);
                    break;
                case PhotoUtils.REQUEST_CAMERA:

                    params = handler.getPhotoParams();
                    path = BitmapUtils.compressBitmap(handler.getContext(), params.uri.getPath(), params.outputX, params.outputY, true);
                    handler.onPhotoTaked(Uri.fromFile(new File(path)));
                    break;
            }


        }
    }



    public static Uri uriFormat(Context context, Uri uri) {
        try{
            String url = getPath(context, uri);
            if(!TextUtils.isEmpty(url)) {
                return Uri.fromFile(new File(url));
            }
        }catch (Exception e){
            LogUtils.e(e.getMessage());
        }
        return uri;
    }

    //以下是关键，原本uri返回的是file:///...来着的，android4.4返回的是content:///...
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public interface PhotoHandler {

        void onPhotoTaked(Uri uri);

        void onPhotoCancel();

        void onPhotoFailed(String message);

        PhotoParams getPhotoParams();

        Activity getContext();
    }

    public static class PhotoParams {

        public static final String OUTPUT_FORMAT = Bitmap.CompressFormat.JPEG
                .toString();

        public static final int DEFAULT_TAKE_OUTPUT_X = 640;
        public static final int DEFAULT_TAKE_OUTPUT_Y = 800;

        public Uri uri;
        public Uri takeResult;

        public String outputFormat;        // 文件生成格式
        public int outputX = DEFAULT_TAKE_OUTPUT_X;
        public int outputY = DEFAULT_TAKE_OUTPUT_Y;



        public PhotoParams() {
            outputFormat = OUTPUT_FORMAT;
            uri = buildUri();
        }

        private static Uri buildUri(){
            return Uri.fromFile(Environment.getExternalStorageDirectory()).buildUpon().appendPath("crop_cache_file.jpg").build();
        }
    }

}
