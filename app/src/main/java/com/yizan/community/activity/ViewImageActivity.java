package com.yizan.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.yizan.community.R;
import com.fanwe.seallibrary.comm.Constants;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 查看大图Activity
 * 
 * @author Altas
 * @email Altas.Tutu@gmail.com
 * @time 2015-3-29 下午1:56:03
 */
public class ViewImageActivity extends BaseActivity {
	private ViewPager mViewPager;
	private List<String> mUrls;
	private ArrayList<ImageView> mImages;
	private TextView mIndicatorTextView;
	private int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewpager);
		setCustomTitle(R.layout.titlebar_viewimage);

		mIndicatorTextView = (TextView) findViewById(R.id.view_image_indicator);
		Intent intent = getIntent();
		mUrls = intent.getStringArrayListExtra(Constants.EXTRA_DATA);
		index = intent.getIntExtra(Constants.EXTRA_INDEX, 0);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mImages = new ArrayList<ImageView>(mUrls.size());
		for (int i = 0, size = mUrls.size(); i < size; i++) {

			final ImageView image = new ImageView(this);
			if (isLocalFile(mUrls.get(i))) {
				try {
					Drawable drawable = Drawable.createFromPath(mUrls.get(i));
					image.setImageBitmap(compressImageFromFile(mUrls.get(i)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				ImageRequest imageRequest = new ImageRequest(mUrls.get(i),
						new Response.Listener<Bitmap>() {
							@Override
							public void onResponse(Bitmap response) {
								image.setImageBitmap(response);
							}
						}, getScreenWidth(), 0, Config.RGB_565,
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								image.setImageResource(R.drawable.ic_default_square);
							}
						});
				RequestManager.getRequestQueue().add(imageRequest);
			}

			mImages.add(image);

		}
		mViewPager.setAdapter(new PagerAdapter() {
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				View view = mImages.get(position);
				if (view.getParent() != null)
					container.removeView(view);
				container.addView(view);
				return view;
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			/**
			 * 销毁对应位置上的object
			 */
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView((View) object);
				object = null;
			}

			@Override
			public int getCount() {
				return mUrls.size();
			}
		});
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				mIndicatorTextView.setText((arg0 + 1) + "/" + mUrls.size());
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mViewPager.setCurrentItem(index);
		mIndicatorTextView.setText((index + 1) + "/" + mUrls.size());
	}

	@Override
	public void onBackPressed() {
		finishActivity();
	}

	public static boolean isLocalFile(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		if (path.startsWith("http://")) {
			return false;
		}
		return true;
	}

	private Bitmap compressImageFromFile(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// 只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 1024f;//
		float ww = 768f;//
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置采样率

		newOpts.inPreferredConfig = Config.ARGB_8888;// 该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		// return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
		// 其实是无效的,大家尽管尝试
		return bitmap;
	}

	private int getScreenWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	public static void show(Activity activity, ArrayList<String> imgs) {
		if(ArraysUtils.isEmpty(imgs)){
			return;
		}
		Intent intent = new Intent(activity, ViewImageActivity.class);
		intent.putStringArrayListExtra(Constants.EXTRA_DATA, imgs);
		activity.startActivity(intent);
	}
}