package com.yizan.community.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fanwe.seallibrary.utils.ArraysUtils;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.CartSellerInfo;
import com.fanwe.seallibrary.model.GoodsNorms;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.StaffInfo;
import com.yizan.community.utils.NumFormat;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class OrderGoodListAdapter extends CommonAdapter<OrderGoodListAdapter.OrderGood> {
    private OrderInfo mOrderInfo;
    private LayoutInflater mLayoutInflater;
    public OrderGoodListAdapter(Context context, OrderInfo orderInfo) {
        super(context, new ArrayList<OrderGood>(), R.layout.item_order_good);
        mOrderInfo = orderInfo;
        mLayoutInflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        reflashData();
    }

    public void setList(List<OrderGood> list) {
        mDatas.clear();
        addAll(list);
    }

    protected void addItem(OrderGood item){
        mDatas.add(item);
    }
    protected OrderGood getItemByGoodId(int goodId){
        if(getCount() == 0){
            return null;
        }
        for(OrderGood item : mDatas){
            if(item.goodId == goodId){
                return item;
            }
        }
        return null;
    }
    protected void reflashData(){
        if(null == mOrderInfo || ArraysUtils.isEmpty(mOrderInfo.cartSellers)){
            return;
        }
        for(CartSellerInfo item: mOrderInfo.cartSellers){
            OrderGood good = getItemByGoodId(item.goodsId);
            if(good == null){
                good = new OrderGood();
                good.goodId = item.goodsId;
                good.name = item.goodsName;
                good.norms = new ArrayList<>();
                good.price = item.price;
                good.num = item.num;
                addItem(good);
            }
            if(!TextUtils.isEmpty(item.goodsNorms)) {
                GoodNorms goodNorms = new GoodNorms();
                goodNorms.goodId = item.goodsId;
                goodNorms.name = item.goodsNorms;
                goodNorms.price = item.price;
                goodNorms.num = item.num;
                good.norms.add(goodNorms);
            }

        }

    }



    @Override
    public void convert(ViewHolder helper, final OrderGood item, int position) {

        helper.setText(R.id.tv_name, item.name);


        LinearLayout layout = helper.getView(R.id.ll_norms_container);
        layout.removeAllViews();
        if(ArraysUtils.isEmpty(item.norms)) {
            helper.setText(R.id.tv_price, String.format(mContext.getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(item.price)));
            helper.setText(R.id.tv_num, " x " + item.num);
        }else{
            double price = 0.0f;
            for(GoodNorms norms : item.norms){
                price += (norms.price * norms.num);
                View normsView = mLayoutInflater.inflate(R.layout.item_order_good_norms, null);
                TextView tv = (TextView)normsView.findViewById(R.id.tv_name);
                tv.setText(norms.name);
                tv = (TextView)normsView.findViewById(R.id.tv_price);
                tv.setText(String.format(mContext.getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(norms.price)));
                tv = (TextView)normsView.findViewById(R.id.tv_num);
                tv.setText(" x " + norms.num);
                layout.addView(normsView);
            }
            helper.setText(R.id.tv_price, String.format(mContext.getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(price)));
            helper.setText(R.id.tv_num, "");
        }
    }

    public static class OrderGood{
        public int goodId;
        public String name;
        public int num;
        public double price;
        public List<GoodNorms> norms;
    }

    public static class GoodNorms{
        public int normsId;
        public int goodId;
        public String name;
        public double price;
        public int num;
    }
}