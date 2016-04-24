package com.yizan.community.adapter;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.fanwe.seallibrary.comm.Constants;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.CartSellerInfo;
import com.yizan.community.activity.SellerGoodsActivity;
import com.yizan.community.activity.SellerServicesActivity;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class ShoppingCartListAdapter extends CommonAdapter<CartSellerInfo> {
    private FragmentActivity mFragmentActivity;
    public ICartListener mICartListener;
    private Intent intent;
    public ShoppingCartListAdapter(FragmentActivity context, List<CartSellerInfo> datas) {
        super(context, datas, R.layout.item_shopping_cart);
        mFragmentActivity = context;
    }

    public void setICartListener(ICartListener listener){
        mICartListener = listener;
    }
    public void setList(List<CartSellerInfo> list) {
        mDatas.clear();
        if(ArraysUtils.isEmpty(list)){
            notifyDataSetChanged();
        }else {
            addAll(list);
        }
    }

    public void selectAll(){
        if(getCount() <= 0){
            return;
        }
        for (CartSellerInfo item: mDatas){
            item.isChecked = true;
            for (CartGoodsInfo goodsInfo : item.goods){
                goodsInfo.isChecked = true;
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public void convert(final ViewHolder helper, final CartSellerInfo item, int position) {

        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_price, "¥" + String.valueOf(item.price));
        final ListView lv = helper.getView(R.id.lv_list);
        CartGoodsListAdapter adapter = new CartGoodsListAdapter(mFragmentActivity, item, this);
        lv.setAdapter(adapter);

        final View btnBuy = helper.getView(R.id.btn_buy);
        final CheckBox cb = helper.getView(R.id.cb_sel);
        cb.setOnCheckedChangeListener(null);
        cb.setChecked(item.isChecked);
        cb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                item.isChecked = !item.isChecked;
                cb.setChecked(item.isChecked);
                CartGoodsListAdapter adapter = (CartGoodsListAdapter) lv.getAdapter();
                if (adapter != null) {
                    adapter.setAllChecked(item.isChecked);
                }
                btnBuy.setEnabled(item.isChecked);
            }
        });

        TextView tv = helper.getView(R.id.tv_name);
        tv.setFocusable(true);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.type == 1)
                    intent = new Intent(mFragmentActivity, SellerGoodsActivity.class);
                else
                    intent = new Intent(mFragmentActivity, SellerServicesActivity.class);
                intent.putExtra(Constants.EXTRA_DATA, item.id);
                mFragmentActivity.startActivity(intent);
            }
        });

        btnBuy.setEnabled(adapter.haveOneChecked());
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mICartListener != null) {
                    mICartListener.onBuyClick(item);
                }
            }
        });
    }


    public interface ICartListener {
        public void onBuyClick(CartSellerInfo info);
    }


}
