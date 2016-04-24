package com.yizan.community.adapter;

import android.app.Activity;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.SellerInfo;
import com.zongyou.library.widget.adapter.CommonAdapter;

import java.util.List;

public class SearchHotListAdapter extends CommonAdapter<SellerInfo> {

    public SearchHotListAdapter(Activity context, List<SellerInfo> list) {
        super(context, list, R.layout.item_search_hot);
    }

    @Override
    public void convert(com.zongyou.library.widget.adapter.ViewHolder helper, SellerInfo item, int position) {
        helper.setText(R.id.search_text, item.name);
//        helper.getView(R.id.search_text).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.print("ssss");
//            }
//        });
    }

}
