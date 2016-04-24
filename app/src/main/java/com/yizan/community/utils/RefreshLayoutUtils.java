package com.yizan.community.utils;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

/**
 * @author Altas
 * @email Altas.Tutu@gmail.com
 * @time Dec 19, 2014 9:56:37 AM
 */
public class RefreshLayoutUtils {
	/**
	 * 初始化 swiperefreshlayout
	 * @param context
	 * @param swipeRefreshLayout
	 * @param onRefreshListener
	 * @param isShow
	 * @param colorResIds
	 */
	public static final void initSwipeRefreshLayout(Context context, SwipeRefreshLayout swipeRefreshLayout, OnRefreshListener onRefreshListener, boolean isShow,
			int... colorResIds) {
		swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
		
		if (colorResIds.length ==0)
			swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light,
					android.R.color.holo_red_light);
		else
			swipeRefreshLayout.setColorSchemeResources(colorResIds);
		if (isShow)
			showSwipeRefreshLayout(context, swipeRefreshLayout);
	}

	private static void showSwipeRefreshLayout(Context context, SwipeRefreshLayout swipeRefreshLayout) {
//		TypedValue typed_value = new TypedValue();
//		context.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
//		swipeRefreshLayout.setProgressViewOffset(false, 0, context.getResources().getDimensionPixelSize(typed_value.resourceId));
		swipeRefreshLayout.setProgressViewOffset(false, 0, context.getResources().getDimensionPixelSize(com.zongyou.library.R.dimen.title_bar_height));
		swipeRefreshLayout.setRefreshing(true);
	}
}
