package com.yizan.community.adapter;

import android.content.Context;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.GoodsNorms;
import com.fanwe.seallibrary.model.GoodsPackInfo;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class GoodNormsAdapter extends CommonAdapter<GoodsNorms> {
    public GoodNormsAdapter(Context context, List<GoodsNorms> datas) {
        super(context, datas, R.layout.item_good_norms);

    }

    public void setList(List<GoodsNorms> list) {
        mDatas.clear();
        addAll(list);
    }
    public List<GoodsNorms> getList(){

        return mDatas;
    }

    public void setSelected(int index){
        if(index >= getCount()){
            return;
        }
        for(GoodsNorms item: mDatas){
            item.selected = false;
        }
        mDatas.get(index).selected = true;
        notifyDataSetChanged();
    }
    public void setSelectedById(int id){

        for(GoodsNorms item: mDatas){
            if(item.id == id){
                item.selected = true;
            }else{
                item.selected = false;
            }

        }
        notifyDataSetChanged();
    }

    public int getSelectedId(){
        for(GoodsNorms item: mDatas){
            if(item.selected){
                return item.id;
            }
        }
        return -1;
    }

    public boolean isSelected(int index){
        if(index >= getCount()){
            return true;
        }

        return getItem(index).selected;
    }


    @Override
    public void convert(ViewHolder helper, final GoodsNorms item, int position) {
        TextView tv = helper.getView(R.id.tv_name);
        tv.setText(item.name);
        tv.setSelected(item.selected);
        if(item.selected){
            tv.setTextColor(mContext.getResources().getColor(R.color.white));
        }else{
            tv.setTextColor(mContext.getResources().getColor(R.color.theme_black_text));
        }
    }


}
