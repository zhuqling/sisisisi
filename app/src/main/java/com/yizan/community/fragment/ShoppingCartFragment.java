package com.yizan.community.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.yizan.community.R;
import com.yizan.community.activity.OrderConfirmActivity;
import com.yizan.community.activity.SwitchAddressActivity;
import com.yizan.community.adapter.ShoppingCartListAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.yizan.community.comm.ShoppingCartMgr;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.CartSellerInfo;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.event.LoginEvent;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;
import com.fanwe.seallibrary.model.result.CartSellerListResult;
import com.fanwe.seallibrary.model.result.OrderListResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.LoadingFooter;
import com.yizan.community.widget.PageStaggeredGridView;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 购物车Fragment
 */
public class ShoppingCartFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, ShoppingCartListAdapter.ICartListener {
    private final int LOC_REQUEST_CODE = 0x301;


    private SwipeRefreshLayout mSwipeLayout;
    private PageStaggeredGridView mGridView;
    private ShoppingCartListAdapter mListAdapter;
    private UserAddressInfo mAddressInfo;
    private View mAddrView;

    public static ShoppingCartFragment newInstance() {
        ShoppingCartFragment fragment = new ShoppingCartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        EventBus.getDefault().register(this);
        return v;
    }

    public void onEventMainThread(ShoppingCartEvent event) {
        if (mListAdapter != null) {
            if (!event.success) {
                mListAdapter.notifyDataSetChanged();
            } else if (event.nums == 0) {
                mListAdapter.setList(ShoppingCartMgr.getInstance().getCart());
            } else {
                mListAdapter.setList(ShoppingCartMgr.getInstance().getCart());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListAdapter != null) {
            mListAdapter.setList(ShoppingCartMgr.getInstance().getCart());
        }
//        initAddrInfo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initView() {
        initAddrInfo();
        mSwipeLayout = mViewFinder.find(R.id.sp_container);
        mGridView = mViewFinder.find(R.id.gv_list);
        mListAdapter = new ShoppingCartListAdapter(getActivity(), new ArrayList<CartSellerInfo>());
        mListAdapter.setICartListener(this);
        mGridView.setEmptyView(mViewFinder.find(android.R.id.empty));
        mGridView.addHeaderView(mAddrView);
        mGridView.setAdapter(mListAdapter);

        mSwipeLayout.setOnRefreshListener(this);
        if (!ShoppingCartMgr.getInstance().isInited() && O2OUtils.isLogin(getActivity())) {
            loadData();
        }
    }

    private void initAddrInfo() {
        if (mAddressInfo == null) {
            mAddressInfo = getDefaultAddress();
        }
        if (mAddrView == null) {
            mAddrView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_shopping_cart_addr, null);
            mAddrView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShoppingCartFragment.this.getActivity(), SwitchAddressActivity.class);
                    intent.putExtra("isLocate", "false");
                    startActivityForResult(intent, LOC_REQUEST_CODE);
                }
            });
        }

        initAddrView();

    }

    private UserAddressInfo getDefaultAddress() {
        try {
            UserInfo userInfo = PreferenceUtils.getObject(getActivity(), UserInfo.class);
            if (ArraysUtils.isEmpty(userInfo.address)) {
                return null;
            } else {
                for (UserAddressInfo addressInfo : userInfo.address) {
                    if (addressInfo.isDefault) {
                        return addressInfo;
                    }
                }
                return userInfo.address.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initAddrView() {
        if (mAddressInfo == null) {
            mAddrView.findViewById(R.id.ll_container).setVisibility(View.INVISIBLE);
            mAddrView.findViewById(R.id.tv_notice).setVisibility(View.VISIBLE);
            return;
        }
        mAddrView.findViewById(R.id.ll_container).setVisibility(View.VISIBLE);
        mAddrView.findViewById(R.id.tv_notice).setVisibility(View.INVISIBLE);
        ((TextView) mAddrView.findViewById(R.id.tv_name)).setText(mAddressInfo.name);
        ((TextView) mAddrView.findViewById(R.id.tv_tel)).setText(mAddressInfo.mobile);
        ((TextView) mAddrView.findViewById(R.id.tv_addr)).setText(mAddressInfo.address);
    }


    @Override
    public void onRefresh() {
        loadData();
    }

    private void loadData() {

        // 加载数据
        if (!NetworkUtils.isNetworkAvaiable(getActivity())) {
            ToastUtils.show(getActivity(), R.string.loading_err_net);
            mSwipeLayout.setRefreshing(false);
            return;
        }
        mSwipeLayout.setRefreshing(true);
        HashMap<String, String> params = new HashMap<String, String>();
        ApiUtils.post(getActivity(), URLConstants.CART_LISTS,
                params,
                CartSellerListResult.class, responseListener(), errorListener());
    }

    private Response.Listener<CartSellerListResult> responseListener() {
        return new Response.Listener<CartSellerListResult>() {
            @Override
            public void onResponse(final CartSellerListResult response) {
                if (O2OUtils.checkResponse(getActivity(), response)) {

                    mListAdapter.clear();
                    if (!ArraysUtils.isEmpty(response.data)) {
                        mListAdapter.addAll(response.data);
                    }
                    ShoppingCartMgr.getInstance().setCart(response.data);
                    mGridView.setState(LoadingFooter.State.TheEnd, 1000);

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
                mGridView.setState(LoadingFooter.State.Idle, 3000);
            }
        };
    }

    private void setRefreshing(boolean refreshing) {
        mSwipeLayout.setRefreshing(refreshing);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LOC_REQUEST_CODE:
                if (data != null) {
                    UserAddressInfo addressInfo = (UserAddressInfo) data.getSerializableExtra(Constants.EXTRA_DATA);
                    if (addressInfo != null) {
                        mAddressInfo = addressInfo;
                        initAddrView();
                    }
                }
                break;
        }
    }

    /**
     * 点击结算按钮的监听事件
     * @param info
     */
    @Override
    public void onBuyClick(CartSellerInfo info) {
        //判断收货地址是否为空
        if (mAddressInfo == null) {
            ToastUtils.show(getActivity(), R.string.home_notice_sel_location);
            return;
        }
        //创建一个泛型为购物车商品信息的集合
        ArrayList<CartGoodsInfo> list = new ArrayList<CartGoodsInfo>();
        try {
            //遍历info实体类
            for (CartGoodsInfo item : info.goods) {
                //判断每个商品是否选中
                if (item.isChecked) {
                    //选中就添加到list集合
                    list.add(item);
                }
            }
            //判断当前list集合是否为空
            if (ArraysUtils.isEmpty(list)) {

                ToastUtils.show(getActivity(), R.string.err_sel_goods);
                return;
            }
            //跳转到订单确认
            Intent intent = new Intent(getActivity(), OrderConfirmActivity.class);
            intent.putExtra(Constants.EXTRA_DATA, list);
            intent.putExtra(Constants.EXTRA_ADDR, mAddressInfo);
            intent.putExtra(Constants.EXTRA_SELLER, info);
            startActivity(intent);
        } catch (Exception e) {
            ToastUtils.show(getActivity(), R.string.err_sel_goods);
        }
    }

    public void reflashUI(){
        if(O2OUtils.isLogin(getActivity())) {
            mAddressInfo = getDefaultAddress();
        }else{
            mAddressInfo = null;
        }
        initAddrView();
    }

    public void selectAll(){
        if(mListAdapter != null ){
            mListAdapter.selectAll();
        }

    }
}
