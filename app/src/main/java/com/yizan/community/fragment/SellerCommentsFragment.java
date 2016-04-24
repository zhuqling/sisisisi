package com.yizan.community.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.SellerRateStatistics;
import com.fanwe.seallibrary.model.result.SellerRateStatisticsResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.widget.CommentStarView;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;

/**
 * 商家Fragment
 */
public class SellerCommentsFragment extends BaseFragment {
    private SellerInfo mSellerInfo;
    private LayoutInflater mLayoutInflater;
    private FragmentTabHost mTabHost;
    private String []mTabNames;

    public static SellerCommentsFragment newInstance(SellerInfo sellerInfo) {
        SellerCommentsFragment fragment = new SellerCommentsFragment();

        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_DATA, sellerInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTabNames =new String[]{getActivity().getResources().getString(R.string.all), getActivity().getResources().getString(R.string.msg_good_evaluate),getActivity().getResources().getString(R.string.msg_middle_evaluate), getString(R.string.msg_bad_evaluate)};
        if (getArguments() != null) {
            mSellerInfo = (SellerInfo) getArguments().getSerializable(Constants.EXTRA_DATA);
        }
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_seller_comments, container, false);
    }

    @Override
    protected void initView() {
        initTabSpec();
    }


    private void loadDate() {
        if (!NetworkUtils.isNetworkAvaiable(this.getActivity())) {
            return;
        }
        CustomDialogFragment.show(this.getFragmentManager(), R.string.loading, SellerCommentsFragment.class.getName());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("sellerId", String.valueOf(mSellerInfo.id));
        ApiUtils.post(this.getActivity(), URLConstants.SELLER_RATE_STATISTICS,
                params,
                SellerRateStatisticsResult.class, responseListener(), errorListener());
    }

    private Response.Listener<SellerRateStatisticsResult> responseListener() {
        return new Response.Listener<SellerRateStatisticsResult>() {
            @Override
            public void onResponse(final SellerRateStatisticsResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(getActivity(), response)) {
                    initViewData(response.data);
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

    private void initViewData(SellerRateStatistics statistics) {
        if(statistics == null){
            return;
        }
        mViewFinder.setText(R.id.tv_score, String.valueOf(statistics.star));
        setTabName(0, statistics.totalCount);
        setTabName(1, statistics.goodCount);
        setTabName(2, statistics.neutralCount);
        setTabName(3, statistics.badCount);
//        CommentStarView starView = mViewFinder.find(R.id.layout_star_view);
//        starView.setStar(statistics.star);
        RatingBar ratingBar = mViewFinder.find(R.id.rb_star);
        ratingBar.setRating((float)statistics.star);
    }

    private void setTabName(int index, int count){
        TextView tv = (TextView) mTabHost.getTabWidget().getChildTabViewAt(index).findViewById(R.id.tv_name);
        tv.setText(mTabNames[index] + "(" + count + ")");
    }

    private boolean mHasLoadedOnce = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (this.isVisible()) {
            if (isVisibleToUser && !mHasLoadedOnce) {
                loadDate();
                mHasLoadedOnce = true;
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    protected void addTabFragment(int index, Class<?> clss) {
        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(
                clss.getName() + index).setIndicator(
                getTabItemView(index));
        // 将Tab按钮添加进Tab选项卡中
        Bundle b = new Bundle();
        b.putInt(Constants.EXTRA_DATA, index);
        b.putInt(Constants.EXTRA_ID, mSellerInfo.id);
//        b.putInt(Constants.EXTRA_ID, 92);
        mTabHost.addTab(tabSpec, clss, b);
    }
    private void initTabSpec() {
        // 实例化布局对象
        mLayoutInflater = LayoutInflater.from(this.getActivity());
        // 实例化TabHost对象，得到TabHost
        mTabHost = mViewFinder.find(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.fl_container);

        addTabFragment(0, CommentListFragment.class);
        addTabFragment(1, CommentListFragment.class);
        addTabFragment(2, CommentListFragment.class);
        addTabFragment(3, CommentListFragment.class);


        // 显示
//        mTabHost.setCurrentTab(0);

        // 栈上面叠加了很多Fragment的时候，如果想再次点击TabHost的首页，能返回到最初首页的页面的话，那就要把首页的Fragment上面的Fragment的弹出
        for (int i = 0, size = 4; i < size; i++) {
            final int j = i;
            mTabHost.getTabWidget().getChildAt(i)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (j) {
                                case 0:

                                    break;
                                case 1:

                                    break;
                                case 2:

                                    break;
                                case 3:

                                    break;
                            }
                            getChildFragmentManager().popBackStack(null,
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            mTabHost.setCurrentTab(j);
                        }
                    });
        }
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    @SuppressLint("InflateParams")
    private View getTabItemView(int index) {
        View view = mLayoutInflater.inflate(R.layout.comment_tab_item, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_name);
        textView.setText(mTabNames[index]);

        return view;
    }

}
