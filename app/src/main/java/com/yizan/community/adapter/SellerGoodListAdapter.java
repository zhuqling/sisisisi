package com.yizan.community.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.fanwe.seallibrary.model.GoodsNorms;
import com.yizan.community.R;
import com.yizan.community.comm.ShoppingCartMgr;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.GoodInfo;
import com.yizan.community.utils.ImgUrl;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SellerGoodListAdapter extends CommonAdapter<GoodInfo> {
    private final int DEFAULT_NORMS_SIZE = 3;
    public SellerGoodListAdapter(FragmentActivity context, List<GoodInfo> datas) {
        super(context, datas, R.layout.item_seller_good);
    }

    public void setList(List<GoodInfo> list) {
        mDatas.clear();
        mDatas.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public void convert(final ViewHolder helper, final GoodInfo item, int position) {
        if (!ArraysUtils.isEmpty(item.images)) {
//            helper.setImageUrl(R.id.iv_image, item.images.get(0), RequestManager.getImageLoader(), R.drawable.ic_default_square);
            helper.setImageUrl(R.id.iv_image, ImgUrl.squareUrl(R.dimen.image_height_small, item.images.get(0)), RequestManager.getImageLoader(), R.drawable.ic_default_square);
        }

        helper.setText(R.id.tv_name, item.name);
        if (ArraysUtils.isEmpty(item.norms)) {
            helper.setText(R.id.tv_head_name, item.name);
        } else {
            helper.setText(R.id.tv_head_name, item.name + "(" + item.norms.size() + ")");
        }

        if(item.salesCount > 0){
            helper.setText(R.id.tv_sales_num, mContext.getString(R.string.hint_sales_num) + item.salesCount);
        }else{
            helper.setText(R.id.tv_sales_num, "");
        }

        ListView listView = helper.getView(R.id.lv_list);
        if(ArraysUtils.isEmpty(item.norms)){
            GoodsNorms norms = new GoodsNorms();
            norms.goodsId = item.id;
            norms.id = 0;
            norms.name = "";
            norms.price = item.price;
            item.norms = new ArrayList<>();
            item.norms.add(norms);
        }
        final SellerGoodNormsListAdapter adapter = new SellerGoodNormsListAdapter((FragmentActivity)mContext, item, item.isClicked ? -1 : DEFAULT_NORMS_SIZE);
        listView.setAdapter(adapter);
        final TextView textView = helper.getView(R.id.tv_expand);
        if (item.norms.size() <= DEFAULT_NORMS_SIZE) {
            helper.getView(R.id.tv_expand).setVisibility(View.GONE);
        } else {
            helper.getView(R.id.tv_expand).setVisibility(View.VISIBLE);
            setExpandDrawable(textView, item.isClicked, item.norms.size());

        }

        helper.getView(R.id.tv_expand).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isClicked = !item.isClicked;
                adapter.setShowNum(item.isClicked?-1:DEFAULT_NORMS_SIZE);
                setExpandDrawable(textView, item.isClicked, item.norms.size());
            }
        });

    }

    private void setExpandDrawable(TextView textView, boolean isExpand, int nums) {

        Drawable drawable = mContext.getResources().getDrawable(isExpand ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, null, drawable, null);
        if (isExpand){
            textView.setText(mContext.getResources().getString(R.string.msg_pack_up_norms));
        }else{
            textView.setText(mContext.getResources().getString(R.string.msg_unfold_text_left) + (nums-DEFAULT_NORMS_SIZE) + mContext.getResources().getString(R.string.msg_unfold_text_right));
        }
    }

}
