package com.yizan.community.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.GoodsPackInfo;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;
import com.fanwe.seallibrary.model.result.ArticleResult;
import com.fanwe.seallibrary.model.result.SellerGoodsResult;
import com.yizan.community.R;
import com.yizan.community.activity.GoodDetailActivity;
import com.yizan.community.adapter.GoodPackListAdapter;
import com.yizan.community.adapter.SellerGoodsAdapter;
import com.yizan.community.dialog.NoticeDialog;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.ShoppingCartView;
import com.yizan.community.widget.stickylistheaders.StickyListHeadersListView;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.LogUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.List;

public class SellerGoodsFragment extends BaseFragment {
    private final int GOOD_DETAIL_REQUEST_CODE = 0x201;
    private SellerInfo mSellerInfo;
    private List<GoodsPackInfo> mGoodsPackInfoList;
    private SellerGoodsAdapter mGoodListAdapter;
    private GoodPackListAdapter mGoodPackListAdapter;
    private ShoppingCartView mShoppingCartView;
    private StickyListHeadersListView mListView;
    private boolean mIsDetach = false;

    public static SellerGoodsFragment newInstance(SellerInfo sellerInfo) {
        SellerGoodsFragment fragment = new SellerGoodsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_DATA, sellerInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSellerInfo = (SellerInfo) getArguments().getSerializable(Constants.EXTRA_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_seller_goods, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mIsDetach = true;
    }

    public void onEventMainThread(ShoppingCartEvent event) {

        if (mGoodListAdapter != null) {
            mGoodListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void initView() {
        mShoppingCartView = mViewFinder.find(R.id.layout_order_select);
        mShoppingCartView.setSellerInfo(mSellerInfo);
        mViewFinder.onClick(R.id.tv_notice, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NoticeDialog dialog = new NoticeDialog(getActivity());
                dialog.setNotice(mViewFinder.textView(R.id.tv_notice).getText().toString());
                dialog.show();
            }
        });
        loadNotice();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                loadDate();
            }
        }, 200);
    }

    private void loadDate() {
        if (!NetworkUtils.isNetworkAvaiable(this.getActivity())) {
            return;
        }
        CustomDialogFragment.show(this.getFragmentManager(), R.string.loading, SellerGoodsFragment.class.getName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(mSellerInfo.id));
//        params.put("id", String.valueOf(64));
        ApiUtils.post(this.getActivity(), URLConstants.GOODS_LISTS,
                params,
                SellerGoodsResult.class, responseListener(), errorListener());
    }

    private Response.Listener<SellerGoodsResult> responseListener() {
        return new Response.Listener<SellerGoodsResult>() {
            @Override
            public void onResponse(final SellerGoodsResult response) {

                CustomDialogFragment.dismissDialog();
                if(mIsDetach){
                    return;
                }
                if (O2OUtils.checkResponse(getActivity(), response) && !ArraysUtils.isEmpty(response.data)) {
                    mGoodsPackInfoList = response.data;
                    try {
                        initViewData(response.data);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        };

    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(getActivity(), R.string.loading_err_nor);
            }
        };
    }

    private void initViewData(List<GoodsPackInfo> goodsPackInfoList) {
        ListView lvClass = mViewFinder.find(R.id.lv_class);
        if (ArraysUtils.isEmpty(goodsPackInfoList)) {
            mViewFinder.find(R.id.ll_list_container).setVisibility(View.INVISIBLE);
            return;
        }
        mGoodsPackInfoList = goodsPackInfoList;
        goodsPackInfoList.get(0).selected = true;
        lvClass.setEmptyView(mViewFinder.find(android.R.id.empty));
        mGoodPackListAdapter = new GoodPackListAdapter(getActivity(), goodsPackInfoList);
        lvClass.setAdapter(mGoodPackListAdapter);
        lvClass.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mGoodPackListAdapter.isSelected(position)) {
                    return;
                }
                mGoodPackListAdapter.setSelected(position);
                mListView.smoothScrollToPosition(mGoodsPackInfoList.get(position).firstIndex);
//                mListView.setSelection(mGoodsPackInfoList.get(position).firstIndex);

            }
        });

        mListView = mViewFinder.find(R.id.lv_goods);
        mGoodListAdapter = new SellerGoodsAdapter(getActivity(), goodsPackInfoList);
        mListView.setAdapter(mGoodListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), GoodDetailActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, mGoodListAdapter.getItem(position).id);
                startActivityForResult(intent, GOOD_DETAIL_REQUEST_CODE);
            }
        });
        mListView.setOnStickyHeaderChangedListener(new StickyListHeadersListView.OnStickyHeaderChangedListener() {
            @Override
            public void onStickyHeaderChanged(StickyListHeadersListView l, View header, int itemPosition, long headerId) {
                mGoodPackListAdapter.setSelectedById((int)headerId);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case GOOD_DETAIL_REQUEST_CODE:
                mGoodListAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void loadNotice() {
        if (this.getActivity()==null || !NetworkUtils.isNetworkAvaiable(this.getActivity())) {
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("sellerId", String.valueOf(mSellerInfo.id));
        ApiUtils.post(this.getActivity(), URLConstants.SELLER_ARTICLE_LIST,
                params,
                ArticleResult.class, new Response.Listener<ArticleResult>() {
                    @Override
                    public void onResponse(final ArticleResult response) {
                        if (O2OUtils.checkResponse(getActivity(), response) && !ArraysUtils.isEmpty(response.data)) {
                            mViewFinder.setText(R.id.tv_notice, response.data.get(0).content);
                            mViewFinder.find(R.id.tv_notice).setVisibility(View.VISIBLE);
//                            TextView tv = mViewFinder.find(R.id.tv_notice);
//                            tv.setMovementMethod(ScrollingMovementMethod.getInstance());
                        }else{
                            mViewFinder.find(R.id.tv_notice).setVisibility(View.GONE);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

    }


}
