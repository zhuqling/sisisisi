package com.yizan.community.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.yizan.community.R;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.result.UserResultInfo;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.utils.OSSUtils;
import com.yizan.community.widget.ImageSwitcherPopupWindow;
import com.zongyou.library.bitmap.crop.CropHandler;
import com.zongyou.library.bitmap.crop.CropHelper;
import com.zongyou.library.bitmap.crop.CropParams;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ztl on 2015/9/21.
 */
public class UserEditActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener, CropHandler {
    private NetworkImageView mHeadImageView;
    private ImageSwitcherPopupWindow mPopupWinddow;
    private CropParams mCropParams;
    private ArrayList<Uri> mPhotoUris = new ArrayList<Uri>(4);
    private List<String> mPhotoKeys = new ArrayList<String>(4);
    private Uri mHeadUri;
    private String mHeadKey;
    private UserInfo mUserInfo;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap<String, Object> data = new HashMap<String, Object>();
            if (!TextUtils.isEmpty(mHeadKey)) {
                data.put("avatar", mHeadKey);
            }
            data.put("name", mUserInfo.name);


            ApiUtils.post(UserEditActivity.this, URLConstants.STAFF_UPDATE,
                    data, UserResultInfo.class,
                    new Response.Listener<UserResultInfo>() {
                        @Override
                        public void onResponse(UserResultInfo response) {
                            if (O2OUtils.checkResponse(UserEditActivity.this,
                                    response)) {
                                ToastUtils.show(UserEditActivity.this,
                                        R.string.msg_success_save);
                                mUserInfo = response.data;
                                PreferenceUtils.setObject(UserEditActivity.this, mUserInfo);
                                initViewData();
                                Intent intent = new Intent();
                                setResult(RESULT_OK,intent);
//                                finishActivity();
                            }
                            CustomDialogFragment.dismissDialog();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            CustomDialogFragment.dismissDialog();
                            ToastUtils.show(UserEditActivity.this,
                                    R.string.msg_failed_update);
                        }
                    });

        }

    };

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_more_personcenter);
        OSSUtils.init(this);
        mCropParams = new CropParams();
        mCropParams.outputX = 200;
        mCropParams.outputY = 200;
        initView();
        setTitleListener(this);
        setResult(Activity.RESULT_OK);
    }

    private void initView() {

        mHeadImageView = mViewFinder.find(R.id.head1);

        mViewFinder.onClick(R.id.ll_pwd, this);
        mViewFinder.onClick(R.id.ll_name, this);
        mViewFinder.onClick(R.id.ll_phone, this);

        setViewClickListener(R.id.headportrait_ll, this);
        initViewData();
    }

    private void initViewData() {
        mUserInfo = PreferenceUtils.getObject(this, UserInfo.class);
        mHeadImageView.setImageUrl(mUserInfo.avatar,
                RequestManager.getImageLoader());
        mHeadImageView.setDefaultImageResId(R.drawable.no_headportaint);
        mViewFinder.setText(R.id.staff_name, mUserInfo.name);
        mViewFinder.setText(R.id.staff_mobile, mUserInfo.mobile);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.user_edit_title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CropHelper.REQUEST_CAMERA:
            case CropHelper.REQUEST_GALLERY:
            case CropHelper.REQUEST_CROP:
                CropHelper.handleResult(this, requestCode, resultCode, data);
                break;


        }
    }

    @Override
    public void onClick(View arg0) {
        hideSoftInputView();
        switch (arg0.getId()) {
            case R.id.headportrait_ll:

                mPopupWinddow = new ImageSwitcherPopupWindow(this, this, "");
                mPopupWinddow.show(arg0);
                break;
            case R.id.item1:
                mPopupWinddow.dismiss();
                startActivityForResult(CropHelper.buildCaptureIntent(mCropParams.uri), CropHelper.REQUEST_CAMERA);
                break;
            case R.id.item2:
                mPopupWinddow.dismiss();
                startActivityForResult(CropHelper.buildGalleryIntent(), CropHelper.REQUEST_GALLERY);
                break;
            case R.id.ll_name:
                startActivity(new Intent(this, UserNickActivity.class));
                break;
            case R.id.ll_pwd:
                startActivity(new Intent(this, ChangePwdActivity.class));
                break;
            case R.id.ll_phone:
                startActivity(new Intent(this, UserBindPhoneActivity.class));
                break;

        }
    }

    @Override
    public void onPhotoCropped(Uri uri) {
               mHeadImageView.setImageURI(uri);
        mHeadImageView.setTag(uri.getPath());
        mHeadUri = mCropParams.uri ;
        uploadData();
    }

    @Override
    public void onCropCancel() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCropFailed(String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public CropParams getCropParams() {
        return mCropParams;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public Activity getContext() {
        // TODO Auto-generated method stub
        return this;
    }

    private void uploadData() {
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
            return;
        }
        mPhotoUris.clear();
        mPhotoKeys.clear();

        CustomDialogFragment.show(getSupportFragmentManager(),
                R.string.msg_loading_save, UserEditActivity.class.getName());
        uploadHead();
    }

    private void uploadHead() {
        if (mHeadUri == null) {
            uploadPhotos();
            return;
        }
        OSSUtils.save(mHeadImageView.getTag().toString(), new SaveCallback() {
            @Override
            public void onProgress(String arg0, int arg1, int arg2) {
            }

            @Override
            public void onSuccess(String arg0) {
                setResult(RESULT_OK);
                mHeadKey = arg0;
                uploadPhotos();
            }

            @Override
            public void onFailure(String arg0, OSSException arg1) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(UserEditActivity.this,
                        R.string.msg_failed_update);
            }
        });
    }

    private void uploadPhotos() {
        if (mPhotoUris.size() > 0) {
            final int size = mPhotoUris.size();
            for (int i = 0; i < size; i++) {
                final int index = i;
                OSSUtils.save(mPhotoUris.get(i).getPath(), new SaveCallback() {
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
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(UserEditActivity.this,
                                R.string.msg_failed_update);
                    }
                });
            }
        } else {
            mHandler.sendEmptyMessage(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViewData();
    }
}
