package com.yizan.community.adapter;

import android.content.Context;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.GoodInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

/**
 * 推荐商品ListAdapter
 */
public class RecommentGoodsListAdapter extends CommonAdapter<GoodInfo> {
    public RecommentGoodsListAdapter(Context context, List<GoodInfo> datas) {
        super(context, datas, R.layout.item_main_recomment_goods);

    }

    public void setList(List<GoodInfo> list) {
        mDatas.clear();
        addAll(list);
    }


    @Override
    public void convert(ViewHolder helper, final GoodInfo item, int position) {

        if(!ArraysUtils.isEmpty(item.images)){
            helper.setImageUrl(R.id.iv_image, item.images.get(0), RequestManager.getImageLoader(), R.drawable.ic_default_square);
        }

        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_price, String.valueOf(item.price));

    }


}
