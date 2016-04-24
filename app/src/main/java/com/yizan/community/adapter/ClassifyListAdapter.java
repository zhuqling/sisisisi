package com.yizan.community.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.AdvInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.yizan.community.utils.ImgUrl;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class ClassifyListAdapter extends CommonAdapter<AdvInfo> {
    public ClassifyListAdapter(Context context, List<AdvInfo> datas) {
        super(context, datas, R.layout.item_main_classify);

    }

    public void setList(List<AdvInfo> list) {
        mDatas.clear();
        addAll(list);
    }


    @Override
    public void convert(ViewHolder helper, final AdvInfo item, int position) {
        if (!TextUtils.isEmpty(item.arg) && item.arg.equals("0") && TextUtils.isEmpty(item.image)) {
            helper.setImageResource(R.id.iv_image, R.drawable.ic_more);
            helper.setText(R.id.tv_name, mContext.getResources().getString(R.string.all));
        } else {
            helper.setImageUrl(R.id.iv_image, item.image, RequestManager.getImageLoader(), R.drawable.ic_default_circle);
//            helper.setImageUrl(R.id.iv_image, ImgUrl.square4Url(item.image), RequestManager.getImageLoader(), R.drawable.ic_default_circle);
            helper.setText(R.id.tv_name, item.name);
        }

//		helper.setText(R.id.tv_name, item.name);
    }


}
