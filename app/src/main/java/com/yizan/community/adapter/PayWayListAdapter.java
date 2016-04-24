package com.yizan.community.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.OrderInfo;
import com.fanwe.seallibrary.model.PaymentInfo;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class PayWayListAdapter extends CommonAdapter<PaymentInfo> {
    public PayWayListAdapter(Context context, List<PaymentInfo> datas) {
        super(context, datas, R.layout.item_pay_way);

        if(!ArraysUtils.isEmpty(mDatas)){
            for(PaymentInfo item : mDatas){
                if(item.isDefault == 1){
                    item.bSel = true;
                    break;
                }
            }
        }
    }

    public void setList(List<PaymentInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    public void setSelect(String payment){
        if(TextUtils.isEmpty(payment)){
            return;
        }
        int index = -1;
        for (int i=0; i< mDatas.size(); i++){
            if(mDatas.get(i).code.compareTo(payment) == 0){
                index = i;
                break;
            }
        }
        selectItem(index);
    }
    public void selectItem(int index){
        if(index >= getCount()){
            return;
        }
        for (PaymentInfo item: mDatas){
            item.bSel = false;
        }
        mDatas.get(index).bSel = true;
        notifyDataSetChanged();
    }

    public PaymentInfo getSelItem() {
        for (PaymentInfo item: mDatas){
            if(item.bSel){
                return item;
            }
        }
        return null;
    }

    @Override
    public void convert(ViewHolder helper, final PaymentInfo item, final int position) {
        helper.setImageUrl(R.id.iv_image, item.icon, RequestManager.getImageLoader(), R.drawable.ic_default_square);
        helper.setText(R.id.tv_name, item.name);
        ImageView iv = helper.getView(R.id.iv_sel);
        if(item.bSel){
            iv.setImageResource(R.drawable.ic_checked_on);
        }else{
            iv.setImageResource(R.drawable.ic_checked_off);
        }

        if(position == getCount() - 1){
            helper.getView(R.id.line_sep).setVisibility(View.INVISIBLE);
        }else{
            helper.getView(R.id.line_sep).setVisibility(View.VISIBLE);
        }

    }


}
