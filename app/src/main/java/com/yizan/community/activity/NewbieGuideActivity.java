package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.yizan.community.R;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class NewbieGuideActivity extends Activity implements OnClickListener,
		OnPageChangeListener {
	private ViewPager mViewPager;
	private ViewPagerAdapter mViewPagerAdapter;
	private List<View> mViews;
	// 引导图片资源
	private static final int[] PICS = { R.drawable.newbieguide1,
			R.drawable.newbieguide2 };
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newbieguide);
		mViews = new ArrayList<View>();
		// 初始化引导图片列表
		for (int i = 0; i < PICS.length; i++) {
			ImageView iv = new ImageView(this);
			iv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			if (i == PICS.length - 1) {
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PreferenceUtils.setValue(NewbieGuideActivity.this, "isfirst", false);
						startActivity(new Intent(NewbieGuideActivity.this,MainActivity.class));
						finish();
					}
				});
			}
			;
			iv.setBackgroundResource(PICS[i]);
			mViews.add(iv);
		}
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		// 初始化Adapter
		mViewPagerAdapter = new ViewPagerAdapter(mViews);
		mViewPager.setAdapter(mViewPagerAdapter);
		// 绑定回调
		mViewPager.setOnPageChangeListener(this);
	}

	private void setCurView(int position)

	{
		if (position < 0 || position >= PICS.length) {
			return;
		}
		mViewPager.setCurrentItem(position);
	}

	// 当滑动状态改变时调用

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	// 当前页面被滑动时调用
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		setCurView(position);
	}

	public class ViewPagerAdapter extends PagerAdapter {
		// 界面列表
		private List<View> mViews;
		public ViewPagerAdapter(List<View> views) {
			this.mViews = views;
		}
		// 销毁arg1位置的界面
		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mViews.get(arg1));
		}
		@Override
		public void finishUpdate(View arg0) {

		}
		// 获得当前界面数
		@Override
		public int getCount() {

			if (mViews != null)

			{

				return mViews.size();

			}

			return 0;

		}

		// 初始化arg1位置的界面

		@Override
		public Object instantiateItem(View arg0, int arg1) {

			((ViewPager) arg0).addView(mViews.get(arg1), 0);

			return mViews.get(arg1);

		}

		// 判断是否由对象生成界面

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {

			return (arg0 == arg1);

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}
	}

	@Override
	public void onPageSelected(int arg0) {

	}


}

