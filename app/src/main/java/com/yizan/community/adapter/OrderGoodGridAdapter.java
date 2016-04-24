package com.yizan.community.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.NetworkImageView;
import com.yizan.community.R;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class OrderGoodGridAdapter extends CommonAdapter<String> {
    private int MAX_COUNT = 4;
    private IPhotoFunc mPhotoFunc;

    public OrderGoodGridAdapter(Context context, List<String> datas) {
        super(context, datas, R.layout.item_square_image);
    }


    public void setPhotoFunc(IPhotoFunc photoFunc) {
        mPhotoFunc = photoFunc;
    }

    @Override
    public int getCount() {
        if (mDatas.size() > MAX_COUNT) {
            return MAX_COUNT;
        }
        return super.getCount();
    }

    public void addItem(String value) {
        if (!TextUtils.isEmpty(value)) {
            if (mDatas.size() > 0
                    && TextUtils.isEmpty(mDatas.get(mDatas.size() - 1))) {
                mDatas.add(mDatas.size() - 1, value);
            }
        } else {
            if (mDatas.size() > 0
                    && !TextUtils.isEmpty(mDatas.get(mDatas.size() - 1))) {
                mDatas.add(value);
            } else {
                mDatas.add(value);
            }
        }
        notifyDataSetChanged();
    }

    public void removeItem(int pos) {
        if (pos >= 0 && mDatas.size() > pos) {
            mDatas.remove(pos);
            notifyDataSetChanged();
        }
    }

    public List<String> getDatas() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < mDatas.size(); i++) {
            if (!TextUtils.isEmpty(mDatas.get(i))) {
                list.add(mDatas.get(i));
            }
        }
        return list;
    }

    @Override
    public void convert(final ViewHolder helper, String item, int number) {
        try {
            final NetworkImageView ivPhoto = (NetworkImageView) helper.getView(R.id.iv_image);
            if (TextUtils.isEmpty(item)) {
                return;
            }
            ivPhoto.setImageUrl(item, RequestManager.getImageLoader());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public interface IPhotoFunc {
        void removePhoto(int pos, String path);
    }

}