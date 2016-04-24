package com.yizan.community.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.tencent.map.geolocation.TencentLocationUtils;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.CollectionInfo;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.result.CollectionResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.NumFormat;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;
import com.zongyou.library.volley.RequestManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏商家ListAdapter
 * Created by ztl on 2015/9/21.
 */
public class ShopAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<CollectionInfo> mDatas;
    private LocAddressInfo mAddressInfo;

    public ShopAdapter(Context context, List<CollectionInfo> mDatas) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
        mAddressInfo = PreferenceUtils.getObject(context, LocAddressInfo.class);
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
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_shop, null);
            holder.head = (NetworkImageView) convertView.findViewById(R.id.shop_icon);
            holder.name = (TextView) convertView.findViewById(R.id.shop_name);
            holder.articlDetail = (TextView) convertView.findViewById(R.id.shop_articl_detail);
            holder.distance = (TextView) convertView.findViewById(R.id.shop_distance);
            holder.delete = (ImageView) convertView.findViewById(R.id.shop_img_delete);
            holder.mRatingBar = (RatingBar) convertView.findViewById(R.id.rb_star);
            holder.salesNum = (TextView)convertView.findViewById(R.id.tv_sales);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final int index = position;
        CollectionInfo item = mDatas.get(position);
        setImageUrl(holder.head, item.logo, R.drawable.ic_default_square);
        holder.name.setText(item.name);
        holder.articlDetail.setText(Html.fromHtml(item.freight));
        holder.mRatingBar.setRating(item.score);

        if(item.orderCount > 0){
            holder.salesNum.setText(context.getString(R.string.hint_sales_num) + item.orderCount);
        }else{
            holder.salesNum.setText("");
        }
        try {
            if (mAddressInfo.mapPoint.x <= 0) {
                holder.distance.setText(context.getResources().getString(R.string.distance_unknown));
            } else {
                double distance = TencentLocationUtils.distanceBetween(Double.parseDouble(item.mapPoint.x),
                        Double.parseDouble(item.mapPoint.y), mAddressInfo.mapPoint.x, mAddressInfo.mapPoint.y);
                if (distance > 1000) {
                    holder.distance.setText( NumFormat.formatPrice(distance/1000) + "km");
                } else {
                    holder.distance.setText((int)distance + "m");
                }
            }
        } catch (Exception e) {
            holder.distance.setText(context.getResources().getString(R.string.distance_unknown));
        }
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> data = new HashMap<>();
                data.put("id", String.valueOf(mDatas.get(index).id));
                data.put("type", String.valueOf(2));
                ApiUtils.post(context, URLConstants.COLLECTIONDELETE, data, CollectionResult.class, new Response.Listener<CollectionResult>() {
                    @Override
                    public void onResponse(CollectionResult response) {
                        mDatas.remove(index);
                        ShopAdapter.this.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(context,R.string.msg_error_delete_lose);
                    }
                });
            }
        });
        return convertView;
    }

    class ViewHolder {
        public NetworkImageView head;
        public TextView name;
        public TextView articlDetail;
        public TextView distance;
        public ImageView delete;
        public RatingBar mRatingBar;
        public TextView salesNum;
    }

    private void setImageUrl(NetworkImageView imageView, String url, int res) {
        imageView.setDefaultImageResId(res);
        imageView.setErrorImageResId(res);
        imageView.setImageUrl(url, RequestManager.getImageLoader());
    }
}
