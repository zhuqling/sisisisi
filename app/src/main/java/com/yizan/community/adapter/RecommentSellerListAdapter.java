package com.yizan.community.adapter;

import android.content.Context;
import android.text.Html;
import android.widget.RatingBar;
import android.widget.TextView;

import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.SellerInfo;
import com.tencent.map.geolocation.TencentLocationUtils;
import com.yizan.community.R;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.utils.NumFormat;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

/**
 * 推荐商家ListAdapter
 */
public class RecommentSellerListAdapter extends CommonAdapter<SellerInfo> {
    private LocAddressInfo mAddressInfo;

    public RecommentSellerListAdapter(Context context, List<SellerInfo> datas) {
        super(context, datas, R.layout.item_main_recomment_seller);
        mAddressInfo = PreferenceUtils.getObject(context, LocAddressInfo.class);
    }

    public void setList(List<SellerInfo> list) {
        mDatas.clear();
        addAll(list);
    }


    @Override
    public void convert(ViewHolder helper, final SellerInfo item, int position) {
//        helper.setImageUrl(R.id.iv_image, item.logo, RequestManager.getImageLoader(), R.drawable.ic_default_square);
        helper.setImageUrl(R.id.iv_image, ImgUrl.squareUrl(R.dimen.seller_list_icon_width, item.logo), RequestManager.getImageLoader(), R.drawable.ic_default_square);
        helper.setText(R.id.tv_name, item.name);

        TextView tv = helper.getView(R.id.tv_price);
        tv.setText(Html.fromHtml(item.freight));
        if(item.orderCount > 0) {
            helper.setText(R.id.tv_sales, mContext.getString(R.string.hint_sales_num) + item.orderCount);
        }else{
            helper.setText(R.id.tv_sales, "");
        }
        RatingBar rb = helper.getView(R.id.rb_star);
        rb.setRating(item.score);
        try {
            if (mAddressInfo.mapPoint.x <= 0.0f || mAddressInfo.mapPoint.y <= 0.0f) {
                helper.setText(R.id.tv_distance, mContext.getResources().getString(R.string.distance_unknown));
            } else {
                double distance = TencentLocationUtils.distanceBetween(item.mapPoint.x,
item.mapPoint.y, mAddressInfo.mapPoint.x, mAddressInfo.mapPoint.y);
                if (distance > 1000) {
                    helper.setText(R.id.tv_distance, NumFormat.formatPrice(distance / 1000) + "km");
                } else {
                    helper.setText(R.id.tv_distance, (int) distance + "m");
                }
            }

        } catch (Exception e) {
            helper.setText(R.id.tv_distance, mContext.getResources().getString(R.string.distance_unknown));
        }

    }


}
