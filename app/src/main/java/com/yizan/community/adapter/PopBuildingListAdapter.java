package com.yizan.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fanwe.seallibrary.model.BuildingInfo;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.yizan.community.R;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;


public class PopBuildingListAdapter extends CommonAdapter<BuildingInfo> {
    public PopBuildingListAdapter(Context context, List<BuildingInfo> list){
        super(context, list, R.layout.item_pop);

    }

    @Override
    public void convert(ViewHolder viewHolder, BuildingInfo buildingInfo, int i) {
        viewHolder.setText(R.id.tv_pop, buildingInfo.name);
    }


}
