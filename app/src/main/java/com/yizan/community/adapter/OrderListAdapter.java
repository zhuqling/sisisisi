package com.yizan.community.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.event.GotoHomeEvent;
import com.fanwe.seallibrary.model.event.OrderEvent;
import com.fanwe.seallibrary.model.result.OrderResult;
import com.yizan.community.R;
import com.yizan.community.activity.AddCommentActivity;
import com.yizan.community.activity.PayWayActivity;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.utils.OrderService;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.LogUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class OrderListAdapter extends CommonAdapter<OrderInfo> {
    private Object mViewTag;
    private Fragment mFragment;

    public OrderListAdapter(Object viewTag, Fragment context, List<OrderInfo> datas) {
        super(context.getActivity(), datas, R.layout.item_order);
        mFragment = context;

    }

    public void setList(List<OrderInfo> list) {
        mDatas.clear();
        addAll(list);
    }


    @Override
    public void convert(ViewHolder helper, final OrderInfo item, final int position) {
        helper.setText(R.id.tv_seller_name, item.shopName);
        helper.setText(R.id.tv_state, item.orderStatusStr);
        helper.setText(R.id.tv_pay_need, formatPriceText(mContext.getResources().getString(R.string.price_yuan, item.totalFee)));
        helper.setText(R.id.tv_goods_num, formatPriceText(mContext.getResources().getString(R.string.count_arg, item.count)));
        NetworkImageView imageView;
        if (!ArraysUtils.isEmpty(item.goodsImages)) {
            imageView = helper.getView(R.id.order_img0);
            if (item.goodsImages.size() > 0) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setDefaultImageResId(R.drawable.ic_default_square);
                imageView.setErrorImageResId(R.drawable.ic_default_square);
                imageView.setImageUrl(ImgUrl.squareUrl(R.dimen.image_height,item.goodsImages.get(0)), RequestManager.getImageLoader());
            } else
                imageView.setVisibility(View.GONE);

            imageView = helper.getView(R.id.order_img1);
            if (item.goodsImages.size() > 1) {

                imageView.setVisibility(View.VISIBLE);
                imageView.setDefaultImageResId(R.drawable.ic_default_square);
                imageView.setErrorImageResId(R.drawable.ic_default_square);
                imageView.setImageUrl(ImgUrl.squareUrl(R.dimen.image_height,item.goodsImages.get(1)), RequestManager.getImageLoader());
            } else
                imageView.setVisibility(View.GONE);

            imageView = helper.getView(R.id.order_img2);
            if (item.goodsImages.size() > 2) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setDefaultImageResId(R.drawable.ic_default_square);
                imageView.setErrorImageResId(R.drawable.ic_default_square);
                imageView.setImageUrl(ImgUrl.squareUrl(R.dimen.image_height, item.goodsImages.get(2)), RequestManager.getImageLoader());
            } else
                imageView.setVisibility(View.GONE);

            imageView = helper.getView(R.id.order_img3);
            if (item.goodsImages.size() > 3) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setDefaultImageResId(R.drawable.ic_default_square);
                imageView.setErrorImageResId(R.drawable.ic_default_square);
                imageView.setImageUrl(ImgUrl.squareUrl(R.dimen.image_height,item.goodsImages.get(3)), RequestManager.getImageLoader());
            } else
                imageView.setVisibility(View.GONE);
        } else {
            helper.getView(R.id.order_img0).setVisibility(View.GONE);
            helper.getView(R.id.order_img1).setVisibility(View.GONE);
            helper.getView(R.id.order_img2).setVisibility(View.GONE);
            helper.getView(R.id.order_img3).setVisibility(View.GONE);
        }
        initButtomStatus(helper, item);
        helper.getView(R.id.btn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isCanCancel) {
                    OrderService.cancelOrder(mFragment.getActivity(), item, responseListener(), errorListener());
                } else if (item.isCanRate) {
                    Intent intent = new Intent(mContext, AddCommentActivity.class);
                    intent.putExtra(Constants.EXTRA_DATA, item);
                    mFragment.startActivity(intent);
                } else if (item.isCanDelete) {
                    orderDel(item);
                }
            }
        });
        helper.getView(R.id.btn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isCanPay) {
                    gotoPayActivity(item);
                } else if (item.isCanConfirm) {
                    OrderService.confirmOrder(mFragment.getActivity(), item, responseListener(), errorListener());
                } else {
                    EventBus.getDefault().post(new GotoHomeEvent(0));
                }
            }
        });

    }

    private void initButtomStatus(ViewHolder helper, OrderInfo orderInfo) {
        // left button
        Button btnLeft, btnRight;
        btnLeft = helper.getView(R.id.btn_left);
        btnRight = helper.getView(R.id.btn_right);
        btnLeft.setVisibility(View.VISIBLE);
        if (orderInfo.isCanCancel) {
            btnLeft.setText(R.string.msg_cancel_order);
        } else if (orderInfo.isCanRate) {
            btnLeft.setText(R.string.msg_go_evaluate);
        } else if (orderInfo.isCanDelete) {
            btnLeft.setText(R.string.msg_order_delete);
        } else {
            btnLeft.setVisibility(View.GONE);
        }
        // right button
        btnRight.setVisibility(View.VISIBLE);
        if (orderInfo.isCanPay) {
            btnRight.setText(R.string.msg_go_pay);
        } else if (orderInfo.isCanConfirm) {
            btnRight.setText(R.string.msg_affirm_goods);
        } else {
            btnRight.setText(R.string.msg_go_stroll);
        }
    }

    private SpannableString formatPriceText(String str) {
        SpannableString sp = new SpannableString(str);
        sp.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.theme_price)), 1, str.length() - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return sp;
    }

    public void orderDel(final OrderInfo orderInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);  //先得到构造器
        builder.setTitle(mContext.getResources().getString(R.string.hint)); //设置标题
        builder.setMessage(mContext.getResources().getString(R.string.msg_delete_order_hint)); //设置内容
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                mDatas.remove(position);
                notifyDataSetChanged();
                ToastUtils.show(mContext, R.string.msg_success_del);
                OrderService.orderDel(mViewTag, mContext, orderInfo.id);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void gotoPayActivity(OrderInfo orderInfo) {
        Intent intent = new Intent(mContext, PayWayActivity.class);
        intent.putExtra(Constants.EXTRA_DATA, orderInfo);
        mFragment.startActivityForResult(intent, PayWayActivity.REQUEST_CODE);
    }

    private Response.Listener<OrderResult> responseListener() {
        return new Response.Listener<OrderResult>() {
            @Override
            public void onResponse(final OrderResult response) {
                CustomDialogFragment.dismissDialog();
                if (O2OUtils.checkResponse(mContext, response)) {
                    try {
                        OrderEvent orderEvent = new OrderEvent(null);
                        EventBus.getDefault().post(orderEvent);
                    } catch (Exception e) {
                        LogUtils.e("order", e);
                        ToastUtils.show(mContext, R.string.msg_error_network);
                    }
                }
            }
        };
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(mContext, R.string.loading_err_nor);
                CustomDialogFragment.dismissDialog();
            }
        };
    }
}
