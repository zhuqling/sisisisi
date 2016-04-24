package com.yizan.community.adapter;

import android.content.Context;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.UserInfo;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class StarGoodsListAdapter extends CommonAdapter<UserInfo> {
	public StarGoodsListAdapter(Context context, List<UserInfo> datas) {
		super(context, datas, R.layout.item_main_classify);

	}

	public void setList(List<UserInfo> list){
		mDatas.clear();
		addAll(list);
	}


	@Override
	public void convert(ViewHolder helper, final UserInfo item,int position) {



	}


}
