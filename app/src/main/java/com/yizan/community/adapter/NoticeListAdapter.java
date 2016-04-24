package com.yizan.community.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.AdvInfo;
import com.yizan.community.utils.ImgUrl;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class NoticeListAdapter extends CommonAdapter<AdvInfo> {
	public NoticeListAdapter(Context context, List<AdvInfo> datas) {
		super(context, datas, R.layout.item_main_notice);

	}

	public void setList(List<AdvInfo> list){
		mDatas.clear();
		addAll(list);
	}


	@Override
	public void convert(ViewHolder helper, final AdvInfo item,int position) {
//		helper.setImageUrl(R.id.iv_image, ImgUrl.heightUrl(R.dimen.homepage_hor_notice_height, item.image), RequestManager.getImageLoader(), R.drawable.ic_default_rectangle);
		View image = helper.getView(R.id.iv_image);
		int heigth = ImgUrl.getScreenWidth()*3/8;
		ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
		layoutParams.height = heigth;
		image.setLayoutParams(layoutParams);
		helper.setImageUrl(R.id.iv_image, ImgUrl.formatUrl(ImgUrl.getScreenWidth(), heigth, item.image), RequestManager.getImageLoader(), R.drawable.ic_default_rectangle);
	}


}
