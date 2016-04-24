package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.JoinBusinessInfo;
import com.fanwe.seallibrary.model.PointInfo;
import com.fanwe.seallibrary.model.RegionLocal;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.req.SellerRegRequest;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.FileCache;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.utils.OSSUtils;
import com.yizan.community.widget.ImageSwitcherPopupWindow;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.bitmap.crop.CropHandler;
import com.zongyou.library.bitmap.crop.CropHelper;
import com.zongyou.library.bitmap.crop.CropParams;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.LogUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 我要开店Activity
 */
public class JoinBusinessActivity extends BaseActivity implements BaseActivity.TitleListener, OnClickListener, CropHandler{
    private static final String TAG = JoinBusinessActivity.class.getName();
    private static final int REQUEST_OPERATE = 110;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    uploadData();
                    break;
                case 1:
                    CustomDialogFragment.dismissDialog();
                    ToastUtils.show(JoinBusinessActivity.this, R.string.msg_failed_update);
                    break;
                case 0xFF:
                    try {
                        initRegionInfo();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }

    };
    private ImageSwitcherPopupWindow mPopupWinddow;
    private EditText mNameET, mAddressET, mIdCardET, mIntroductionET, mMobileET, mContactsET;
    private int id;
    private Uri mLogoUri, mCardUri1, mCardUri2, mLicencesUri;
    private CropParams mCropParams;
    private List<Uri> mUris = new ArrayList<Uri>(4);
    private TextView mJyTV;

    private Spinner mTypeSpinner;
    private int mType = 1;
    private NetworkImageView mLogoImage, mIdCardImage1, mIdCardImage2, mLicencesImage;
    private int index, count = 3;
    private String pathLogo, mPathCard1, mPathCard2, mPathLicence;
    private String mOperate;

    private PointInfo mSelPointInfo;
    private String  mShopRange;
    private RegionLocal mProvinceRegion, mCityRegion, mAreaRegion;

    private List<RegionLocal> mCityDatas;

    private JoinBusinessInfo mSellerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_business);
        setTitleListener(this);
        mCropParams = new CropParams();

        initView();
        OSSUtils.init(this);
        FileCache.init(this);
        initSeller();
    }


    private void initSeller() {
        JoinBusinessInfo info = (JoinBusinessInfo) getIntent().getParcelableExtra(Constants.EXTRA_DATA);
        if (null != info) {
            mSellerInfo = info;
            mTypeSpinner.setSelection(info.type - 1, true);
            if (!TextUtils.isEmpty(info.logo)) {
                mLogoUri = Uri.parse(info.logo);
                mLogoImage.setImageUrl(info.logo, RequestManager.getImageLoader());
            }
            mNameET.setText(info.name);
            mOperate = "";
            String names = "";
            if (!ArraysUtils.isEmpty(info.cateIds)) {
                for (SellerCatesInfo item : info.cateIds) {
                    if (!TextUtils.isEmpty(mOperate)) {
                        mOperate += ",";
                        names += ",";
                    }
                    mOperate += item.id;
                    names += item.name;
                }
                mJyTV.setText(names);
            }

            mIdCardET.setText(info.idcardSn);
            if (!TextUtils.isEmpty(info.idcardPositiveImg)) {
                mCardUri1 = Uri.parse(info.idcardPositiveImg);
                mIdCardImage1.setImageUrl(info.idcardPositiveImg, RequestManager.getImageLoader());
            }
            if (!TextUtils.isEmpty(info.idcardNegativeImg)) {
                mCardUri2 = Uri.parse(info.idcardNegativeImg);
                mIdCardImage2.setImageUrl(info.idcardNegativeImg, RequestManager.getImageLoader());
            }
            if (!TextUtils.isEmpty(info.certificateImg)) {
                mLicencesUri = Uri.parse(info.certificateImg);
                mLicencesImage.setImageUrl(info.certificateImg, RequestManager.getImageLoader());
            }
            mIntroductionET.setText(info.brief);

            mSelPointInfo = new PointInfo(info.mapPointStr);
            mSelPointInfo.address = info.address;

            mAddressET.setText(info.addressDetail);
            mViewFinder.setText(R.id.join_address_tv, info.address);

            mMobileET.setText(info.serviceTel);
            mContactsET.setText(info.contacts);
            initArea(info.mapPosStr);
            try {
                loadCitys();
                mProvinceRegion = info.province.toLocal();
                mCityRegion = info.city.toLocal();
                mAreaRegion = info.area.toLocal();
                initServiceRegion();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void initRegionInfo(){
        if(ArraysUtils.isEmpty(mCityDatas) || mSellerInfo == null){
            return;
        }
        if(mSellerInfo.provinceId == 0){
            return;
        }
        for (RegionLocal prov : mCityDatas){
            if(prov.i.compareTo(String.valueOf(mSellerInfo.provinceId)) != 0){
                continue;
            }
            mProvinceRegion = prov;
            if(!ArraysUtils.isEmpty(mProvinceRegion.child)){
                for (RegionLocal city: mProvinceRegion.child){
                    if(city.i.compareTo(String.valueOf(mSellerInfo.cityId)) != 0){
                        continue;
                    }
                    mCityRegion = city;
                    if(mSellerInfo.areaId == 0 || ArraysUtils.isEmpty(mCityRegion.child)){
                        mAreaRegion = new RegionLocal();
                        mAreaRegion.i = "0";
                        mAreaRegion.n = "";
                        break;
                    }
                    for (RegionLocal area: mCityRegion.child){
                        if(area.i.compareTo(String.valueOf(mSellerInfo.areaId)) != 0){
                            continue;
                        }
                        mAreaRegion = area;
                    }
                    break;
                }
            }
            break;
        }
        initServiceRegion();
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.join_business);
    }

    private void initView() {
        mViewFinder.onClick(R.id.join_business_ll, this);
        mViewFinder.onClick(R.id.join_logo_ll, this);
        mViewFinder.onClick(R.id.join_jy_ll, this);


        mViewFinder.onClick(R.id.join_licences_id1_ll, this);

        mViewFinder.onClick(R.id.join_submit, this);
        mViewFinder.onClick(R.id.join_licences_id2_ll, this);
        mViewFinder.onClick(R.id.join_licences_id1_ll, this);
        mViewFinder.onClick(R.id.join_address_tv, this);
        mViewFinder.onClick(R.id.join_range_tv, this);
        mViewFinder.onClick(R.id.join_area_tv, this);

        mNameET = mViewFinder.find(R.id.join_name_et);
        mAddressET = mViewFinder.find(R.id.join_street_et);
        mMobileET = mViewFinder.find(R.id.join_mobile_et);
        mContactsET = mViewFinder.find(R.id.shop_corporation_et);
//        UserInfo user = PreferenceUtils.getObject(this, UserInfo.class);
//        mMobileET.setText(user.mobile);

        mIdCardET = mViewFinder.find(R.id.join_idcard_et);

        mLogoImage = mViewFinder.find(R.id.join_logo_image);
        mIdCardImage1 = mViewFinder.find(R.id.join_card1);
        mIdCardImage2 = mViewFinder.find(R.id.join_card2);
        mLicencesImage = mViewFinder.find(R.id.join_licences);

        mIntroductionET = mViewFinder.find(R.id.join_introduction_et);
        mTypeSpinner = mViewFinder.find(R.id.join_type_spinner);
        mViewFinder.find(R.id.join_licences_ll).setOnClickListener(this);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mType = position + 1;
                initLicencesBlock();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mJyTV = mViewFinder.find(R.id.join_jy_tv);
        initLicencesBlock();
    }

    private boolean isBusinessReg() {
        if (mType == 2) {
            return true;
        }
        return false;
    }

    private void initLicencesBlock() {
        mViewFinder.find(R.id.join_licences_ll).setVisibility(isBusinessReg() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        AppUtils.hideSoftInput(this);
        switch (v.getId()) {
            case R.id.join_business_ll:
                break;
            case R.id.join_logo_ll:
                mCropParams.aspectX = CropParams.DEFAULT_ASPECT;
                mCropParams.aspectY = CropParams.DEFAULT_ASPECT;
                mCropParams.outputX = CropParams.DEFAULT_OUTPUT;
                mCropParams.outputY = CropParams.DEFAULT_OUTPUT;
                id = v.getId();
                if (null == mPopupWinddow)
                    mPopupWinddow = new ImageSwitcherPopupWindow(JoinBusinessActivity.this, JoinBusinessActivity.this, TAG);
                mPopupWinddow.show(getWindow().getDecorView());
                break;
            case R.id.join_jy_ll:
                startActivityForResult(new Intent(this, OperateListActivity.class), REQUEST_OPERATE);
                break;
            case R.id.join_licences_id1_ll:
            case R.id.join_licences_id2_ll:
            case R.id.join_licences_ll:
                mCropParams.aspectX = 8;
                mCropParams.aspectY = 6;
                mCropParams.outputX = 900;
                mCropParams.outputY = 600;
                id = v.getId();
                if (null == mPopupWinddow)
                    mPopupWinddow = new ImageSwitcherPopupWindow(JoinBusinessActivity.this, JoinBusinessActivity.this, TAG);
                mPopupWinddow.show(getWindow().getDecorView());
                break;
            case R.id.item1:
                mPopupWinddow.dismiss();
                    startActivityForResult(CropHelper.buildCaptureIntent(mCropParams.uri), CropHelper.REQUEST_CAMERA);
                break;
            case R.id.item2:
                mPopupWinddow.dismiss();
                    startActivityForResult(CropHelper.buildGalleryIntent(), CropHelper.REQUEST_GALLERY);
                break;
            case R.id.join_submit:
                commitData();
                break;
            case R.id.join_address_tv:
                Intent intent = new Intent(this, MapAddressActivity.class);
                startActivityForResult(intent, MapAddressActivity.REQUEST_CODE);
                break;
            case R.id.join_range_tv:
                Intent intent1 = new Intent(this, ShopRangeActivity.class);
                startActivityForResult(intent1, ShopRangeActivity.REQUEST_CODE);
                break;
            case R.id.join_area_tv:
                Intent intent2 = new Intent(this, EditShopRegionActivity.class);
                startActivityForResult(intent2, EditShopRegionActivity.REQUEST_CODE);
                break;
        }
    }

    private void commitData() {
        if (null == mLogoUri) {
            ToastUtils.show(this, R.string.msg_error_logo);
            return;
        }

        if (mNameET.length() == 0) {
            ToastUtils.show(this, R.string.msg_error_name);
            return;
        }
        if(mMobileET.length() == 0){
            ToastUtils.show(this, R.string.msg_error_tel);
            return;
        }
        if (getResources().getString(R.string.select_none).equals(mJyTV.getText().toString()) || mJyTV.length() == 0) {
            ToastUtils.show(this, R.string.msg_error_jy);
            return;
        }
        if(mSelPointInfo == null){
            ToastUtils.show(this, R.string.msg_error_store_addr);
        }

        if (mAddressET.length() == 0) {
            ToastUtils.show(this, R.string.msg_error_address);
            return;
        }
        if (mIdCardET.length() != 15 && mIdCardET.length() != 18) {
            ToastUtils.show(this, R.string.msg_error_idcard_type);
            return;
        }

        String contacts = mContactsET.getText().toString().trim();
        if(contacts.length() <= 0){
            ToastUtils.show(this, R.string.msg_error_bad_contacts);
            mContactsET.requestFocus();
            return;
        }

        if (null == mCardUri1 || null == mCardUri2) {
            ToastUtils.show(this, R.string.msg_error_idcard);
            return;
        }
        if (isBusinessReg()) {
            if (null == mLicencesUri) {
                ToastUtils.show(this, R.string.msg_error_licences);
                return;
            }
        } else {

        }

        if(mProvinceRegion == null || mCityRegion == null || mAreaRegion == null){
            ToastUtils.show(this, R.string.msg_error_area);
            return;
        }

        if(TextUtils.isEmpty(mShopRange)){
            ToastUtils.show(this,R.string.msg_error_server_area);
            return;
        }

        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.msg_error_network);
            return;
        }
        uploadPics();

    }

    private void uploadData() {

        UserInfo user = PreferenceUtils.getObject(this, UserInfo.class);

        SellerRegRequest data = new SellerRegRequest(mType,
                pathLogo,
                mNameET.getText().toString().trim(),
                mOperate,
                mSelPointInfo.address,
                user.mobile,
                null,
                mIdCardET.getText().toString(),
                mPathCard1,
                mPathCard2,
                mPathLicence,
                mIntroductionET.getText().toString().trim(),
                Integer.parseInt(mProvinceRegion.i),
                Integer.parseInt(mCityRegion.i),
                Integer.parseInt(mAreaRegion.i),
                mSelPointInfo.toString(),
                mShopRange,
                mAddressET.getText().toString().trim(),
                mContactsET.getText().toString().trim(),
                mMobileET.getText().toString());
        ApiUtils.post(this,
                URLConstants.USER_SELLER_REG, data, BaseResult.class, new Response.Listener<BaseResult>() {
                    @Override
                    public void onResponse(BaseResult response) {
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(JoinBusinessActivity.this, response)) {
                            ToastUtils.show(JoinBusinessActivity.this, R.string.msg_failed_update_success);
                            finishActivity();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(JoinBusinessActivity.this, R.string.msg_failed_update);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        for (Uri uri : mUris)
            CropHelper.clearCachedCropFile(uri);
        super.onDestroy();
    }

    private void uploadPics() {

        CustomDialogFragment.show(getSupportFragmentManager(),
                R.string.msg_loading_save, JoinBusinessActivity.class.getName());
        count = 4;
        index = 0;

        // upload image
        if ("http://".equals(mLogoUri.toString().substring(0, 7))) {
            pathLogo = mLogoUri.toString();
            if (++index == count)
                mHandler.sendEmptyMessage(0);
        } else
            OSSUtils.save(mLogoUri.getPath(), new SaveCallback() {
                @Override
                public void onProgress(String arg0, int arg1, int arg2) {
                }

                @Override
                public void onSuccess(String arg0) {
                    pathLogo = arg0;
                    if (++index == count)
                        mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onFailure(String arg0, OSSException arg1) {
                    mHandler.sendEmptyMessage(1);
                }
            });
        if ("http://".equals(mCardUri1.toString().substring(0, 7))) {
            mPathCard1 = mCardUri1.toString();
            if (++index == count)
                mHandler.sendEmptyMessage(0);
        } else
            OSSUtils.save(mCardUri1.getPath(), new SaveCallback() {
                @Override
                public void onProgress(String arg0, int arg1, int arg2) {
                }

                @Override
                public void onSuccess(String arg0) {
                    mPathCard1 = arg0;
                    if (++index == count)
                        mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onFailure(String arg0, OSSException arg1) {
                    mHandler.sendEmptyMessage(1);
                }
            });
        if ("http://".equals(mCardUri2.toString().substring(0, 7))) {
            mPathCard2 = mCardUri2.toString();
            if (++index == count)
                mHandler.sendEmptyMessage(0);
        } else
            OSSUtils.save(mCardUri2.getPath(), new SaveCallback() {
                @Override
                public void onProgress(String arg0, int arg1, int arg2) {
                }

                @Override
                public void onSuccess(String arg0) {
                    mPathCard2 = arg0;
                    if (++index == count)
                        mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onFailure(String arg0, OSSException arg1) {
                    mHandler.sendEmptyMessage(1);
                }
            });
        if (!isBusinessReg() || "http://".equals(mLicencesUri.toString().substring(0, 7))) {
            if(!isBusinessReg()){
                mPathLicence = "";
            }else {
                mPathLicence = mLicencesUri.toString();
            }
            if (++index == count)
                mHandler.sendEmptyMessage(0);
        } else
            OSSUtils.save(mLicencesUri.getPath(), new SaveCallback() {
                @Override
                public void onProgress(String arg0, int arg1, int arg2) {
                }

                @Override
                public void onSuccess(String arg0) {
                    mPathLicence = arg0;
                    if (++index == count)
                        mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onFailure(String arg0, OSSException arg1) {
                    mHandler.sendEmptyMessage(1);
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
            case CropHelper.REQUEST_CAMERA:
            case CropHelper.REQUEST_GALLERY:
            case CropHelper.REQUEST_CROP:
                CropHelper.handleResult(this, requestCode, resultCode, data);
                break;

            case REQUEST_OPERATE:
                mOperate = data.getStringExtra("data");
                mJyTV.setText(data.getStringExtra("dataName"));
                break;
            case MapAddressActivity.REQUEST_CODE:
                mSelPointInfo = (PointInfo)data.getSerializableExtra(Constants.EXTRA_DATA);
                mViewFinder.setText(R.id.join_address_tv, mSelPointInfo.address);
                break;
            case ShopRangeActivity.REQUEST_CODE:
                String shopRange = data.getStringExtra(Constants.EXTRA_DATA);
                try{
                    JSONObject obj = new JSONObject(shopRange);
                    initArea(obj.getString("mapPos"));
                }catch (Exception e){

                }
                break;
            case EditShopRegionActivity.REQUEST_CODE:
                try {
                    mProvinceRegion = (RegionLocal) data.getSerializableExtra("province");
                    mCityRegion = (RegionLocal) data.getSerializableExtra("city");
                    mAreaRegion = (RegionLocal) data.getSerializableExtra("area");
                    initServiceRegion();
                }catch (Exception e){
                    mProvinceRegion = null;
                    mCityRegion = null;
                    mAreaRegion = null;
                }
                break;

        }
    }
    private void initArea(String range){
        mShopRange = range;
        if(TextUtils.isEmpty(mShopRange)){
            mViewFinder.setText(R.id.join_range_tv, R.string.msg_un_setting_server_area);
        }else{
            mViewFinder.setText(R.id.join_range_tv, R.string.msg_yet_setting_server_area);
        }
    }
    private void initServiceRegion(){
        if(mProvinceRegion == null || mCityRegion == null){

            return;
        }
        String region = mProvinceRegion.n;
        region += "-";
        region += mCityRegion.n;
        if(mAreaRegion != null && !TextUtils.isEmpty(mAreaRegion.n)){
            region += "-";
            region += mAreaRegion.n;
        }
        mViewFinder.setText(R.id.join_area_tv, region);
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        if (null == uri)
            return;
        switch (id) {
            case R.id.join_logo_ll:
                mLogoImage.setImageURI(uri);
                mLogoUri = uri;
                break;

            case R.id.join_licences_id1_ll:
                mIdCardImage1.setImageURI(uri);
                mCardUri1 = uri;
                break;

            case R.id.join_licences_id2_ll:
                mIdCardImage2.setImageURI(uri);
                mCardUri2 = uri;
                break;
            case R.id.join_licences_ll:
                mLicencesImage.setImageURI(uri);
                mLicencesUri = uri;
                break;
        }
    }

    @Override
    public void onCropCancel() {

    }

    @Override
    public void onCropFailed(String message) {

    }

    @Override
    public CropParams getCropParams() {
        return mCropParams;
    }

    @Override
    public Activity getContext() {
        return this;
    }


    private void loadCitys() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCityDatas =  new GsonBuilder().create().fromJson(readCity(), new TypeToken<List<RegionLocal>>() {
                }.getType());
                if (mCityDatas != null && mCityDatas.size()>0) {
                    mHandler.sendEmptyMessage(0xFF);
                }
            }
        }).start();
    }

    private String readCity() {
        InputStream in = null;
        AssetManager assetManager = null;
        try {
            assetManager = getAssets();
            in = assetManager.open("city");
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) != -1) {
                out.append(new String(b, 0, n));
            }
            if (null != in)
                in.close();
            return out.toString();
        } catch (IOException e) {
            if (null != in)
                try {
                    in.close();
                } catch (IOException e1) {
                }
            LogUtils.e("asserts error", e);
        }
        if (null != assetManager)
            assetManager.close();
        return null;
    }


}
