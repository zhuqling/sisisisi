package com.yizan.community.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yizan.community.R;
import com.yizan.community.activity.MainActivity;
import com.yizan.community.comm.ShoppingCartMgr;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;
import com.yizan.community.utils.NumFormat;
import com.yizan.community.utils.O2OUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.ToastUtils;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-24
 * Time: 15:43
 * FIXME
 */
public class ShoppingCartView extends RelativeLayout implements View.OnClickListener{
    private View mRootView;
    private TextView mTvNum, mTvPrice, mTvSelect, mTvStatus;
    private SellerInfo mSellerInfo;
    public ShoppingCartView(Context context) {
        super(context);
        initView(context);
    }

    public ShoppingCartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }


    private void initView(Context context){
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_shopping_cart_status, null);

        this.addView(mRootView);
        ViewGroup.LayoutParams layoutParams = mRootView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mRootView.setLayoutParams(layoutParams);

        mTvNum = (TextView)mRootView.findViewById(R.id.tv_total_count);
        mTvPrice = (TextView)mRootView.findViewById(R.id.tv_total_price);
        mTvSelect = (TextView)mRootView.findViewById(R.id.tv_selected);
        mTvStatus = (TextView)mRootView.findViewById(R.id.tv_status);
        reflashState();
        EventBus.getDefault().register(this);

        mRootView.findViewById(R.id.iv_cart).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_selected).setOnClickListener(this);
    }

    public void reflashState(){
        int totalCount = 0;
        double price = 0.0f;
        double serviceFee = 0.0f;
        if(mSellerInfo != null){
            totalCount = ShoppingCartMgr.getInstance().getTotalCount(mSellerInfo.id);
            price = ShoppingCartMgr.getInstance().getTotalPrice(mSellerInfo.id);
            serviceFee = mSellerInfo.serviceFee;
        }

        mTvNum.setText("" + totalCount);
        if(totalCount <= 0){
            mTvStatus.setText("");
            mTvPrice.setText(R.string.order_car_empty);
        }else{
            mTvStatus.setText(R.string.order_car_total);
            mTvPrice.setText("¥" + NumFormat.formatPrice(price));
        }
        mTvSelect.setText(R.string.order_select_ok);
        if(serviceFee > price){
            mTvSelect.setText(String.format(getResources().getString(R.string.order_car_base_servicefee), NumFormat.formatPrice(mSellerInfo.serviceFee - price)));
            mTvSelect.setEnabled(false);
        }else if(totalCount == 0){
            mTvSelect.setEnabled(false);
        }else{
            mTvSelect.setEnabled(true);
        }

    }

    public void onEventMainThread(ShoppingCartEvent event) {
        reflashState();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {

        try {
            if(O2OUtils.turnLogin(getContext())){
                return;
            }
            int totalCount = 0;
            boolean bigPrice = false;
            if(mSellerInfo != null){
                totalCount = ShoppingCartMgr.getInstance().getTotalCount(mSellerInfo.id);
                double price = ShoppingCartMgr.getInstance().getTotalPrice(mSellerInfo.id);
                bigPrice = (mSellerInfo.serviceFee > price);
            }
            if(totalCount <= 0){
                ToastUtils.show(getContext(), R.string.msg_select_goods);
                return;
            }
            if(bigPrice){
                ToastUtils.show(getContext(), R.string.msg_select_goods_nomore);
                return;
            }

            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("cart", 1);
            getContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setSellerInfo(SellerInfo sellerInfo){
        mSellerInfo = sellerInfo;
        reflashState();
    }
}
