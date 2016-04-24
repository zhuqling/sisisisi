package com.yizan.community.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.AdvInfo;
import com.fanwe.seallibrary.model.GoodInfo;
import com.fanwe.seallibrary.model.HomeConfig;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.result.HomeConfigResult;
import com.jude.rollviewpager.RollPagerView;
import com.tencent.map.geolocation.TencentLocationUtils;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.activity.BusinessClassificationActivity;
import com.yizan.community.activity.CateListActivity;
import com.yizan.community.activity.GoodDetailActivity;
import com.yizan.community.activity.PropertyActivity;
import com.yizan.community.activity.SellerDetailActivity;
import com.yizan.community.activity.SellerGoodsActivity;
import com.yizan.community.activity.SellerServicesActivity;
import com.yizan.community.activity.WebViewActivity;
import com.yizan.community.adapter.BannerPagerAdapter;
import com.yizan.community.adapter.ClassifyListAdapter;
import com.yizan.community.adapter.NoticeListAdapter;
import com.yizan.community.adapter.RecommentGoodsListAdapter;
import com.yizan.community.adapter.RecommentSellerListAdapter;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomePageFragment extends BaseFragment
        implements AdapterView.OnItemClickListener,
        View.OnClickListener,
        BannerPagerAdapter.IItemClick {
    private RollPagerView mRollViewPager;
    private GridView mClassifyGridView;
    private HomeConfig mHomeConfig;
    private boolean mIsLoading = false;
    private LocAddressInfo mAddressInfo;


    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    protected void initView() {
//        mAddressInfo = PreferenceUtils.getObject(getActivity(), UserAddressInfo.class);
//        if (mAddressInfo == null) {
//            ToastUtils.show(getActivity(), R.string.home_notice_sel_location);
//            return;
//        }
        mAddressInfo = PreferenceUtils.getObject(getActivity(), LocAddressInfo.class);
        initRetryState(0);
        loadData();
    }

    private void initRetryState(int state) {
        View container = mViewFinder.find(R.id.ll_retry_container);//数据加载失败的布局
        View btnRetry = mViewFinder.find(R.id.btn_retry);
        if (BuildConfig.USER_PROPERTY_GUARD) {
            mViewFinder.find(R.id.tv_shake_open_door).setVisibility(View.VISIBLE);
        }
        btnRetry.setOnClickListener(this);
        switch (state) {
            case 0:
                container.setVisibility(View.GONE);
                break;
            case 1:
                container.setVisibility(View.VISIBLE);
                btnRetry.setEnabled(true);
                break;
            case 2:
                btnRetry.setEnabled(false);
                break;
        }
    }

    private void initViewData(HomeConfig homeConfig) {
        mHomeConfig = homeConfig;
        if (mHomeConfig == null) {
            mViewFinder.find(R.id.sv_container).setVisibility(View.GONE);
            initRetryState(1);
            return;
        }
        initRetryState(0);
        mViewFinder.find(R.id.sv_container).setVisibility(View.VISIBLE);

        // 广告banner
        mRollViewPager = mViewFinder.find(R.id.roll_view_pager);
        mRollViewPager.setAdapter(new BannerPagerAdapter(getActivity(), mHomeConfig.banner, this));

        // 分类菜单----被移出的部分
        mClassifyGridView = mViewFinder.find(R.id.gv_classify);
        List<AdvInfo> advInfoList = new ArrayList<AdvInfo>();
        if (!ArraysUtils.isEmpty(mHomeConfig.menu) && mHomeConfig.menu.size() > 8) {
            advInfoList.addAll(mHomeConfig.menu.subList(0, 7));
            AdvInfo advInfo = new AdvInfo();
            advInfo.arg = "0";
            advInfo.type = -1;
            advInfoList.add(advInfo);
            mClassifyGridView.setAdapter(new ClassifyListAdapter(getActivity(), advInfoList));
        } else {
            mClassifyGridView.setAdapter(new ClassifyListAdapter(getActivity(), null == mHomeConfig.menu ? new ArrayList<AdvInfo>() : mHomeConfig.menu));
        }
        if (ArraysUtils.isEmpty(mHomeConfig.menu)) {
            mClassifyGridView.setVisibility(View.GONE);
        }

        mClassifyGridView.setOnItemClickListener(this);

        // 活动列表
        initNotices();

        // 商户列表
        initSellerList();

        // 商品列表
        initGoodsList();
    }

    private void initNotices() {
        if (ArraysUtils.isEmpty(mHomeConfig.notice)) {
            mViewFinder.find(R.id.ll_hor_notice).setVisibility(View.GONE);
            mViewFinder.find(R.id.line_hor_notice).setVisibility(View.GONE);
            mViewFinder.find(R.id.lv_notices).setVisibility(View.GONE);
            mViewFinder.find(R.id.line_list_notice).setVisibility(View.GONE);
            return;
        }

        ListView listView = mViewFinder.find(R.id.lv_notices);
        listView.setOnItemClickListener(this);
        if (mHomeConfig.notice.size() < 2) {
            mViewFinder.find(R.id.ll_hor_notice).setVisibility(View.GONE);
            mViewFinder.find(R.id.line_hor_notice).setVisibility(View.GONE);
            mViewFinder.find(R.id.lv_notices).setVisibility(View.VISIBLE);
            mViewFinder.find(R.id.line_list_notice).setVisibility(View.VISIBLE);

            listView.setAdapter(new NoticeListAdapter(getActivity(), mHomeConfig.notice));
        } else {
            int horWidth = ImgUrl.getScreenWidth() / 2;
            int horHeigth = horWidth * 3 / 4;
            View vLine = mViewFinder.find(R.id.v_hor_notice_line);
            ViewGroup.LayoutParams layoutParams = vLine.getLayoutParams();
            layoutParams.height = horHeigth;
            vLine.setLayoutParams(layoutParams);
            mViewFinder.find(R.id.ll_hor_notice).setVisibility(View.VISIBLE);
            mViewFinder.find(R.id.line_hor_notice).setVisibility(View.VISIBLE);
            NetworkImageView iv = mViewFinder.find(R.id.iv_notice_1);
            layoutParams = iv.getLayoutParams();
            layoutParams.height = horHeigth;
            iv.setLayoutParams(layoutParams);
            iv.setDefaultImageResId(R.drawable.ic_default_square);
            iv.setImageUrl(ImgUrl.formatUrl(horWidth, horHeigth, mHomeConfig.notice.get(0).image), RequestManager.getImageLoader());
            iv.setOnClickListener(this);
            iv.setTag(mHomeConfig.notice.get(0));

            iv = mViewFinder.find(R.id.iv_notice_2);
            layoutParams = iv.getLayoutParams();
            layoutParams.height = horHeigth;
            iv.setLayoutParams(layoutParams);
            iv.setOnClickListener(this);
            iv.setTag(mHomeConfig.notice.get(1));
            iv.setDefaultImageResId(R.drawable.ic_default_square);
            iv.setImageUrl(ImgUrl.formatUrl(horWidth, horHeigth, mHomeConfig.notice.get(1).image), RequestManager.getImageLoader());
            mViewFinder.find(R.id.lv_notices).setVisibility(View.GONE);
            mViewFinder.find(R.id.line_list_notice).setVisibility(View.GONE);
            if (mHomeConfig.notice.size() > 2) {
                mViewFinder.find(R.id.lv_notices).setVisibility(View.VISIBLE);
                mViewFinder.find(R.id.line_list_notice).setVisibility(View.VISIBLE);
                List<AdvInfo> list = new ArrayList<AdvInfo>(mHomeConfig.notice.subList(2, mHomeConfig.notice.size()));
                listView.setAdapter(new NoticeListAdapter(getActivity(), list));
            }


        }

    }

    private void initSellerList() {
        if (ArraysUtils.isEmpty(mHomeConfig.sellers)) {
            mViewFinder.find(R.id.ll_seller_head).setVisibility(View.GONE);
            mViewFinder.find(R.id.lv_recomment_sellers).setVisibility(View.GONE);
            return;
        }
        List<SellerInfo> list_seller=mHomeConfig.sellers;
        for (SellerInfo sellinfo:list_seller) {
            double distance = TencentLocationUtils.distanceBetween(sellinfo.mapPoint.x,
                    sellinfo.mapPoint.y, mAddressInfo.mapPoint.x, mAddressInfo.mapPoint.y);
            if (distance<20000){//距離小於20千米---success,获得距离内的商品
                Log.e("看一看",sellinfo.name);

            }
        }

        mViewFinder.onClick(R.id.tv_seller_more, this);
        mViewFinder.find(R.id.ll_seller_head).setVisibility(View.VISIBLE);
        mViewFinder.find(R.id.lv_recomment_sellers).setVisibility(View.VISIBLE);
        ListView lv = mViewFinder.find(R.id.lv_recomment_sellers);
        lv.setAdapter(new RecommentSellerListAdapter(getActivity(), mHomeConfig.sellers));
        lv.setOnItemClickListener(this);
    }

    private void initGoodsList() {
        //数据返回为空值
        if (ArraysUtils.isEmpty(mHomeConfig.goods)) {
            Log.e("youmeiyou","空值？");
            mViewFinder.find(R.id.ll_goods_head).setVisibility(View.GONE);
            mViewFinder.find(R.id.gv_recomment_goods).setVisibility(View.GONE);
            return;
        }

        List<GoodInfo> list_seller=mHomeConfig.goods;
        for (GoodInfo sellinfo:list_seller) {
          Log.e("youmeiyou",sellinfo.name);
        }

        mViewFinder.onClick(R.id.tv_goods_more, this);
        mViewFinder.find(R.id.ll_goods_head).setVisibility(View.VISIBLE);
        mViewFinder.find(R.id.gv_recomment_goods).setVisibility(View.VISIBLE);
        GridView gv = mViewFinder.find(R.id.gv_recomment_goods);
        gv.setAdapter(new RecommentGoodsListAdapter(getActivity(), mHomeConfig.goods));
        gv.setOnItemClickListener(this);
    }

    public void reflashData() {
        loadData();
    }

    private void loadData() {
        if (mIsLoading) {
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(getActivity())) {
            ToastUtils.show(getActivity(), R.string.loading_err_net);
            initRetryState(1);
            return;
        }
        mIsLoading = true;
        initRetryState(2);
        CustomDialogFragment.show(getFragmentManager(), R.string.loading, HomePageFragment.this.getTag());
        ApiUtils.post(getActivity(), URLConstants.HOME_CONFIG,
                new HashMap<String, String>(),
                HomeConfigResult.class, new Response.Listener<HomeConfigResult>() {

                    @Override
                    public void onResponse(final HomeConfigResult response) {
                        mIsLoading = false;
                        CustomDialogFragment.dismissDialog();
                        if (O2OUtils.checkResponse(getActivity(), response)) {

                        } else {

                        }
                        initViewData(response.data);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mIsLoading = false;
                        CustomDialogFragment.dismissDialog();
                        ToastUtils.show(getActivity(), R.string.loading_err_nor);
                        initViewData(null);
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.gv_classify:
                AdvInfo advInfo = (AdvInfo) parent.getAdapter().getItem(position);
                //
                if (advInfo.type == 0) {
//                    startActivity(new Intent(getActivity(), PropertyActivity.class));
                    PropertyActivity.getInstance(getActivity()).openFuncCheck(4);
                    return;
                }
                if (TextUtils.isEmpty(advInfo.arg)) {
                    return;
                }
                // 更多分类
                if (advInfo.type == -1) {
                    Intent intent = new Intent(getActivity(), CateListActivity.class);
                    startActivity(intent);
                } else {
                    openAdvActivity(advInfo);
                }
                break;
            case R.id.lv_notices:
                AdvInfo advInfo1 = (AdvInfo) parent.getAdapter().getItem(position);
                openAdvActivity(advInfo1);
                break;
            case R.id.lv_recomment_sellers:
                Intent intent = null;
                SellerInfo info = mHomeConfig.sellers.get(position);
                if (info.countGoods > 0 && info.countService <= 0) {
                    intent = new Intent(getActivity(), SellerGoodsActivity.class);
                } else if (info.countGoods <= 0 && info.countService > 0) {
                    intent = new Intent(getActivity(), SellerServicesActivity.class);
                } else {
                    intent = new Intent(getActivity(), SellerDetailActivity.class);
                }

                intent.putExtra(Constants.EXTRA_DATA, info.id);
                startActivity(intent);
                break;
        }
    }

    private void openAdvActivity(AdvInfo advInfo) {
        if (advInfo == null) {
            return;
        }
        try {
            Intent intent = null;
            switch (advInfo.type) {
                case 1:
                case 2:
                    intent = new Intent(getActivity(), BusinessClassificationActivity.class);
                    intent.putExtra(Constants.EXTRA_TITLE, advInfo.name);
                    intent.putExtra(Constants.EXTRA_DATA, advInfo.arg);
                    break;
                case 3: // 商品详情
                    intent = new Intent(getActivity(), GoodDetailActivity.class);
                    intent.putExtra(Constants.EXTRA_DATA, Integer.parseInt(advInfo.arg));
                    break;
                case 4: // 商家详情
                    intent = new Intent(getActivity(), SellerDetailActivity.class);
                    intent.putExtra(Constants.EXTRA_DATA, Integer.parseInt(advInfo.arg));
                    break;
                case 5:
                    intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra(Constants.EXTRA_URL, advInfo.arg);
                    intent.putExtra(Constants.EXTRA_TITLE, advInfo.name);
                    break;
                case 6: // 服务详情
                    intent = new Intent(getActivity(), GoodDetailActivity.class);
                    intent.putExtra(Constants.EXTRA_DATA, Integer.parseInt(advInfo.arg));
                    break;
                default:
                    ToastUtils.show(getActivity(), R.string.err_not_open);
                    return;
            }

            intent.putExtra(Constants.EXTRA_TYPE, advInfo.type);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_notice_1:
            case R.id.iv_notice_2:
                openAdvActivity((AdvInfo) v.getTag());
                break;
            case R.id.tv_goods_more:
                break;
            case R.id.tv_seller_more:
                Intent intent = new Intent(getActivity(), BusinessClassificationActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, "0");
                startActivity(intent);
                break;
            case R.id.btn_retry:
                loadData();
                break;
        }
    }

    @Override
    public void onPageItemClick(View v, Object data) {
        openAdvActivity((AdvInfo) data);
//        try {
//            AdvInfo advInfo = (AdvInfo) data;
//            if (advInfo == null) {
//                return;
//            }
//            Intent intent = null;
//            switch (advInfo.type) {
//                case 1:
//                case 2:
//                    intent = new Intent(getActivity(), BusinessClassificationActivity.class);
//                    intent.putExtra(Constants.EXTRA_TITLE, getActivity().getResources().getString(R.string.all));
//                    break;
//                case 3:
//                    intent = new Intent(getActivity(), OrderDetailActivity.class);
//                    break;
//                case 4:
//                    intent = new Intent(getActivity(), OrderDetailActivity.class);
//                    break;
//
//                case 5:
//                    intent = new Intent(getActivity(), WebViewActivity.class);
//                    intent.putExtra(Constants.EXTRA_URL, advInfo.arg);
//                    intent.putExtra(Constants.EXTRA_TITLE, advInfo.name);
//                    break;
//                default:
//                    ToastUtils.show(getActivity(), R.string.err_bad_param);
//                    return;
//            }
//            intent.putExtra(Constants.EXTRA_DATA, advInfo.arg);
//            intent.putExtra(Constants.EXTRA_TYPE, advInfo.type);
//            startActivity(intent);
//        } catch (Exception e) {
//
//        }
    }
}
