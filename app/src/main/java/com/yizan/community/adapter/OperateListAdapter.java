package com.yizan.community.adapter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class OperateListAdapter extends CommonAdapter<SellerCatesInfo> {
    public OperateListAdapter(Context context, List<SellerCatesInfo> datas) {
        super(context, datas, R.layout.item_operate);
    }

    public void setList(List<SellerCatesInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    @Override
    public void convert(final ViewHolder helper, final SellerCatesInfo item, int position) {
        helper.setText(R.id.operate_tv, item.name);
        helper.getView(R.id.operate_check).setVisibility(item.checked ? View.VISIBLE : View.GONE);

        ListView listView = helper.getView(R.id.lv_list);
        if (ArraysUtils.isEmpty(item.childs)) {
            listView.setVisibility(View.GONE);
            helper.setViewVisible(R.id.line_deliver, View.INVISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            helper.setViewVisible(R.id.line_deliver, View.VISIBLE);
            final OperateChildListAdapter adapter = new OperateChildListAdapter(mContext, item.childs);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (BuildConfig.MULTI_CATES) {
                        adapter.getItem(position).checked = !adapter.getItem(position).checked;
                        adapter.notifyDataSetChanged();
                        if (item.checked != adapter.isAllSel()) {
                            item.checked = adapter.isAllSel();
                            helper.getView(R.id.operate_check).setVisibility(item.checked ? View.VISIBLE : View.GONE);
                        }
                    } else {
                        selSingleCateId(adapter.getItem(position).id);
                    }
                }
            });
        }
    }

    public List<SellerCatesInfo> getSelCateList() {
        if (getCount() == 0) {
            return null;
        }
        List<SellerCatesInfo> list = new ArrayList<>();
        for (SellerCatesInfo item : mDatas) {

            if (item.checked) {
                list.add(item);
            }
            if (!ArraysUtils.isEmpty(item.childs)) {
                for (SellerCatesInfo info : item.childs) {
                    if (info.checked) {
                        list.add(info);
                    }
                }
            }

        }

        return list;
    }

    protected void selSingleCateId(int cateId) {
        if (getCount() == 0) {
            return;
        }
        for (SellerCatesInfo item : mDatas) {

            if (item.id == cateId) {
                item.checked = true;
                if (ArraysUtils.isEmpty(item.childs)) {
                    item.checked = true;
                } else {
                    return;
                }
            } else {
                item.checked = false;
                if (!ArraysUtils.isEmpty(item.childs)) {
                    for (SellerCatesInfo info : item.childs) {
                        if (info.id == cateId) {
                            info.checked = true;
                        } else {
                            info.checked = false;
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    protected void selSingleCateByPosition(int pos) {
        if (getCount() == 0) {
            return;
        }
        SellerCatesInfo catesInfo = getItem(pos);
        if (catesInfo == null || !ArraysUtils.isEmpty(catesInfo.childs)) {
            return;
        }
        selSingleCateId(catesInfo.id);
    }

    protected void selOneCateByPosition(int pos) {
        SellerCatesInfo item = mDatas.get(pos);
        item.checked = !item.checked;

        if (!ArraysUtils.isEmpty(item.childs)) {
            for (SellerCatesInfo info : item.childs) {
                info.checked = item.checked;
            }
        }
        notifyDataSetChanged();
    }

    public void selCateByPosition(int pos) {
        if (BuildConfig.MULTI_CATES) {
            selOneCateByPosition(pos);
        } else {
            selSingleCateByPosition(pos);
        }
    }
}
