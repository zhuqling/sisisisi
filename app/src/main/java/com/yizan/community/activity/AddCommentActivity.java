package com.yizan.community.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.event.OrderEvent;
import com.fanwe.seallibrary.model.req.RateCreateRequest;
import com.fanwe.seallibrary.model.result.OrderResult;
import com.yizan.community.R;
import com.yizan.community.adapter.PhotoGridAdapter;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.CacheUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.utils.OSSUtils;
import com.yizan.community.widget.ImageSwitcherPopupWindow;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.bitmap.crop.CropHelper;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.volley.RequestManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddCommentActivity extends BaseActivity implements BaseActivity.TitleListener, OnClickListener {
    private final String POP_TAG = "photo";
    private final int REQUEST_CAMERA = 0x101;
    private final int REQUEST_ALBUM = 0x102;
    public static final int REQUEST_CODE = 0x199;

    private OrderInfo mOrderInfo;
    private int mCurrScore = 5;
    private PhotoGridAdapter mPhotoGridAdapter;

    private ImageSwitcherPopupWindow mPopupWinddow;
    private Uri mCurrUri;
    private ArrayList<ImageView> mImages;
    private ArrayList<String> mPhotoUris = new ArrayList<String>();
    private List<String> mPhotoKeys = new ArrayList<String>();

    private List<String> mTmpFile = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        mOrderInfo = (OrderInfo) this.getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        setTitleListener(this);
        initView();
        OSSUtils.init(this);
    }


    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.evaluate_grade_text);
    }

    private void initView() {
        if (mOrderInfo == null) {
            finish();
            return;
        }
        mViewFinder.setText(R.id.tv_sn, mOrderInfo.sn);

        initGoodsImage(R.id.order_img0, 0);
        initGoodsImage(R.id.order_img1, 1);
        initGoodsImage(R.id.order_img2, 2);
        initGoodsImage(R.id.order_img3, 3);
        String str = getString(R.string.count_arg, String.valueOf(mOrderInfo.count));
        SpannableString sp = new SpannableString(str);
        sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.theme_main_text)), 1, str.length() - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mViewFinder.setText(R.id.tv_goods_num, sp);

        mViewFinder.onClick(R.id.iv_star1, this);
        mViewFinder.onClick(R.id.iv_star2, this);
        mViewFinder.onClick(R.id.iv_star3, this);
        mViewFinder.onClick(R.id.iv_star4, this);
        mViewFinder.onClick(R.id.iv_star5, this);
        mViewFinder.onClick(R.id.ll_anonymous, this);
        mViewFinder.onClick(R.id.btn_commit, this);
        GridView gv = mViewFinder.find(R.id.gv_pics);
        mPhotoGridAdapter = new PhotoGridAdapter(this, new ArrayList<String>(), 8, true);
        gv.setAdapter(mPhotoGridAdapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoGridAdapter adapter = (PhotoGridAdapter) parent.getAdapter();
                if (TextUtils.isEmpty(adapter.getItem(position))) {
                    mPopupWinddow = new ImageSwitcherPopupWindow(
                            AddCommentActivity.this, AddCommentActivity.this,
                            POP_TAG);
                    mPopupWinddow.show(parent);
                    return;
                }
            }
        });
    }

    private void initGoodsImage(int imgId, int index) {
        String imgUrl = "";
        if (!ArraysUtils.isEmpty(mOrderInfo.cartSellers) && mOrderInfo.cartSellers.size() > index) {
            imgUrl = mOrderInfo.cartSellers.get(index).goodsImages;
        }
        if (TextUtils.isEmpty(imgUrl)) {
            if (!ArraysUtils.isEmpty(mOrderInfo.goodsImages) && mOrderInfo.goodsImages.size() > index) {
                imgUrl = mOrderInfo.goodsImages.get(index);
            }
        }
        if (TextUtils.isEmpty(imgUrl)) {
            mViewFinder.find(imgId).setVisibility(View.GONE);
            return;
        }
        NetworkImageView iv = mViewFinder.find(imgId);
        iv.setVisibility(View.VISIBLE);
        iv.setDefaultImageResId(R.drawable.ic_default_square);
        iv.setErrorImageResId(R.drawable.ic_default_square);
        iv.setImageUrl(imgUrl, RequestManager.getImageLoader());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_star1:
                initStar(1);
                break;
            case R.id.iv_star2:
                initStar(2);
                break;
            case R.id.iv_star3:
                initStar(3);
                break;
            case R.id.iv_star4:
                initStar(4);
                break;
            case R.id.iv_star5:
                initStar(5);
                break;
            case R.id.ll_anonymous:
                setAnonymous(!isAnonymous());
                break;
            case R.id.item1:
                mPopupWinddow.dismiss();
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                mCurrUri = genImageUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrUri);
                startActivityForResult(intent, REQUEST_CAMERA);
                break;
            case R.id.item2:
                mPopupWinddow.dismiss();
                Intent intent1 = new Intent();
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent1, REQUEST_ALBUM);

                break;
            case R.id.btn_commit:
                commitData();
                break;
        }
    }

    private boolean isAnonymous() {
        CheckBox cb = mViewFinder.find(R.id.cb_anonymous);
        return cb.isChecked();
    }

    private void setAnonymous(boolean isAnonymous) {
        CheckBox cb = mViewFinder.find(R.id.cb_anonymous);
        cb.setChecked(isAnonymous);
    }

    private void initStar(int score) {
        mCurrScore = score;
        if (score < 1) {
            mViewFinder.setDrawable(R.id.iv_star1, R.drawable.ic_star_off_big);
        } else {
            mViewFinder.setDrawable(R.id.iv_star1, R.drawable.ic_star_on_big);
        }
        if (score < 2) {
            mViewFinder.setDrawable(R.id.iv_star2, R.drawable.ic_star_off_big);
        } else {
            mViewFinder.setDrawable(R.id.iv_star2, R.drawable.ic_star_on_big);
        }
        if (score < 3) {
            mViewFinder.setDrawable(R.id.iv_star3, R.drawable.ic_star_off_big);
        } else {
            mViewFinder.setDrawable(R.id.iv_star3, R.drawable.ic_star_on_big);
        }
        if (score < 4) {
            mViewFinder.setDrawable(R.id.iv_star4, R.drawable.ic_star_off_big);
        } else {
            mViewFinder.setDrawable(R.id.iv_star4, R.drawable.ic_star_on_big);
        }
        if (score < 5) {
            mViewFinder.setDrawable(R.id.iv_star5, R.drawable.ic_star_off_big);
        } else {
            mViewFinder.setDrawable(R.id.iv_star5, R.drawable.ic_star_on_big);
        }
    }

    private String getContent() {
        EditText et = mViewFinder.find(R.id.et_content);
        return et.getText().toString().trim();
    }

    private void commitData() {
        if (getContent().length() < 5) {
            ToastUtils.show(this, R.string.msg_error_evaluate_content);
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
            return;
        }
        uploadPics();

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

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Calculate inSampleSize
        options.inSampleSize = 1;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    private Uri genImageUri() {
        return CacheUtils.createTmpJpgURI();
    }

    /**
     * 保存方法
     */
    public String saveBitmap(Bitmap bm) {
        File f = new File(genImageUri().getPath());
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
        return f.getAbsolutePath();
    }

    private void addPhoto(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        Bitmap bmp = getSmallBitmap(path);
        if (null != bmp) {
            String tmp = saveBitmap(bmp);
            if (!TextUtils.isEmpty(tmp)) {
                mTmpFile.add(tmp);
                path = tmp;
            }
        } else {
            bmp = getBitmap(path);
        }


        mPhotoGridAdapter.addItem(path);
        ImageView image = new ImageView(this);
        try {
            image.setImageBitmap(bmp);
            image.setTag(path);
            mImages.add(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadPics() {


        mPhotoUris.clear();
        mPhotoKeys.clear();
        List<String> datas = mPhotoGridAdapter.getDatas();
        for (int i = 0; i < datas.size(); i++) {
            if (TextUtils.isEmpty(datas.get(i))) {
                continue;
            }
            mPhotoUris.add(Uri.parse(datas.get(i)).getPath());

        }

        CustomDialogFragment.show(getSupportFragmentManager(),
                R.string.msg_loading, AddCommentActivity.class.getName());
        if (mPhotoUris.size() > 0) {

            final int size = mPhotoUris.size();

            for (int i = 0; i < size; i++) {
                String path = mPhotoUris.get(i);

                // upload image
                final int index = i;
                OSSUtils.save(path, new SaveCallback() {
                    @Override
                    public void onProgress(String arg0, int arg1, int arg2) {
                    }

                    @Override
                    public void onSuccess(String arg0) {
                        mPhotoKeys.add(arg0);
                        if (index == size - 1)
                            mHandler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onFailure(String arg0, OSSException arg1) {
                        mHandler.sendEmptyMessage(1);
                    }
                });
            }
        } else {
            mHandler.sendEmptyMessage(0);
        }
    }


    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(AddCommentActivity.this,
                        R.string.msg_failed_add);
            } else {
                uploadData();
//                Intent intent = new Intent();
//                intent.putExtra(Constants.EXTRA_DATA, mRequest);
//                setResult(Activity.RESULT_OK, intent);
//                finish();
            }
        }

    };


    protected void uploadData() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            return;
        }

        RateCreateRequest rateCreateRequest = new RateCreateRequest();
        rateCreateRequest.content = getContent();
        rateCreateRequest.orderId = mOrderInfo.id;
        rateCreateRequest.star = mCurrScore;
        rateCreateRequest.isAno = isAnonymous() ? 1 : 0;
        rateCreateRequest.images = mPhotoKeys;
        ApiUtils.post(this, URLConstants.SELLER_RATE_CREATE, rateCreateRequest, OrderResult.class, new Response.Listener<OrderResult>() {
            @Override
            public void onResponse(OrderResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(AddCommentActivity.this, response)) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.EXTRA_DATA, response.data);
                    setResult(Activity.RESULT_OK, intent);
                    ToastUtils.show(AddCommentActivity.this, R.string.evaluate_win_hint);
                    EventBus.getDefault().post(new OrderEvent(null));
                    finish();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(AddCommentActivity.this, R.string.msg_error);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {

            case REQUEST_CAMERA:
                mTmpFile.add(mCurrUri.getPath());
                addPhoto(mCurrUri.getPath());
                break;
            case REQUEST_ALBUM:
                Uri photoUri = data.getData();
                if (Build.VERSION.SDK_INT >= 19) {
                    String url = CropHelper.getPath(this, photoUri);
                    photoUri = Uri.fromFile(new File(url));
                }

                if (photoUri == null) {
                    ToastUtils.show(this, R.string.evaluate_image_select_lose);
                    return;
                }
                addPhoto(getRealFilePath(AddCommentActivity.this,
                        photoUri));
                break;

        }
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
