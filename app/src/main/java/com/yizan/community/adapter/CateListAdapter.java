package com.yizan.community.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yizan.community.R;
import com.yizan.community.activity.BusinessClassificationActivity;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class CateListAdapter extends CommonAdapter<SellerCatesInfo> {
    public CateListAdapter(Context context, List<SellerCatesInfo> datas) {
        super(context, datas, R.layout.item_cate_list);
    }

    public void setList(List<SellerCatesInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    @Override
    public void convert(final ViewHolder helper, final SellerCatesInfo item, int position) {
        helper.setImageUrl(R.id.iv_image, item.logo, RequestManager.getImageLoader(), R.drawable.ic_default_square);
        helper.setText(R.id.tv_name, item.name);

        ListView listView = helper.getView(R.id.lv_list);
        if(ArraysUtils.isEmpty(item.childs)){
            listView.setVisibility(View.GONE);
            helper.setViewVisible(R.id.line_deliver, View.INVISIBLE);
        }else{
            listView.setVisibility(View.VISIBLE);
            helper.setViewVisible(R.id.line_deliver, View.VISIBLE);
            final CateChildListAdapter adapter = new CateChildListAdapter(mContext, item.childs);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mContext, BusinessClassificationActivity.class);
                    intent.putExtra(Constants.EXTRA_DATA, String.valueOf(adapter.getItem(position).id));
                    intent.putExtra(Constants.EXTRA_TYPE, adapter.getItem(position).type);
                    intent.putExtra(Constants.EXTRA_TITLE, adapter.getItem(position).name);
                    mContext.startActivity(intent);
                }
            });
        }
    }


}
