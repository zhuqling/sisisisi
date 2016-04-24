package com.yizan.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanwe.seallibrary.comm.Constants;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.MessageInfo;
import com.fanwe.seallibrary.model.UserAddressInfo;

import java.util.List;

/**
 * 消息ListAdapter
 * Created by ztl on 2015/9/18.
 */
public class MessageAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<MessageInfo> mDatas;

    public MessageAdapter(Context context, List<MessageInfo> mDatas) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public MessageInfo getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.item_service_msg,null);
            holder.tile = (TextView)convertView.findViewById(R.id.msg_title);
            holder.date = (TextView)convertView.findViewById(R.id.msg_date);
            holder.content = (TextView)convertView.findViewById(R.id.msg_content);
            holder.isRead = (ImageView)convertView.findViewById(R.id.msg_is_read);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        MessageInfo item = mDatas.get(position);
        if (!("").equals(item.title)){
            holder.tile.setText(item.title);
        }else{
            holder.tile.setText("");
        }

        if (!("").equals(item.createTime)){
            holder.date.setText(item.createTime);
        }else{
            holder.date.setText("");
        }

        if (!("").equals(item.content)){
            holder.content.setText(item.content);
        }else{
            holder.content.setText("");
        }

        if (item.isRead == Constants.MSG_UNREAD){
            holder.isRead.setVisibility(View.VISIBLE);
        }else{
            holder.isRead.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    class ViewHolder{
        public TextView tile;
        public TextView date;
        public TextView content;
        public ImageView isRead;
    }
}
