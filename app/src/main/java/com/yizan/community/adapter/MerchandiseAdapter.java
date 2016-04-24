package com.yizan.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.CollectionInfo;
import com.fanwe.seallibrary.model.result.CollectionResult;
import com.yizan.community.utils.ApiUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ztl on 2015/9/21.
 */
public class MerchandiseAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<CollectionInfo> mDatas;

    public MerchandiseAdapter(Context context, List<CollectionInfo> mDatas) {
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
            convertView = mInflater.inflate(R.layout.item_merchandise,null);
            holder.head = (NetworkImageView)convertView.findViewById(R.id.merchandise_icon);
            holder.title = (TextView)convertView.findViewById(R.id.merchandise_title);
            holder.price = (TextView)convertView.findViewById(R.id.merchandise_price);
            holder.delete = (ImageView)convertView.findViewById(R.id.merchandise_delete);
            holder.saleNum = (TextView)convertView.findViewById(R.id.tv_sales_num);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        final int index = position;
        final CollectionInfo item = mDatas.get(position);
        setImageUrl(holder.head, item.logo, R.drawable.ic_default_square);
        holder.title.setText(item.name);
        holder.price.setText("¥" + String.valueOf(item.price));
        if(item.salesCount > 0){
            holder.saleNum.setText(context.getString(R.string.hint_sales_num) + item.salesCount);
        }else{
            holder.saleNum.setText("");
        }
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> data = new HashMap<>();
                data.put("id", String.valueOf(mDatas.get(index).id));
                data.put("type", String.valueOf(1));
                ApiUtils.post(context, URLConstants.COLLECTIONDELETE, data, CollectionResult.class, new Response.Listener<CollectionResult>() {
                    @Override
                    public void onResponse(CollectionResult response) {
                        mDatas.remove(index);
                        MerchandiseAdapter.this.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(context, R.string.msg_error_delete_lose);
                    }
                });
            }
        });
        return convertView;
    }

    class ViewHolder{
        public NetworkImageView head;
        public TextView title;
        public TextView price;
        public ImageView delete;
        public TextView saleNum;
    }

    private void setImageUrl(NetworkImageView imageView, String url, int res) {
        imageView.setDefaultImageResId(res);
        imageView.setErrorImageResId(res);
        imageView.setImageUrl(url, RequestManager.getImageLoader());
    }
}
