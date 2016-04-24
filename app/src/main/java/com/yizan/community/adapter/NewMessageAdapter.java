package com.yizan.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.MessageInfo;

import java.util.List;

/**
 * Created by ztl on 2015/8/11.
 */
public class NewMessageAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private List<MessageInfo> list;


    public NewMessageAdapter(Context context, List<MessageInfo> data){
        this.context = context;
        this.list = data;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if(view == null){
            view = mInflater.inflate(R.layout.item_message,null);
            holder = new Holder(view);
            view.setTag(holder);
        }else{
            holder = (Holder)view.getTag();
        }
        holder.setData(holder, i);
        return view;
    }

    class Holder{
        private TextView title;
        private TextView content;
        private TextView date;
        private ImageView img;
        private ImageView imgBtn;
        private RelativeLayout img_is_read_layout;



        public Holder(View view){
            title = (TextView)view.findViewById(R.id.tv_msg_title);
            content = (TextView)view.findViewById(R.id.message_content);
            date = (TextView)view.findViewById(R.id.message_createtime);
            img = (ImageView)view.findViewById(R.id.img_is_read);
            img_is_read_layout = (RelativeLayout)view.findViewById(R.id.img_is_read_layout);
            imgBtn = (ImageView)view.findViewById(R.id.img_btn_is_read);
        }
        public void setData( Holder holder,final int index){
            MessageInfo ann = list.get(index);
            title.setText(ann.title);
            content.setText(ann.content);
            date.setText(ann.createTime);
            if(ann.isRead == 1){
                imgBtn.setVisibility(View.VISIBLE);
            }else if(ann.isRead == 0){
                imgBtn.setVisibility(View.INVISIBLE);
            }
            if(!list.get(index).isChick){
                img.setImageResource(R.drawable.msg_no_selector);
            }else{
                img.setImageResource(R.drawable.msg__selector);

            }

            img_is_read_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(list.get(index).isChick){
                        img.setImageResource(R.drawable.msg_no_selector);
                        list.get(index).isChick=false;
                    }else{
                        img.setImageResource(R.drawable.msg__selector);
                        list.get(index).isChick=true;
                    }
                    notifyDataSetChanged();
                }
            });

        }
    }
}
