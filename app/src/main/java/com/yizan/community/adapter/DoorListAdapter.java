package com.yizan.community.adapter;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.fanwe.seallibrary.model.DoorKeysInfo;
import com.fanwe.seallibrary.model.event.OpenDoorEvent;
import com.yizan.community.R;
import com.yizan.community.dialog.DoorNameDialog;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class DoorListAdapter extends CommonAdapter<DoorKeysInfo> {
    public DoorListAdapter(FragmentActivity context, List<DoorKeysInfo> datas) {
        super(context, datas, R.layout.item_door_list);
    }

    public void setList(List<DoorKeysInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    public DoorKeysInfo getItemByDoorId(String keyName){
        if(getCount() <= 0 || TextUtils.isEmpty(keyName)){
            return null;
        }
        for(DoorKeysInfo item: mDatas){
            if(item.keyname.compareTo(keyName) == 0){
                return item;
            }
        }
        return null;
    }

    public void updateItem(int doorId, String remark){
        if(getCount() <= 0 || doorId < 0){
            return;
        }
        for(DoorKeysInfo item: mDatas){
            if(item.doorid == doorId){
                item.remark = remark;
                break;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void convert(final ViewHolder helper, final DoorKeysInfo item, int position) {
        if(TextUtils.isEmpty(item.remark)){
            helper.setText(R.id.tv_name, item.doorname);
        }else{
            helper.setText(R.id.tv_name, item.remark);
        }

        helper.setText(R.id.tv_date, item.expiretime);
        helper.getView(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDoorEvent event = new OpenDoorEvent(item.userid, item.keyname, item.community, item.keyid,item.doorid);
                EventBus.getDefault().post(event);
            }
        });
        helper.getView(R.id.tv_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoorNameDialog.show((FragmentActivity)mContext, item);
            }
        });

    }


}
