package com.yizan.community.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.R;
import com.yizan.community.activity.SellerGoodsActivity;
import com.yizan.community.activity.SellerServicesActivity;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.result.ArticleResult;
import com.fanwe.seallibrary.model.result.SellerObjectResult;
import com.yizan.community.dialog.NoticeDialog;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.utils.NumFormat;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.app.BaseFragment;
import com.zongyou.library.app.IntentUtils;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.HashMap;
import java.util.Map;

public class SellerDetailFragment extends BaseFragment implements View.OnClickListener{
    private static final String SHOW_PARAM = "show";
    private SellerInfo mSellerInfo;
    private boolean mShowBotton;


    public static SellerDetailFragment newInstance(SellerInfo sellerInfo, boolean showBotton) {
        SellerDetailFragment fragment = new SellerDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_DATA, sellerInfo);
        args.putBoolean(SHOW_PARAM, showBotton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSellerInfo = (SellerInfo) getArguments().getSerializable(Constants.EXTRA_DATA);
            mShowBotton = getArguments().getBoolean(SHOW_PARAM, false);
        }
    }


    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_seller_detail, container, false);
    }


    @Override
    protected void initView() {
        initViewData(mSellerInfo);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                loadNotice();
            }
        }, 100);

    }


    private void loadDate() {
        if (!NetworkUtils.isNetworkAvaiable(this.getActivity())) {
            return;
        }
        CustomDialogFragment.show(getFragmentManager(), R.string.msg_loading, SellerDetailFragment.class.getName());
        Map<String, Integer> data = new HashMap<>();
        data.put("id", mSellerInfo.id);
        ApiUtils.post(getActivity(), URLConstants.SELLERDETAIL, data, SellerObjectResult.class, new Response.Listener<SellerObjectResult>() {
            @Override
            public void onResponse(SellerObjectResult response) {
                if (response.data != null) {
                    mSellerInfo = response.data;
                    initViewData(response.data);
                }
                CustomDialogFragment.dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                ToastUtils.show(getActivity(), R.string.msg_error);
            }
        });
    }

    private void initViewData(SellerInfo sellerInfo) {
        if (sellerInfo == null) {
            return;
        }
        mSellerInfo = sellerInfo;
        NetworkImageView iv = mViewFinder.find(R.id.iv_image);
        iv.setImageUrl(ImgUrl.heightUrl(R.dimen.seller_detail_header_height, mSellerInfo.logo), RequestManager.getImageLoader());

        if(!TextUtils.isEmpty(mSellerInfo.image)){
            iv = mViewFinder.find(R.id.iv_bg);
            iv.setImageUrl(ImgUrl.squareUrl(R.dimen.image_height, mSellerInfo.image), RequestManager.getImageLoader());
        }


        if (TextUtils.isEmpty(mSellerInfo.tel)) {
//            mViewFinder.find(R.id.ll_tel).setVisibility(View.GONE);
//            mViewFinder.find(R.id.line_tel).setVisibility(View.GONE);
            mViewFinder.find(R.id.iv_call).setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(mSellerInfo.name)) {
            mViewFinder.setText(R.id.tv_name, "");
        } else {
            mViewFinder.setText(R.id.tv_name, mSellerInfo.name);
        }

        if (TextUtils.isEmpty(mSellerInfo.businessHours)) {

            mViewFinder.setText(R.id.tv_time, R.string.msg_not_available);
        } else {
            mViewFinder.setText(R.id.tv_time, mSellerInfo.businessHours);
        }

        if (TextUtils.isEmpty(mSellerInfo.address)) {
            mViewFinder.setText(R.id.tv_addr, R.string.msg_not_available);
        } else {
            mViewFinder.setText(R.id.tv_addr, mSellerInfo.address);
        }

        if (TextUtils.isEmpty(mSellerInfo.detail)) {
            mViewFinder.setText(R.id.tv_detail, R.string.msg_not_available);
        } else {
            mViewFinder.setText(R.id.tv_detail, mSellerInfo.detail);
        }


        if (!mShowBotton) {
            mViewFinder.find(R.id.ll_bottom_container).setVisibility(View.GONE);
        } else {
            if (sellerInfo.countService != 0) {
                mViewFinder.find(R.id.tv_sel_service).setVisibility(View.VISIBLE);
            } else {
                mViewFinder.find(R.id.tv_sel_service).setVisibility(View.GONE);
            }

            if (sellerInfo.countGoods != 0 || (sellerInfo.countGoods == 0 && sellerInfo.countService == 0)) {
                mViewFinder.find(R.id.tv_sel_goods).setVisibility(View.VISIBLE);
            } else {

                mViewFinder.find(R.id.tv_sel_goods).setVisibility(View.GONE);
            }

            if (mSellerInfo.countGoods == 0 || mSellerInfo.countService == 0) {
                mViewFinder.find(R.id.v_deliver).setVisibility(View.GONE);
            }
        }

        mViewFinder.setText(R.id.tv_deliver_price, String.format(getString(R.string.RMB_sign), NumFormat.formatPrice(mSellerInfo.deliveryFee)));
        mViewFinder.setText(R.id.tv_service_price, String.format(getString(R.string.RMB_sign), NumFormat.formatPrice(mSellerInfo.serviceFee)));

        mViewFinder.onClick(R.id.iv_call, this);
        mViewFinder.onClick(R.id.tv_sel_service, this);
        mViewFinder.onClick(R.id.tv_sel_goods, this);
        mViewFinder.onClick(R.id.ll_notice, this);
    }

    private boolean mHasLoadedOnce = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (this.isVisible()) {
            if (isVisibleToUser && !mHasLoadedOnce) {
                // async http request here
//                loadFirst();
//                loadDate();
//                initViewData(mSellerInfo);

                mHasLoadedOnce = true;
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_sel_service:
                Intent intent1 = new Intent(getActivity(), SellerServicesActivity.class);
                intent1.putExtra(Constants.EXTRA_DATA, mSellerInfo.id);
                startActivity(intent1);
                break;
            case R.id.tv_sel_goods:
                Intent intent = new Intent(getActivity(), SellerGoodsActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, mSellerInfo.id);
                startActivity(intent);
                break;
            case R.id.iv_call:
                View convertView = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_layout, null);
                Button cancel = (Button)convertView.findViewById(R.id.cancel);
                Button call = (Button) convertView.findViewById(R.id.sure);
                View parent = LayoutInflater.from(getActivity()).inflate(R.layout.activity_seller_detail, null);
                TextView text = (TextView)convertView.findViewById(R.id.call_seller_text);

                convertView.setBackgroundResource(R.drawable.style_edt_boder);
                text.setText(getActivity().getString(R.string.msg_is_call_seller));
                WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                int width = (wm.getDefaultDisplay().getWidth() / 3) * 2;
                final PopupWindow popupWindow = new PopupWindow(convertView, width, (int) ((width / 5) * 3), true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                backgroundAlpha(0.5f);
                popupWindow.setOnDismissListener(new poponDismissListener());
                popupWindow.showAtLocation(parent, Gravity.CENTER_HORIZONTAL | Gravity.DISPLAY_CLIP_VERTICAL, 0, 0);

                call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            IntentUtils.dial(getActivity(), mSellerInfo.tel);
                            popupWindow.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });

                break;
            case R.id.ll_notice:
                NoticeDialog dialog = new NoticeDialog(getActivity());
                dialog.setNotice(mViewFinder.textView(R.id.tv_notice).getText().toString());
                dialog.show();
                break;
        }
    }

