package com.yizan.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.SearchHistoryInfo;
import com.fanwe.seallibrary.model.UserAddressInfo;

import java.util.List;

/**
 * Created by ztl on 2015/9/18.
 */
public class SearchHistoryAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<SearchHistoryInfo> mDatas;

    public SearchHistoryAdapter(Context context, List<SearchHistoryInfo> mDatas) {
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
            convertView = mInflater.inflate(R.layout.item_search_history,null);
            holder.name = (TextView)convertView.findViewById(R.id.search_history_text);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        SearchHistoryInfo item = mDatas.get(position);
        if (!("").equals(item.name)){
            holder.name.setText(item.name);
        }else{
            holder.name.setText("");
        }
        return convertView;
    }

    class ViewHolder{
        public TextView name;
    }
}
