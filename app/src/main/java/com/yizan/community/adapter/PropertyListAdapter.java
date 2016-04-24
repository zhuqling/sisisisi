package com.yizan.community.adapter;

import android.content.Context;

import com.fanwe.seallibrary.model.PropertyFunc;
import com.yizan.community.R;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class PropertyListAdapter extends CommonAdapter<PropertyFunc> {
    public PropertyListAdapter(Context context, List<PropertyFunc> datas) {
        super(context, datas, R.layout.item_property_func);
    }

    public void setList(List<PropertyFunc> list) {
        mDatas.clear();
        addAll(list);
    }

    @Override
    public void convert(final ViewHolder helper, final PropertyFunc item, int position) {
        helper.setImageResource(R.id.iv_image, item.image);
        helper.setText(R.id.tv_name, mContext.getString(item.name));
    }


}