//    private void loadNotice() {
//        if (!NetworkUtils.isNetworkAvaiable(this.getActivity())) {
//            return;
//        }
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("sellerId", String.valueOf(mSellerInfo.id));
//        ApiUtils.post(this.getActivity(), URLConstants.SELLER_ARTICLE_LIST,
//                params,
//                ArticleResult.class, new Response.Listener<ArticleResult>() {
//                    @Override
//                    public void onResponse(final ArticleResult response) {
//                        if (O2OUtils.checkResponse(getActivity(), response) && !ArraysUtils.isEmpty(response.data)) {
//                            if(!TextUtils.isEmpty(response.data.get(0).content)) {
//                                mViewFinder.setText(R.id.tv_notice, response.data.get(0).content);
//                            }
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                    }
//                });
//    }

    private void loadNotice() {
        if (!NetworkUtils.isNetworkAvaiable(this.getActivity())) {
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("sellerId", String.valueOf(mSellerInfo.id));
        ApiUtils.post(this.getActivity(), URLConstants.SELLER_ARTICLE_LIST,
                params,
                ArticleResult.class, new Response.Listener<ArticleResult>() {
                    @Override
                    public void onResponse(final ArticleResult response) {
                        if (O2OUtils.checkResponse(getActivity(), response) && !ArraysUtils.isEmpty(response.data)) {
                            if(!TextUtils.isEmpty(response.data.get(0).content)) {
                                mViewFinder.setText(R.id.tv_notice, response.data.get(0).content);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

    }
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }
    class poponDismissListener implements PopupWindow.OnDismissListener{

        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            //Log.v("List_noteTypeActivity:", "我是关闭事件");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            backgroundAlpha(1f);
        }

    }
}
