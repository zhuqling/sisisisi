package com.yizan.community.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.GoodInfo;
import com.fanwe.seallibrary.model.GoodsNorms;
import com.yizan.community.R;
import com.yizan.community.comm.ShoppingCartMgr;
import com.yizan.community.utils.NumFormat;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class SellerGoodNormsListAdapter extends CommonAdapter<GoodsNorms> {
    private GoodInfo mGoodInfo;
    private int mShowNum = -1;


    public SellerGoodNormsListAdapter(FragmentActivity context, GoodInfo datas, int showNum) {
        super(context, datas.norms, R.layout.item_seller_good_norms);
        mGoodInfo = datas;
        mShowNum = showNum;
    }

    public void setList(List<GoodsNorms> list) {
        mDatas.clear();
        mDatas.addAll(list);
        notifyDataSetChanged();
    }

    public void setShowNum(int showNum) {
        mShowNum = showNum;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mShowNum == -1) {
            return super.getCount();
        }
        if (super.getCount() > mShowNum) {
            return mShowNum;
        }
        return super.getCount();
    }


    @Override
    public void convert(final ViewHolder helper, final GoodsNorms item, int position) {

        if (TextUtils.isEmpty(item.name)) {
            helper.setViewVisible(R.id.tv_name, View.GONE);
        } else {
            helper.setText(R.id.tv_name, item.name);
            helper.setViewVisible(R.id.tv_name, View.VISIBLE);
        }

        helper.setText(R.id.tv_price, String.format(mContext.getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(item.price)));

        final CartGoodsInfo info = ShoppingCartMgr.getInstance().getCartGood(mGoodInfo.id, item.id);
        if (info != null && info.num > 0) {
            helper.setText(R.id.tv_num, String.valueOf(info.num));
            helper.setViewVisible(R.id.tv_num, View.VISIBLE);
            helper.setViewVisible(R.id.iv_sub, View.VISIBLE);
        } else {
            helper.setText(R.id.tv_num, String.valueOf(0));
            helper.setViewVisible(R.id.tv_num, View.INVISIBLE);
            helper.setViewVisible(R.id.iv_sub, View.INVISIBLE);
        }

        helper.getView(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changeShopCard(item.id, 1)) {
                    TextView tv = helper.getView(R.id.tv_num);
                    helper.setText(R.id.tv_num, String.valueOf(Integer.parseInt(tv.getText().toString()) + 1));
                }
            }
        });
        helper.getView(R.id.iv_sub).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changeShopCard(item.id, -1)) {
                    TextView tv = helper.getView(R.id.tv_num);
                    helper.setText(R.id.tv_num, String.valueOf(Integer.parseInt(tv.getText().toString()) - 1));
                }
            }
        });
    }

    private boolean changeShopCard(int normsId, int chgCount) {
        CartGoodsInfo cartGoodsInfo = ShoppingCartMgr.getInstance().getCartGood(mGoodInfo.id, normsId);
        if (cartGoodsInfo == null) {
            return ShoppingCartMgr.getInstance().saveShopping((FragmentActivity) mContext, mGoodInfo.id, normsId, chgCount);
        } else {
            return ShoppingCartMgr.getInstance().saveShopping((FragmentActivity) mContext, mGoodInfo.id, normsId, chgCount + cartGoodsInfo.num);
        }
    }

}
