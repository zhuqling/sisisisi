package com.yizan.community.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.model.event.OrderListCommentsEvent;
import com.fanwe.seallibrary.model.result.OrderListPackResult;
import com.yizan.community.R;
import com.yizan.community.activity.AddCommentActivity;
import com.yizan.community.activity.OrderDetailActivity;
import com.yizan.community.adapter.OrderListAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.event.LoginEvent;
import com.fanwe.seallibrary.model.event.OrderEvent;
import com.fanwe.seallibrary.model.result.OrderListResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.LoadingFooter;
import com.yizan.community.widget.OnLoadNextListener;
import com.yizan.community.widget.PageStaggeredGridView;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 订单ListFragment
 */
public class OrderListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private int mStatus;
    private SwipeRefreshLayout mSwipeLayout;
    private PageStaggeredGridView mGridView;
    private OrderListAdapter mListAdapter;
    private int mPage = 1;
    private boolean mNeedReload = false;

    public static OrderListFragment newInstance(int state) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRA_DATA, state);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    protected void initView() {
        mSwipeLayout = mViewFinder.find(R.id.sp_container);
        mGridView = mViewFinder.find(R.id.gv_list);
        mListAdapter = new OrderListAdapter(this, this, new ArrayList<OrderInfo>());

        mGridView.addHeaderView(new View(getActivity()));
        mGridView.setEmptyView(mViewFinder.find(android.R.id.empty));
        mGridView.setAdapter(mListAdapter);
        if (getArguments() != null) {
            mStatus = getArguments().getInt(Constants.EXTRA_DATA);
        }
        mGridView.setLoadNextListener(new OnLoadNextListener() {
            @Override
            public void onLoadNext() {
                loadNext();
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, mListAdapter.getItem(position - mGridView.getHeaderViewsCount()).id);
                startActivity(intent);
            }
        });

        mSwipeLayout.setOnRefreshListener(this);
//        loadFirst();
        EventBus.getDefault().register(this);


    }

    private boolean isFirstPage(int page) {
        return 1 == page;
    }

    @Override
    public void onRefresh() {
        loadFirst();
    }

    private void loadData(int next) {

        // 加载数据
        if (!NetworkUtils.isNetworkAvaiable(getActivity())) {
            ToastUtils.show(getActivity(), R.string.loading_err_net);
            mSwipeLayout.setRefreshing(false);
            return;
        }
        if (isFirstPage(next)) {
            mSwipeLayout.setRefreshing(true);
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("status", String.valueOf(mStatus));
        params.put("page", String.valueOf(mPage));
        ApiUtils.post(getActivity(), URLConstants.ORDERLISTS,
                params,
                OrderListPackResult.class, responseListener(), errorListener());
    }

    private Response.Listener<OrderListPackResult> responseListener() {
        final boolean isRefreshFromTop = isFirstPage(mPage);
        return new Response.Listener<OrderListPackResult>() {
            @Override
            public void onResponse(final OrderListPackResult response) {
                if (O2OUtils.checkResponse(getActivity(), response)) {
                    if (isRefreshFromTop) {
                        mListAdapter.clear();
                    }
                    if (response.data != null) {
                        OrderListCommentsEvent event = new OrderListCommentsEvent(OrderListFragment.this, response.data.commentNum);
                        EventBus.getDefault().post(event);

                    }
                    if (response.data != null && !ArraysUtils.isEmpty(response.data.orderList)) {
                        mListAdapter.addAll(response.data.orderList);
                    }
                    mPage += 1;

                    if (mListAdapter.getCount() < Constants.PAGE_SIZE || mListAdapter.getCount() % Constants.PAGE_SIZE > 0) {
                        mGridView.setState(LoadingFooter.State.TheEnd, 1000);
                    }
                }

                setRefreshing(false);
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

    public void onEventMainThread(LoginEvent event) {
        if (O2OUtils.isLogin(getActivity())) {
            if (mListAdapter != null) {
                mListAdapter.clear();
            }
            mHasLoadedOnce = false;
        }
    }

    public void onEventMainThread(OrderEvent event) {
        if (!O2OUtils.isLogin(getActivity())) {
            return;
        }
        if (event.viewObj == null || !event.viewObj.equals(this)) {
            if (mHasLoadedOnce) {
//                mNeedReload = true;
                reloadData();
            }
        }
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

            if (isVisibleToUser) {
                if (!mHasLoadedOnce) {
                    mHasLoadedOnce = true;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            loadFirst();
                        }
                    }, 200);
                } else if (mNeedReload) {
                    reloadData();
                }
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mStatus == 0) {
            setUserVisibleHint(true);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNeedReload) {
            reloadData();
        }
    }

    public void reloadData() {
        if (mHasLoadedOnce) {
            loadFirst();
            mNeedReload = false;
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode != Activity.RESULT_OK){
//            return;
//        }
//        switch (requestCode){
//            case AddCommentActivity.REQUEST_CODE:
//                reloadData();
//                break;
//        }
//    }
}
