package com.yizan.community.adapter;

import android.content.Context;

import com.android.volley.toolbox.NetworkImageView;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.OrderInfo;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class ImageListAdapter extends CommonAdapter<String> {
    public ImageListAdapter(Context context, List<String> datas) {
        super(context, datas, R.layout.item_square_image);
    }

    public void setList(List<String> list) {
        mDatas.clear();
        addAll(list);
    }

    @Override
    public void convert(ViewHolder helper, final String item, int position) {
        NetworkImageView iv = helper.getView(R.id.iv_image);
        iv.setImageUrl(item, RequestManager.getImageLoader());
    }


}
