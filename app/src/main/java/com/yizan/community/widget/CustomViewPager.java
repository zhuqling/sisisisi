package com.yizan.community.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Altas
 * @email Altas.Tutu@gmail.com
 * @time 2015-3-27 下午1:58:12
 */
public class CustomViewPager extends ViewPager {
	private float preX;

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// @Override
	// public boolean onInterceptTouchEvent(MotionEvent event) {
	// boolean res = super.onInterceptTouchEvent(event);
	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
	// preX = event.getY();
	// } else {
	// if (Math.abs(event.getY() - preX) > 20) {
	// return false;
	// } else {
	// preX = event.getY();
	// }
	// }
	// return true;
	// }

	/**
	 * 以下这一段是 viewpager滑动有反弹卡顿 所以加这个就不会这样了
	 */
	private float xDistance, yDistance, xLast, yLast;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > yDistance) {
				return true;
			}else
				return false;
		}

		return super.onInterceptTouchEvent(ev);
	}
}
