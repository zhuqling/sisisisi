package com.yizan.community.adapter;

import android.content.Context;
import android.view.View;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class CateChildListAdapter extends CommonAdapter<SellerCatesInfo> {
    public CateChildListAdapter(Context context, List<SellerCatesInfo> datas) {
        super(context, datas, R.layout.item_cate_child_list);
    }

    public void setList(List<SellerCatesInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    @Override
    public void convert(final ViewHolder helper, final SellerCatesInfo item, int position) {
        helper.setText(R.id.tv_text, item.name);
        if(position >= getCount()-1){
            helper.setViewVisible(R.id.line_deliver, View.INVISIBLE);
        }else{
            helper.setViewVisible(R.id.line_deliver, View.VISIBLE);
        }
    }


}
