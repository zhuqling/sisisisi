package com.yizan.community.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jude.rollviewpager.RollPagerView;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.adapter.BannerPagerAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.yizan.community.comm.ShoppingCartMgr;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.AdvInfo;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.GoodInfo;
import com.fanwe.seallibrary.model.GoodsNorms;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.GoodResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.NumFormat;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.ShoppingCartView;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品详情Activity
 */
public class GoodDetailActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {

    private GoodInfo mGoodInfo;
    private boolean isCollect = false;
    private LayoutInflater inflater;
    private ShoppingCartView mShoppingCartView;
    private CartGoodsInfo mCartGoodsInfo;
    private String starttime;
    private int mGoodId;
    private ImageButton mTitleRightButton;
    private boolean mCartIsClicked = false;
    private int mCurrNormsId = 0;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_detail);

        mGoodId = this.getIntent().getIntExtra(Constants.EXTRA_DATA, 0);
        initViews();
        loadData();
        mCartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodId, 0);
        setTitleListener_RightImage(this);
        EventBus.getDefault().register(this);

    }
    private void squareHeadImage(){
        if(!BuildConfig.GOOD_DETAIL_SQUARE){
            return;
        }
        View v = mViewFinder.find(R.id.roll_view_pager);
        DisplayMetrics dm = new DisplayMetrics();
        //获取屏幕信息
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.height = screenWidth;
        v.setLayoutParams(layoutParams);
    }

    private void initWeb(String url) {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(url);
    }

    private void initViews() {
        mViewFinder.onClick(R.id.ll_norms, this);
        mViewFinder.onClick(R.id.ll_service_time, this);
        mViewFinder.onClick(R.id.ll_detail, this);
        mViewFinder.onClick(R.id.iv_sub, this);
        mViewFinder.onClick(R.id.iv_add, this);
        mViewFinder.onClick(R.id.iv_cart, this);
        mShoppingCartView = mViewFinder.find(R.id.layout_order_select);
        mWebView= (WebView) findViewById(R.id.detail_web);
        squareHeadImage();
    }


    private void loadData() {
        UserInfo user = PreferenceUtils.getObject(GoodDetailActivity.this, UserInfo.class);
        Map<String, String> map = new HashMap<>();
        map.put("goodsId", String.valueOf(mGoodId));
        map.put("userId", String.valueOf(user.id));
        // 加载数据
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.loading_err_net);
            return;
        }
        CustomDialogFragment.show(this.getSupportFragmentManager(), R.string.loading, this.getLocalClassName());
        ApiUtils.post(GoodDetailActivity.this, URLConstants.GOODS_DETAIL
                , map, GoodResult.class, new Response.Listener<GoodResult>() {
            @Override
            public void onResponse(GoodResult response) {
                CustomDialogFragment.dismissDialog();
                if(O2OUtils.checkResponse(GoodDetailActivity.this, response)){
                    initViewData(response.data);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(GoodDetailActivity.this, R.string.msg_error);
                CustomDialogFragment.dismissDialog();
            }
        });
    }

    private int getSelNum() {
        TextView tv = mViewFinder.find(R.id.tv_num);
        int num = Integer.parseInt(tv.getText().toString());
        return num;
    }

    private String getServiceTime() {
        return ((TextView)mViewFinder.find(R.id.tv_service_time)).getText().toString();
    }

    // TODO: 这样获取规格ID有问题
    private int getNormsId() {
        if (mCartGoodsInfo == null) {
            mCartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodId, 0);
        }
        if (mCartGoodsInfo == null) {
            return 0;
        } else {
            return mCartGoodsInfo.normsId;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initViewData(GoodInfo goodInfo){
        if(goodInfo == null){
            return;
        }
        setTitle(goodInfo.name);
        mGoodInfo = goodInfo;
        mViewFinder.setText(R.id.tv_good_name, mGoodInfo.name);
        mViewFinder.setText(R.id.tv_price,String.format(getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(mGoodInfo.price)));

        initCartState();

        if(ArraysUtils.isEmpty(mGoodInfo.images)){
            mViewFinder.find(R.id.roll_view_pager).setVisibility(View.GONE);
        }else {
            List<AdvInfo> list = new ArrayList<AdvInfo>();
            for (int i=0; i<mGoodInfo.images.size(); i++){
                AdvInfo item = new AdvInfo();
                item.image = mGoodInfo.images.get(i);
                list.add(item);
            }
            RollPagerView rollViewPager = mViewFinder.find(R.id.roll_view_pager);
            rollViewPager.setAdapter(new BannerPagerAdapter(this, list, null));
        }

        initGoodNorms();

        mViewFinder.find(R.id.ll_other_container).setVisibility(View.VISIBLE);
        if(mGoodInfo.type == 1){
            mViewFinder.find(R.id.ll_service_time).setVisibility(View.GONE);
            mViewFinder.find(R.id.line_service_time).setVisibility(View.GONE);
        }else {
        }

        if(mTitleRightButton != null) {
            isCollect = mGoodInfo.iscollect==1;
            mTitleRightButton.setImageResource(isCollect?R.drawable.heart_red:R.drawable.heart);
        }
        mShoppingCartView.setSellerInfo(mGoodInfo.seller);

        if(mGoodInfo.salesCount > 0){
            mViewFinder.setText(R.id.tv_sales_num, getString(R.string.hint_sales_num) + mGoodInfo.salesCount);
        }else{
            mViewFinder.setText(R.id.tv_sales_num, "");
        }
        initWeb(mGoodInfo.url);
    }

    private void initCartState() {
        int normsId = 0;
        if(mGoodInfo == null){
            return;
        }
        if(!ArraysUtils.isEmpty(mGoodInfo.norms) && mGoodInfo.norms.size() == 1){
            normsId = mGoodInfo.norms.get(0).id;
        }
        if(isCanSel()){
            mCartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodInfo.id, normsId);
            if(mCartGoodsInfo != null || mCartIsClicked){
                if(mCartGoodsInfo != null) {
                    mViewFinder.setText(R.id.tv_num, String.valueOf(mCartGoodsInfo.num));
                }else{
                    mViewFinder.setText(R.id.tv_num, String.valueOf(0));
                }
                mViewFinder.find(R.id.ll_sel_container).setVisibility(View.VISIBLE);
                mViewFinder.find(R.id.iv_cart).setVisibility(View.GONE);
            }else {
                mViewFinder.find(R.id.ll_sel_container).setVisibility(View.GONE);
                mViewFinder.find(R.id.iv_cart).setVisibility(View.VISIBLE);
            }
        }else{
            mViewFinder.find(R.id.ll_sel_container).setVisibility(View.GONE);
            mViewFinder.find(R.id.iv_cart).setVisibility(View.VISIBLE);
        }
    }

    private boolean isCanSel() {
        if(mGoodInfo == null){
            return false;
        }
        if(ArraysUtils.isEmpty(mGoodInfo.norms) || mGoodInfo.norms.size() <= 0){
            mCurrNormsId = 0;
            return true;
        }
        if(mGoodInfo.norms.size() == 1){
            mCurrNormsId = mGoodInfo.norms.get(0).id;
            return true;
        }
        return false;
    }
    private boolean isServiceGood(){
        if(mGoodInfo != null && mGoodInfo.type == 2){
            return true;
        }
        return false;
    }
    @Override
    public void onClick(View v) {
        if(mGoodInfo == null){
            return;
        }
        String time = getServiceTime();
        switch (v.getId()) {
            case R.id.iv_sub:
                int num = getSelNum();

                if(TextUtils.isEmpty(time) && isServiceGood()){
                    ToastUtils.show(this, R.string.select_server_time);
                    return;
                }
                num -= 1;
                if (num >= 0) {
                    if (ShoppingCartMgr.getInstance().saveShopping(this, mGoodInfo.id, mCurrNormsId, time, num)) {
                        mViewFinder.setText(R.id.tv_num, String.valueOf(num));
                    }
                }
                break;
            case R.id.iv_add:
                int num1 = getSelNum();
                if(TextUtils.isEmpty(time) && isServiceGood()){
                    ToastUtils.show(this, R.string.select_server_time);
                    return;
                }
                num1 += 1;
                if (num1 >= 0) {
                    if (ShoppingCartMgr.getInstance().saveShopping(this, mGoodInfo.id, mCurrNormsId, time, num1)) {
                        mViewFinder.setText(R.id.tv_num, String.valueOf(num1));
                    }
                }
                break;
            case R.id.ll_norms:
                Intent intent = new Intent(this, GoodNormsActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, mGoodInfo);
                startActivity(intent);
                break;
            case R.id.ll_service_time:
//                DateTimePickDialogUtil date = new DateTimePickDialogUtil(GoodDetailActivity.this, starttime);
//                date.dateTimePicKDialog(mTvTime);
//                starttime = mTvTime.getText().toString();
                selDate();
                break;
//            case R.id.ll_detail:
//                Intent detailIntent = new Intent(this, WebViewActivity.class);
//                detailIntent.putExtra(Constants.EXTRA_TITLE, mGoodInfo.name);
//                detailIntent.putExtra(Constants.EXTRA_URL, mGoodInfo.url);
//                startActivity(detailIntent);
//                break;
            case R.id.iv_cart:

                if(mGoodInfo != null) {
                    if(isCanSel()){
                        if(TextUtils.isEmpty(time) && isServiceGood()){
                            ToastUtils.show(this, R.string.select_server_time);
                            return;
                        }
                        mCartIsClicked = true;
                        initCartState();
                        int num2 = getSelNum() + 1;
                        if (num2 >= 0) {
                            if (ShoppingCartMgr.getInstance().saveShopping(this, mGoodInfo.id, mCurrNormsId, time, num2)) {
                                mViewFinder.setText(R.id.tv_num, String.valueOf(num2));
                            }
                        }
                    } else{
                        Intent intent2 = new Intent(this, GoodNormsActivity.class);
                        intent2.putExtra(Constants.EXTRA_DATA, mGoodInfo);
                        startActivity(intent2);
                    }
                }
                break;
        }
    }


    private void setCollect() {
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, GoodDetailActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(mGoodInfo.id));
        data.put("type", String.valueOf(1));
        ApiUtils.post(GoodDetailActivity.this, URLConstants.COLLECT_CREATE
                , data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
                if (O2OUtils.checkResponse(GoodDetailActivity.this, response)) {

                }
                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(GoodDetailActivity.this, R.string.msg_error);
                CustomDialogFragment.dismissDialog();
            }
        });
    }

    @Override
    public void setTitle(TextView title, ImageButton left, final View right) {
        title.setText(getString(R.string.product_detail));
        ((ImageButton) right).setImageResource(R.drawable.heart);
        mTitleRightButton = (ImageButton)right;
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(O2OUtils.turnLogin(GoodDetailActivity.this)){
                    return;
                }
                if(mGoodInfo == null){
                    return;
                }
                if (isCollect) {
                    ((ImageButton) right).setImageResource(R.drawable.heart);
                    isCollect = false;
                    deleteCollect();
                } else {
                    ((ImageButton) right).setImageResource(R.drawable.heart_red);
                    isCollect = true;
                    setCollect();
                }
            }
        });
    }

    private void deleteCollect() {
        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, GoodDetailActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(mGoodInfo.id));
        data.put("type", String.valueOf(1));
        ApiUtils.post(GoodDetailActivity.this, URLConstants.COLLECTIONDELETE, data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
                switch (response.code) {
                    case 0:
                        ToastUtils.show(GoodDetailActivity.this, R.string.collection_delete_success);
                        break;
                    case 10503:
                        ToastUtils.show(GoodDetailActivity.this, R.string.collection_delete_error);
                        break;

                }
                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(GoodDetailActivity.this, R.string.msg_error);
            }
        });
    }

    public void onEventMainThread(ShoppingCartEvent event) {
        mCartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodId, mCurrNormsId);
        if(mCartGoodsInfo != null){
            mViewFinder.setText(R.id.tv_num, String.valueOf(mCartGoodsInfo.num));
            if(mGoodInfo != null){
                initGoodNorms();
            }
        }
    }

    private void initGoodNorms() {
        if(ArraysUtils.isEmpty(mGoodInfo.norms)){
            mViewFinder.find(R.id.ll_norms).setVisibility(View.GONE);
            mViewFinder.find(R.id.line_norms).setVisibility(View.GONE);
        }else{
            mViewFinder.setText(R.id.tv_norms, mGoodInfo.norms.get(0).name);
            if(mCartGoodsInfo != null){
                for (GoodsNorms norms : mGoodInfo.norms){
                    if(norms.id == mCartGoodsInfo.normsId){
                        mViewFinder.setText(R.id.tv_norms, norms.name);
                        break;
                    }
                }
            }
        }
    }

    private void selDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_date_time, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        final TimePicker timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker);
        builder.setView(view);

        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(System.currentTimeMillis());
        cal.setTimeInMillis(System.currentTimeMillis() + 15 * 60 * 1000);
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));



        builder.setTitle(R.string.select_server_time);
        builder.setPositiveButton(R.string.sure_plus_null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                StringBuffer sb = new StringBuffer();
                sb.append(String.format(getResources().getString(R.string.time_format),
                        datePicker.getYear(),
                        datePicker.getMonth() + 1,
                        datePicker.getDayOfMonth()));
                sb.append(" ");
                sb.append(timePicker.getCurrentHour())
                        .append(":").append(timePicker.getCurrentMinute()).append(":00");

                SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.time_format_2));
                try {
                    Date date = format.parse(sb.toString());
                    Date nowDate = new Date();
                    if (date.compareTo(nowDate) == 1) {
                        mViewFinder.setText(R.id.tv_service_time, sb.toString());
                        dialog.cancel();
                    } else {
                        ToastUtils.show(GoodDetailActivity.this, R.string.select_server_time);
                    }

                } catch (Exception e) {

                }

            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }
}
