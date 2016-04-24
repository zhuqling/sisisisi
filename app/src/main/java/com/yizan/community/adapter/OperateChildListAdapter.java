package com.yizan.community.adapter;

import android.content.Context;
import android.view.View;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class OperateChildListAdapter extends CommonAdapter<SellerCatesInfo> {
    public OperateChildListAdapter(Context context, List<SellerCatesInfo> datas) {
        super(context, datas, R.layout.item_operate_child);
    }

    public void setList(List<SellerCatesInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    @Override
    public void convert(final ViewHolder helper, final SellerCatesInfo item, int position) {
        helper.setText(R.id.operate_tv, item.name);
        helper.getView(R.id.operate_check).setVisibility(item.checked ? View.VISIBLE : View.GONE);
        if(position >= getCount()-1){
            helper.setViewVisible(R.id.line_deliver, View.INVISIBLE);
        }else{
            helper.setViewVisible(R.id.line_deliver, View.VISIBLE);
        }
    }


    public boolean isAllSel(){
        if(getCount() == 0){
            return false;
        }

        for (SellerCatesInfo item: mDatas){
            if(!item.checked){
                return false;
            }
        }
        return true;
    }
}
