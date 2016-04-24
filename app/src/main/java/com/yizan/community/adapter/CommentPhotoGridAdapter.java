package com.yizan.community.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.yizan.community.R;
import com.yizan.community.utils.ImgUrl;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CommentPhotoGridAdapter extends CommonAdapter<String> {
    private final String TAG = "PhotoGridAdapter";
    private int MAX_COUNT = 4;
    private IPhotoFunc mPhotoFunc;

    public CommentPhotoGridAdapter(Context context, List<String> datas) {
        super(context, datas, R.layout.item_comment_square_photo);

    }



    public void setPhotoFunc(IPhotoFunc photoFunc) {
        mPhotoFunc = photoFunc;
    }

    @Override
    public int getCount() {
        if (mDatas.size() > MAX_COUNT) {
            return MAX_COUNT;
        }
        return super.getCount();
    }

    public void addItem(String value) {
        if (!TextUtils.isEmpty(value)) {
            if (mDatas.size() > 0
                    && TextUtils.isEmpty(mDatas.get(mDatas.size() - 1))) {
                mDatas.add(mDatas.size() - 1, value);
            }
        } else {
            if (mDatas.size() > 0
                    && !TextUtils.isEmpty(mDatas.get(mDatas.size() - 1))) {
                mDatas.add(value);
            } else {
                mDatas.add(value);
            }
        }
        notifyDataSetChanged();
    }

    public void removeItem(int pos) {
        if (pos >= 0 && mDatas.size() > pos) {
            mDatas.remove(pos);
            notifyDataSetChanged();
        }
    }

    public List<String> getDatas() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < mDatas.size(); i++) {
            if (!TextUtils.isEmpty(mDatas.get(i))) {
                list.add(mDatas.get(i));
            }
        }
        return list;
    }

    @Override
    public void convert(final ViewHolder helper, String item, int number) {
        try {
            final ImageView ivPhoto = helper.getView(R.id.iv_photo);

            RequestManager.getImageLoader().get(ImgUrl.square4Url(item), new ImageListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Load Image: " + error.getMessage());
                }

                @Override
                public void onResponse(ImageContainer response,
                                       boolean isImmediate) {

                    if (response.getBitmap() != null) {
                        ivPhoto.setImageBitmap(response.getBitmap());
//                        int inSampleSize = 1;
//                        int reqWidth = ivPhoto.getWidth();
//                        int outHeight = response.getBitmap().getHeight();
//                        int outWidth = response.getBitmap().getWidth();
//                        if ( outHeight > reqWidth || outWidth > reqWidth) {
//                            final int heightRatio = Math.round((float) outHeight / (float) reqWidth);
//                            final int widthRatio = Math.round((float) outWidth / (float) reqWidth);
//                            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
//                            Matrix matrix = new Matrix();
//                            matrix.postScale(inSampleSize, inSampleSize);
//                            ivPhoto.setImageBitmap(Bitmap.createBitmap(response.getBitmap(), 0, 0, reqWidth, reqWidth, matrix,
//                                    true));
//                        }

                    }
                }
            });

//            helper.setImageUrl(R.id.iv_photo, item, RequestManager.getImageLoader(), R.drawable.ic_default_square);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isLocalFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        if (path.startsWith("http://")) {
            return false;
        }
        return true;
    }

    public interface IPhotoFunc {
        void removePhoto(int pos, String path);
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 768, 1024);

        if (options.inSampleSize <= 1) {
            return null;
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}