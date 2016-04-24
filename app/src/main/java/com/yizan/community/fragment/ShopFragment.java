package com.yizan.community.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.SellerInfo;
import com.yizan.community.R;
import com.yizan.community.activity.SellerDetailActivity;
import com.yizan.community.activity.SellerGoodsActivity;
import com.yizan.community.activity.SellerServicesActivity;
import com.yizan.community.adapter.ShopAdapter;
import com.fanwe.seallibrary.model.CollectionInfo;
import com.fanwe.seallibrary.model.result.CollectionResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家ListFragment
 * Created by ztl on 2015/9/21.
 */
public class ShopFragment extends BaseFragment {

    private List<CollectionInfo> listDatas;
    private boolean mLoadMore;
    private ListView mListview;
    private View emptyView;
    private ShopAdapter adapter;

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.shop_layout, container, false);
    }

    @Override
    protected void initView() {

        listDatas = new ArrayList<CollectionInfo>();
        getShop(true);
        emptyView = mViewFinder.find(android.R.id.empty);
        mListview = mViewFinder.find(R.id.shop_lv);
        mListview.setEmptyView(emptyView);
        adapter = new ShopAdapter(mFragmentActivity, listDatas);
        mListview.setAdapter(adapter);
        mListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!mLoadMore && firstVisibleItem + visibleItemCount >= totalItemCount && adapter.getCount() >= Constants.PAGE_SIZE && adapter.getCount() % Constants.PAGE_SIZE == 0) {
                    getShop(false);
                }
            }
        });
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                CollectionInfo info = listDatas.get(position);
                if(info.countGoods > 0 && info.countService <= 0){
                    intent = new Intent(getActivity(), SellerGoodsActivity.class);
                }else if(info.countGoods <=0 && info.countService > 0){
                    intent = new Intent(getActivity(), SellerServicesActivity.class);
                }else{
                    intent = new Intent(getActivity(), SellerDetailActivity.class);
                }

                intent.putExtra(Constants.EXTRA_DATA, info.id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void getShop(final boolean isRefresh) {
        if (mLoadMore)
            return;
        if (!NetworkUtils.isNetworkAvaiable(mFragmentActivity)) {
            ToastUtils.show(mFragmentActivity, R.string.msg_error_network);
            return;
        }
        CustomDialogFragment.show(getFragmentManager(), R.string.msg_loading, mFragmentActivity.getClass().getName());
        Map<String, String> data = new HashMap<>();
        data.put("page", String.valueOf(listDatas.size() / Constants.PAGE_SIZE + 1));
        data.put("type", String.valueOf(2));
        mLoadMore = true;
        ApiUtils.post(mFragmentActivity, URLConstants.COLLECTIONLISTS, data, CollectionResult.class, new Response.Listener<CollectionResult>() {
            @Override
            public void onResponse(CollectionResult response) {
                if (isRefresh)
                    listDatas.clear();
                if (O2OUtils.checkResponse(mFragmentActivity, response)) {
                    if (!ArraysUtils.isEmpty(response.data))
                        listDatas.addAll(response.data);
                }
                adapter.notifyDataSetChanged();
                CustomDialogFragment.dismissDialog();
                mLoadMore = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                mLoadMore = false;
            }
        });
    }
}
