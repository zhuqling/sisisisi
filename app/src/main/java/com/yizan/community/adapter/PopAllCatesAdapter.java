package com.yizan.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.SellerCatesInfo;

import java.util.List;

/**
 * Created by ztl on 2015/8/17.
 */
public class PopAllCatesAdapter extends BaseAdapter {
    private List<SellerCatesInfo> list;
    private LayoutInflater mInflater;
    public PopAllCatesAdapter(Context context, List<SellerCatesInfo> list){
        this.list=list;
        this.mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return list==null?0:list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_pop,null);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (Holder)convertView.getTag();
        }
        holder.setData(position);
        return convertView;
    }

    class Holder{
        TextView tv_pop;
        public Holder(View view){
            tv_pop = (TextView)view.findViewById(R.id.tv_pop);
        }
        public void setData(int index){
            SellerCatesInfo p = list.get(index);
            tv_pop.setText(p.name);
        }
    }
}
