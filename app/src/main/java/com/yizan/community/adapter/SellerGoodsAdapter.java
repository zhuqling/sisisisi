package com.yizan.community.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.fanwe.seallibrary.model.GoodInfo;
import com.fanwe.seallibrary.model.GoodsNorms;
import com.fanwe.seallibrary.model.GoodsPackInfo;
import com.yizan.community.R;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.widget.stickylistheaders.StickyListHeadersAdapter;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.LogUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SellerGoodsAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private final int DEFAULT_NORMS_SIZE = 3;
    private Context mContext;
    private List<GoodsPackInfo> mDatas;
    private List<GoodInfo> mGoodInfoList;
    private LayoutInflater mLayoutInflater;
    public SellerGoodsAdapter(FragmentActivity context, List<GoodsPackInfo> datas) {
        mContext = context;
        mDatas = datas;
        mGoodInfoList = new ArrayList<>();
        formatDatas();
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setList(List<GoodsPackInfo> list) {
        mDatas.clear();
        mDatas.addAll(list);
        formatDatas();
        notifyDataSetChanged();
    }

    protected void formatDatas(){
        mGoodInfoList.clear();
        if(ArraysUtils.isEmpty(mDatas)){
            return;
        }

        int firstIndex = 0;
        for(GoodsPackInfo item: mDatas){
            item.firstIndex = firstIndex;
            if(!ArraysUtils.isEmpty(item.goods)) {
                mGoodInfoList.addAll(item.goods);
                firstIndex += item.goods.size();
            }else{
                GoodInfo goodInfo = new GoodInfo();
                goodInfo.id = 0;
                mGoodInfoList.add(goodInfo);
                firstIndex += 1;
            }
        }
    }


    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder hvh;
        if(convertView == null){
            hvh = new HeaderViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.item_seller_group, null);
            hvh.tvHeader = (TextView) convertView.findViewById(R.id.tv_head_name);
            convertView.setTag(hvh);
        }else{
            hvh = (HeaderViewHolder)convertView.getTag();
        }
        GoodsPackInfo info = getPackInfo(position);
        hvh.tvHeader.setText(info.name);
        return convertView;
    }

    protected GoodsPackInfo getPackInfo(int position){
        int count = 0;
        GoodsPackInfo pack = null;
        for(GoodsPackInfo item: mDatas){
            count = ArraysUtils.isEmpty(item.goods)?1:item.goods.size();
            if((item.firstIndex <= position) && ((count + item.firstIndex) > position) ){
                pack = item;
                break;
            }
        }
        if(pack == null){
            pack = mDatas.get(mDatas.size()-1);
        }
        return pack;
    }
    @Override
    public long getHeaderId(int position) {
        return getPackInfo(position).id;
    }

    @Override
    public int getCount() {
        return mGoodInfoList.size();
    }

    @Override
    public GoodInfo getItem(int position) {
        return mGoodInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mGoodInfoList.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder hvh;
        if(convertView == null){
            hvh = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.item_seller_good, null);
            hvh.ivImage = (NetworkImageView) convertView.findViewById(R.id.iv_image);
            hvh.lvList = (ListView)convertView.findViewById(R.id.lv_list);
            hvh.tvExpand = (TextView)convertView.findViewById(R.id.tv_expand);
            hvh.tvName = (TextView)convertView.findViewById(R.id.tv_name);
            hvh.tvSalesNum = (TextView)convertView.findViewById(R.id.tv_sales_num);
            hvh.ivImage.setDefaultImageResId(R.drawable.ic_default_square);
            hvh.tvEmpty = (TextView)convertView.findViewById(R.id.tv_empty);
            hvh.llContainer = convertView.findViewById(R.id.ll_container);
            convertView.setTag(hvh);
        }else{
            hvh = (ViewHolder)convertView.getTag();
        }
        final GoodInfo item = getItem(position);

        if(item.id == 0){
            hvh.tvEmpty.setVisibility(View.VISIBLE);
            hvh.llContainer.setVisibility(View.GONE);
        }else {
            hvh.tvEmpty.setVisibility(View.GONE);
            hvh.llContainer.setVisibility(View.VISIBLE);
            if (!ArraysUtils.isEmpty(item.images)) {
                hvh.ivImage.setImageUrl(ImgUrl.squareUrl(R.dimen.image_height_small, item.images.get(0)), RequestManager.getImageLoader());
            }
            hvh.tvName.setText(item.name);


            if (item.salesCount > 0) {
                hvh.tvSalesNum.setText(mContext.getString(R.string.hint_sales_num) + item.salesCount);
            } else {
                hvh.tvSalesNum.setText("");
            }

            if (ArraysUtils.isEmpty(item.norms)) {
                GoodsNorms norms = new GoodsNorms();
                norms.goodsId = item.id;
                norms.id = 0;
                norms.name = "";
                norms.price = item.price;
                item.norms = new ArrayList<>();
                item.norms.add(norms);
            }
            final SellerGoodNormsListAdapter adapter = new SellerGoodNormsListAdapter((FragmentActivity) mContext, item, item.isClicked ? -1 : DEFAULT_NORMS_SIZE);
            hvh.lvList.setAdapter(adapter);
            if (item.norms.size() <= DEFAULT_NORMS_SIZE) {
                hvh.tvExpand.setVisibility(View.GONE);
            } else {
                hvh.tvExpand.setVisibility(View.VISIBLE);
                setExpandDrawable(hvh.tvExpand, item.isClicked, item.norms.size());
            }

            hvh.tvExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.isClicked = !item.isClicked;
                    adapter.setShowNum(item.isClicked ? -1 : DEFAULT_NORMS_SIZE);
                    setExpandDrawable(hvh.tvExpand, item.isClicked, item.norms.size());
                }
            });
        }
        return convertView;
    }

    private void setExpandDrawable(TextView textView, boolean isExpand, int nums) {

        Drawable drawable = mContext.getResources().getDrawable(isExpand ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, null, drawable, null);
        if (isExpand){
            textView.setText(mContext.getResources().getString(R.string.msg_pack_up_norms));
        }else{
            textView.setText(mContext.getResources().getString(R.string.msg_unfold_text_left) + (nums-DEFAULT_NORMS_SIZE) + mContext.getResources().getString(R.string.msg_unfold_text_right));
        }
    }

    class ViewHolder {
        NetworkImageView ivImage;
        TextView tvName;
        TextView tvSalesNum;
        ListView lvList;
        TextView tvExpand;
        TextView tvEmpty;
        View llContainer;
    }

    class HeaderViewHolder{
        TextView tvHeader;
    }
}
