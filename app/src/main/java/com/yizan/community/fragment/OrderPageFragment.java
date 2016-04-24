package com.yizan.community.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fanwe.seallibrary.model.event.OrderListCommentsEvent;
import com.yizan.community.R;
import com.yizan.community.widget.PagerSlidingTabStrip;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.BaseFragment;

public class OrderPageFragment extends BaseFragment {

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private String[] title;

    public static OrderPageFragment newInstance() {
        OrderPageFragment fragment = new OrderPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        title = new String[]{getActivity().getResources().getString(R.string.msg_all_order), getActivity().getResources().getString(R.string.msg_wait_evaluate)};
        return inflater.inflate(R.layout.fragment_order_page, container, false);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void initView() {
        mViewPager = mViewFinder.find(R.id.viewpager);
        mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.margin_small));
        //向ViewPager绑定PagerSlidingTabStrip
        mPagerSlidingTabStrip = mViewFinder.find(R.id.tabs);
        mPagerSlidingTabStrip.setViewPager(mViewPager);

        mPagerSlidingTabStrip.setUnderlineColorResource(R.color.theme_divide_line);
        mPagerSlidingTabStrip.setUnderlineHeight(1);

        EventBus.getDefault().register(this);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        OrderListFragment mFragment1, mFragment2, mFragment3;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFragment1 = OrderListFragment.newInstance(0);
                    return mFragment1;
                case 1:
                    mFragment2 = OrderListFragment.newInstance(1);
                    return mFragment2;
                case 2:
                    mFragment3 = OrderListFragment.newInstance(2);
                    return mFragment3;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {

            return title.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }

    }

    public void onEventMainThread(OrderListCommentsEvent event) {

        title[1] = getActivity().getResources().getString(R.string.msg_wait_evaluate)+"("+ event.comments + ")";
        mPagerSlidingTabStrip.notifyDataSetChanged();
    }


}
