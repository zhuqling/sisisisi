package com.yizan.community.adapter;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.ServiceInfo;
import com.yizan.community.utils.NumFormat;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class ServicesListAdapter extends CommonAdapter<ServiceInfo> {
    private FragmentActivity mFragmentActivity;
    public ServicesListAdapter(FragmentActivity context, List<ServiceInfo> datas) {
        super(context, datas, R.layout.item_service);
        mFragmentActivity = context;
    }

    public void setList(List<ServiceInfo> list) {
        mDatas.clear();
        addAll(list);
    }




    @Override
    public void convert(final ViewHolder helper, final ServiceInfo item, int position) {
        if(!TextUtils.isEmpty(item.logo)) {
            helper.setImageUrl(R.id.iv_image, item.logo, RequestManager.getImageLoader(), R.drawable.ic_default_square);
        }
        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_price, String.format(mContext.getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(item.price)));
        helper.setText(R.id.tv_time, item.duration +mContext.getString(R.string.minute));

    }


}
