package com.yizan.community.adapter;

import android.content.Context;

import com.fanwe.seallibrary.model.BuildingInfo;
import com.fanwe.seallibrary.model.RoomInfo;
import com.yizan.community.R;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;


public class PopRoomListAdapter extends CommonAdapter<RoomInfo> {
    public PopRoomListAdapter(Context context, List<RoomInfo> list){
        super(context, list, R.layout.item_pop);

    }

    @Override
    public void convert(ViewHolder viewHolder, RoomInfo item, int i) {
        viewHolder.setText(R.id.tv_pop, item.roomNum);
    }


}
