package com.yizan.community.widget.doublemenu;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yizan.community.R;
import com.yizan.community.YizanApp;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.zongyou.library.util.ArraysUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vonchenchen on 2015/10/30 0030.
 */
public class DoubleListViewHolder extends BaseHolder<List<SellerCatesInfo>> {

    private List<SellerCatesInfo> mData;

    private ListView mLeftListView;
    private ListView mRightListView;

    private List<SellerCatesInfo> mLeftList;
    private List<SellerCatesInfo> mRightList;
    private TextListAdapter mLeftAdapter;
    private TextRightListAdapter mRightAdapter;

    private IListSel mIListSel;


    @Override
    public View initView() {
        View view = View.inflate(YizanApp.getInstance(), R.layout.pop_doublelistview_layout, null);

        mLeftListView = (ListView) view.findViewById(R.id.ll_left);
        mRightListView = (ListView) view.findViewById(R.id.ll_right);


        mLeftListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //如果点击条目，更换被点击条目的颜色

                SellerCatesInfo info = (SellerCatesInfo)mLeftAdapter.getItem(position);
                if(info.id == 0){
                    if (mIListSel != null) {
                        mIListSel.onItemSel(info);
                        mLeftAdapter.selOnlyOneyCate(info.id);
                    }
                }
                if(mLeftAdapter.getSelId() != info.id) {
                    mLeftAdapter.selCateId(((SellerCatesInfo) mLeftAdapter.getItem(position)).id);
                    initRightList(mData.get(position));
                    mRightAdapter = new TextRightListAdapter(mRightList);
                    mRightListView.setAdapter(mRightAdapter);
                }


            }
        });

        mRightListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIListSel != null) {
                    SellerCatesInfo item = (SellerCatesInfo) mRightAdapter.getItem(position);
                    mIListSel.onItemSel(item);
                    mRightAdapter.selOnlyOneyCate(item.id);
                    mLeftAdapter.selOnlyOneyCate(item.id);
                }
            }
        });

        return view;
    }

    private void initRightList(SellerCatesInfo catesInfo) {
        mRightList = new ArrayList<>();
        if(catesInfo == null ){
            return;
        }
        SellerCatesInfo info = catesInfo.clone();
        info.showName = "全部";
        mRightList.add(info);
        if (!ArraysUtils.isEmpty(catesInfo.childs)) {
            mRightList.addAll(catesInfo.childs);
        }
    }

    public void setIListSel(IListSel listener) {
        this.mIListSel = listener;
    }

    @Override
    public void refreshView(List<SellerCatesInfo> info) {

        this.mData = info;

        mLeftList = info;
        mLeftAdapter = new TextListAdapter(mLeftList);

        initRightList(mLeftAdapter.getSelItem());
        mRightAdapter = new TextRightListAdapter(mRightList);

        mLeftListView.setAdapter(mLeftAdapter);
        mRightListView.setAdapter(mRightAdapter);
    }



    class TextListAdapter extends MyAdapter {

        public TextListAdapter(List list) {
            super(list);
        }

        public int getSelId() {
            if (ArraysUtils.isEmpty(mData)) {
                return -1;
            }
            for (int i = 0; i < mData.size(); i++) {
                if (mData.get(i).checked) {
                    return mData.get(i).id;
                }
            }
            return -1;
        }

        public SellerCatesInfo getSelItem(){
            if (ArraysUtils.isEmpty(mData)) {
                return null;
            }
            for (int i = 0; i < mData.size(); i++) {
                if (mData.get(i).checked) {
                    return mData.get(i);
                }
            }
            return null;
        }

        public void selCateId(int id) {
            if (ArraysUtils.isEmpty(mData)) {
                return;
            }

            for (int i = 0; i < mData.size(); i++) {

                if (mData.get(i).id == id) {
                    mData.get(i).checked = true;
                } else {
                    mData.get(i).checked = false;
                }
            }
            this.notifyDataSetChanged();
        }

        public void selOnlyOneyCate(int id){
            if (ArraysUtils.isEmpty(mData)) {
                return;
            }

            for (int i = 0; i < mData.size(); i++) {

                if(mData.get(i).id == id){
                    mData.get(i).selected = true;
                }else{
                    mData.get(i).selected = false;
                }
                if(ArraysUtils.isEmpty(mData.get(i).childs)){
                    continue;
                }


                for (SellerCatesInfo item: mData.get(i).childs){
                    if(item.id == id){
                        item.selected = true;
                    }else{
                        item.selected = false;
                    }
                }

            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            TextViewHolder holder = null;
            if (convertView == null) {
                holder = new TextViewHolder(false);
            } else {
                holder = (TextViewHolder) convertView.getTag();
            }

            convertView = holder.getView();
            if (((SellerCatesInfo) getItem(position)).checked) {
                convertView.setBackgroundColor(YizanApp.getInstance().getResources().getColor(R.color.normal_selected_color));
            } else {
                convertView.setBackgroundColor(YizanApp.getInstance().getResources().getColor(R.color.normal_unselected_color));
            }
            holder.refreshView((SellerCatesInfo) getItem(position));

            //防止多次测量
//            if (position == 0 && mFirstMesure) {
//                mFirstMesure = false;
//                convertView.setBackgroundColor(YizanApp.getInstance().getResources().getColor(R.color.normal_selected_color));
//                mViewClickRecorder = convertView;
//            }

            return convertView;
        }
    }

    class TextRightListAdapter extends MyAdapter {

        public TextRightListAdapter(List list) {
            super(list);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            TextViewHolder holder = null;
            if (convertView == null) {
                holder = new TextViewHolder(true);
            } else {
                holder = (TextViewHolder) convertView.getTag();
            }

            convertView = holder.getView();
            holder.refreshView((SellerCatesInfo) getItem(position));

//            if(position == 0){
//                convertView.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.normal_selected_color));
//                mViewClickRecorder = convertView;
//            }

            return convertView;
        }

        public void selOnlyOneyCate(int id){
            if (getCount() == 0) {
                return;
            }

            for (int i = 0; i < getCount(); i++) {
                SellerCatesInfo item = (SellerCatesInfo)getItem(i);
                if(item.id == id){
                    item.selected = true;
                }else{
                    item.selected = false;
                }

            }
        }
    }

    /**
     * ListView 中的 Holder
     */
    private class TextViewHolder extends BaseHolder<SellerCatesInfo> {

        private TextView mTextView;
        private ImageView mIvSel;
        private boolean mShowSelected = false;

        public TextViewHolder(boolean showSelected){
            super();
            this.mShowSelected = showSelected;
        }

        @Override
        public View initView() {
            View view = View.inflate(YizanApp.getInstance(), R.layout.pop_simpletext_item, null);
            mTextView = (TextView) view.findViewById(R.id.tv_text);
            mIvSel = (ImageView)view.findViewById(R.id.iv_sel);
            return view;
        }

        @Override
        public void refreshView(SellerCatesInfo info) {
            if(TextUtils.isEmpty(info.showName)) {
                mTextView.setText(info.name);
            }else{
                mTextView.setText(info.showName);
            }
            if(mShowSelected) {
                if (info.selected) {
                    mTextView.setTextColor(getView().getResources().getColor(R.color.theme_main_text));
                    mIvSel.setVisibility(View.VISIBLE);
                } else {
                    mTextView.setTextColor(getView().getResources().getColor(R.color.theme_black_text));
                    mIvSel.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public interface IListSel {
        void onItemSel(SellerCatesInfo item);
    }
}
