package com.yizan.community.adapter;

import android.content.Context;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.AdvInfo;
import com.fanwe.seallibrary.model.GoodsPackInfo;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class GoodPackListAdapter extends CommonAdapter<GoodsPackInfo> {
    public GoodPackListAdapter(Context context, List<GoodsPackInfo> datas) {
        super(context, datas, R.layout.item_good_pack);

    }

    public void setList(List<GoodsPackInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    public void setSelected(int index){
        if(index >= getCount()){
            return;
        }
        for(GoodsPackInfo item: mDatas){
            item.selected = false;
        }
        mDatas.get(index).selected = true;
        notifyDataSetChanged();
    }

    public void setSelectedById(int id){
        for(GoodsPackInfo item: mDatas){

            if(item.id == id){
                item.selected = true;
            }else{
                item.selected = false;
            }
        }
        notifyDataSetChanged();
    }

    public boolean isSelected(int index){
        if(index >= getCount()){
            return true;
        }

        return getItem(index).selected;
    }


    @Override
    public void convert(ViewHolder helper, final GoodsPackInfo item, int position) {
        TextView tv = helper.getView(R.id.tv_name);
        tv.setText(item.name);
        if(item.selected){
            tv.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            tv.setTextColor(mContext.getResources().getColor(R.color.theme_main_text));
        }else{
            tv.setBackgroundColor(mContext.getResources().getColor(R.color.theme_background));
            tv.setTextColor(mContext.getResources().getColor(R.color.theme_black_text));
        }
    }


}
