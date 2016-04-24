package com.yizan.community.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.yizan.community.R;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 意见反馈Activity
 */
public class FeedbackActivity extends BaseActivity implements BaseActivity.TitleListener {

	private EditText mFeedbackEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		setTitleListener(this);

	}

	private void initView() {
		setContentView(R.layout.activity_feedback);
		mFeedbackEditText = (EditText) findViewById(R.id.feedback_et);

		mViewFinder.find(R.id.feedback_button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AppUtils.hideSoftInput(FeedbackActivity.this);
				feedback();

			}
		});

	}



	private void feedback() {
		if (TextUtils.isEmpty(mFeedbackEditText.getText().toString().trim())) {
			ToastUtils.show(FeedbackActivity.this, R.string.opinion_edi_text);
			return;
		}
		if (NetworkUtils.isNetworkAvaiable(FeedbackActivity.this)) {

			CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, FeedbackActivity.class.getName());

			ApiUtils.post(getApplicationContext(), URLConstants.FEEDBACK_CREATE, getParams(), BaseResult.class, new Response.Listener<BaseResult>() {
				@Override
				public void onResponse(BaseResult response) {
					CustomDialogFragment.dismissDialog();
					if (O2OUtils.checkResponse(FeedbackActivity.this.getApplicationContext(), response)) {
						ToastUtils.show(FeedbackActivity.this, R.string.opinion_finsh);
						finishActivity();
					}
				}
			}, new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					CustomDialogFragment.dismissDialog();
				}
			});

		} else
			ToastUtils.show(FeedbackActivity.this, R.string.msg_error_network);
	}

	private Map<String, String> getParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("deviceType", "Android");
		map.put("content", mFeedbackEditText.getText().toString().trim());

		return map;
	}

	@Override
	public void setTitle(TextView title, ImageButton left, View right) {
		title.setText(R.string.opinion);
	}
}
