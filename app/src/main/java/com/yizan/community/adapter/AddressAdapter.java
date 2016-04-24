package com.yizan.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.result.AddressResult;

import java.util.List;

/**
 * Created by ztl on 2015/9/18.
 */
public class AddressAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<UserAddressInfo> mDatas;

    public AddressAdapter(Context context, List<UserAddressInfo> mDatas) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView ==null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_switch_addr,null);
            holder.name = (TextView)convertView.findViewById(R.id.item_switch_addr_name);
            holder.mobile = (TextView)convertView.findViewById(R.id.item_switch_addr_mobile);
            holder.address = (TextView)convertView.findViewById(R.id.item_switch_addr_text);
            holder.top = (ImageView)convertView.findViewById(R.id.item_switch_top);
            holder.bottom = (ImageView)convertView.findViewById(R.id.item_switch_bottom);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        UserAddressInfo item = mDatas.get(position);
        if (!("").equals(item.name)){
            holder.name.setText(item.name);
        }else{
            holder.name.setText("");
        }
        if (!("").equals(item.mobile)){
            holder.mobile.setText(item.mobile);
        }else{
            holder.mobile.setText("");
        }
        if (!("").equals(item.address)){
            holder.address.setText(item.address);
        }else{
            holder.address.setText("");
        }
        if (item.isDefault){
            holder.top.setVisibility(View.VISIBLE);
            holder.bottom.setVisibility(View.VISIBLE);
        }else{
            holder.top.setVisibility(View.GONE);
            holder.bottom.setVisibility(View.GONE);
        }
        return convertView;
    }

    class ViewHolder{
        public TextView name;
        public TextView mobile;
        public TextView address;
        public ImageView top;
        public ImageView bottom;
    }
}
