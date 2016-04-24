package com.yizan.community.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.yizan.community.R;
import com.yizan.community.adapter.CommentListAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.SellerRateInfo;
import com.fanwe.seallibrary.model.result.OrderListResult;
import com.fanwe.seallibrary.model.result.SellerRateResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.LoadingFooter;
import com.yizan.community.widget.OnLoadNextListener;
import com.yizan.community.widget.PageStaggeredGridView;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 评价ListFragment
 */
public class CommentListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private int mType;
    private SwipeRefreshLayout mSwipeLayout;
    private PageStaggeredGridView mGridView;
    private CommentListAdapter mListAdapter;
    private int mSellerId;
    private int mPage = 1;

    public static CommentListFragment newInstance(int type, int sellerId) {
        CommentListFragment fragment = new CommentListFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRA_DATA, type);
        args.putInt(Constants.EXTRA_ID, sellerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_comment_list, container, false);
    }

    @Override
    protected void initView() {
        mSwipeLayout = mViewFinder.find(R.id.sp_container);
        mGridView = mViewFinder.find(R.id.gv_list);
        mListAdapter = new CommentListAdapter(getActivity(), new ArrayList<SellerRateInfo>());

        mGridView.addHeaderView(new View(getActivity()));
        mGridView.setEmptyView(mViewFinder.find(android.R.id.empty));
        mGridView.setAdapter(mListAdapter);
        if (getArguments() != null) {
            mType = getArguments().getInt(Constants.EXTRA_DATA);
            mSellerId = getArguments().getInt(Constants.EXTRA_ID);
        }
        mGridView.setLoadNextListener(new OnLoadNextListener() {
            @Override
            public void onLoadNext() {
                loadNext();
            }
        });


        mSwipeLayout.setOnRefreshListener(this);

    }

    private boolean isFirstPage(int page) {
        return 1 == page;
    }

    @Override
    public void onRefresh() {
        loadFirst();
    }

    private void loadData(int next) {
        if (!mSwipeLayout.isRefreshing() && isFirstPage(next)) {
            mSwipeLayout.setRefreshing(true);
        }
        // 加载数据
        if (!NetworkUtils.isNetworkAvaiable(getActivity())) {
            ToastUtils.show(getActivity(), R.string.loading_err_net);
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", String.valueOf(mType));
        params.put("sellerId", String.valueOf(mSellerId));
        params.put("page", String.valueOf(mPage));
        ApiUtils.post(getActivity(), URLConstants.SELLER_RATE_LIST,
                params,
                SellerRateResult.class, responseListener(), errorListener());
    }

    private Response.Listener<SellerRateResult> responseListener() {
        final boolean isRefreshFromTop = isFirstPage(mPage);
        return new Response.Listener<SellerRateResult>() {
            @Override
            public void onResponse(final SellerRateResult response) {
                if (O2OUtils.checkResponse(getActivity(), response)) {
                    if (isRefreshFromTop) {
                        mListAdapter.clear();
                    }
                    if (!ArraysUtils.isEmpty(response.data)) {
                        mListAdapter.addAll(response.data);
                    }
                    mPage += 1;

                    if (mListAdapter.getCount() < Constants.PAGE_SIZE || mListAdapter.getCount() % Constants.PAGE_SIZE > 0) {
                        mGridView.setState(LoadingFooter.State.TheEnd, 1000);
                    }
                }
                if (isRefreshFromTop) {
                    setRefreshing(false);
                }

            }
        };
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(getActivity(), R.string.loading_err_nor);
                setRefreshing(false);
                mGridView.setState(LoadingFooter.State.TheEnd, 3000);
            }
        };
    }


    private void loadFirst() {
        mPage = 1;
        loadData(mPage);
    }

    private void loadNext() {
        loadData(mPage);
    }

    private void setRefreshing(boolean refreshing) {
        mSwipeLayout.setRefreshing(refreshing);
    }

    private boolean mHasLoadedOnce = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (this.isVisible()) {
            if (isVisibleToUser && !mHasLoadedOnce) {
                // async http request here
                loadFirst();
                mHasLoadedOnce = true;
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        setUserVisibleHint(true);
        super.onActivityCreated(savedInstanceState);
    }


}
