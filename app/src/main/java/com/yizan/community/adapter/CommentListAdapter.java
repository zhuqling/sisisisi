package com.yizan.community.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RatingBar;

import com.android.volley.toolbox.NetworkImageView;
import com.yizan.community.R;
import com.yizan.community.activity.ViewImageActivity;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.SellerRateInfo;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.widget.CommentStarView;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CommentListAdapter extends CommonAdapter<SellerRateInfo> {
    public CommentListAdapter(Context context, List<SellerRateInfo> datas) {
        super(context, datas, R.layout.item_comment);

    }

    public void setList(List<SellerRateInfo> list) {
        mDatas.clear();
        addAll(list);
    }


    @Override
    public void convert(ViewHolder helper, final SellerRateInfo item, final int position) {
        NetworkImageView iv = helper.getView(R.id.iv_image);
        iv.setImageUrl(ImgUrl.squareUrl(R.dimen.image_height_small, item.avatar), RequestManager.getImageLoader());
        helper.setText(R.id.tv_name, item.userName);
        helper.setText(R.id.tv_time, item.createTime);
        RatingBar starView = helper.getView(R.id.rb_star);
        starView.setRating(item.star);

        helper.setText(R.id.tv_detail, item.content);
        if(ArraysUtils.isEmpty(item.images)){
            helper.getView(R.id.gv_pics).setVisibility(View.GONE);
        }else{
            CommentPhotoGridAdapter photoGridAdapter = new CommentPhotoGridAdapter(mContext, item.images);
            GridView gv = helper.getView(R.id.gv_pics);
            gv.setVisibility(View.VISIBLE);
            gv.setAdapter(photoGridAdapter);
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mContext, ViewImageActivity.class);
                    ArrayList<String> list = new ArrayList<String>();
                    list.addAll(item.images);
                    intent.putExtra(Constants.EXTRA_DATA, list);
                    intent.putExtra(Constants.EXTRA_INDEX, position);
                    mContext.startActivity(intent);
                }
            });
        }

        if(TextUtils.isEmpty(item.reply)){
            helper.setViewVisible(R.id.tv_reply, View.GONE);
        }else{
            helper.setViewVisible(R.id.tv_reply, View.VISIBLE);
            helper.setText(R.id.tv_reply, item.reply);
        }


    }

}
