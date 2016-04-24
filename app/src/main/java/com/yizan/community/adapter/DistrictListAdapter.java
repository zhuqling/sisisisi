package com.yizan.community.adapter;

import android.content.Context;
import android.view.View;

import com.fanwe.seallibrary.model.DistrictInfo;
import com.fanwe.seallibrary.model.DoorKeysInfo;
import com.yizan.community.R;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class DistrictListAdapter extends CommonAdapter<DistrictInfo> {
    public DistrictListAdapter(Context context, List<DistrictInfo> datas) {
        super(context, datas, R.layout.item_district_list);
    }

    public void setList(List<DistrictInfo> list) {
        mDatas.clear();
        if(!ArraysUtils.isEmpty(list)) {
            addAll(list);
        }
    }

    @Override
    public void convert(final ViewHolder helper, final DistrictInfo item, int position) {
        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_addr, item.address);

    }


}
