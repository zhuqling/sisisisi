package com.yizan.community.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.yizan.community.R;
import com.yizan.community.adapter.PopAllCatesAdapter;
import com.yizan.community.adapter.RecommentSellerListAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.SellerCatesInfo;
import com.fanwe.seallibrary.model.SellerInfo;
import com.fanwe.seallibrary.model.result.SellerCatesResult;
import com.fanwe.seallibrary.model.result.SellerResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.utils.RefreshLayoutUtils;
import com.yizan.community.widget.doublemenu.DoubleListViewHolder;
import com.yizan.community.widget.doublemenu.PopMenuButton;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家分类ListActivity
 * Created by ztl on 2015/9/22.
 */
public class BusinessClassificationActivity extends BaseActivity implements
        BaseActivity.TitleListener, View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, DoubleListViewHolder.IListSel, PopMenuButton.IPopMenu {

    private List<SellerCatesInfo> listSellerCates = new ArrayList<>();
    private RelativeLayout mAll, mComplex;
    private ListView mPopListView, mListView;
    private PopupWindow popupWindowAll, popupWindowComplex;
    private TextView mAllText, mComplexText;
    private List<SellerInfo> listDatas = null;
    private boolean mLoadMore = false;
    private View emptyView;
    private RecommentSellerListAdapter mAdapter;
    private int sort = 0;
    private String ids = "0";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPage = 1;
    private TextView newTitle;

    private PopMenuButton mPopMenuButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_classification);
        setTitleListener_RightImage(this);

        Intent intent = this.getIntent();
        mAllText = (TextView) findViewById(R.id.business_all_text);
        ids = intent.getStringExtra(Constants.EXTRA_DATA);
        mAllText.setText(intent.getStringExtra(Constants.EXTRA_TITLE));
        initViews();
    }

    private void initViews() {
        mSwipeRefreshLayout = mViewFinder.find(R.id.swipe_container);
        RefreshLayoutUtils.initSwipeRefreshLayout(BusinessClassificationActivity.this, mSwipeRefreshLayout, this, true);
//        mAll = (RelativeLayout)findViewById(R.id.business_all_cates);
//        mAll.setOnClickListener(this);
        mComplex = (RelativeLayout) findViewById(R.id.business_complex_cates);
        mComplex.setOnClickListener(this);


        mComplexText = (TextView) findViewById(R.id.business_complex_text);
        mComplexText.setText(R.string.sort_all);

        listDatas = new ArrayList<SellerInfo>();
        mListView = (ListView) findViewById(R.id.business_classification_lv);
        emptyView = findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyView);
        mAdapter = new RecommentSellerListAdapter(BusinessClassificationActivity.this, listDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!mLoadMore && firstVisibleItem + visibleItemCount >= totalItemCount && mAdapter.getCount() >= Constants.PAGE_SIZE && mAdapter.getCount() % Constants.PAGE_SIZE == 0) {
                    getSellerLists(false);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                int type = 1;
                SellerInfo info = listDatas.get(position);
                if (ids.equals("0") || ArraysUtils.isEmpty(listSellerCates)) {
                } else {
                    for (SellerCatesInfo item : listSellerCates) {
                        if (item.id == Integer.parseInt(ids)) {
                            type = item.type;
                            break;
                        }
                    }
                }
                if (type == 1) {
                    intent = new Intent(BusinessClassificationActivity.this, SellerGoodsActivity.class);
                } else {
                    intent = new Intent(BusinessClassificationActivity.this, SellerServicesActivity.class);
                }
                intent.putExtra(Constants.EXTRA_DATA, listDatas.get(position).id);
                startActivity(intent);
            }
        });

        new Handler().postDelayed(new Runnable() {
            public void run() {
                getSellerLists(true);
            }
        }, 100);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.business_all_cates:
                initAllPop();
                return;
            case R.id.business_complex_cates:
                initComplexPop();
                return;
            case R.id.tv_zonghe:
                sort = 0;
                break;
            case R.id.tv_xiaoliang:
                sort = 1;
                break;
            case R.id.tv_qisong:
                sort = 2;
                break;
            case R.id.sort_distance:
                sort = 3;
                break;
            case R.id.sort_eva:
                sort = 4;
                break;
        }
        mComplexText.setText(((TextView) v).getText().toString());
        getSellerLists(true);
        popupWindowComplex.dismiss();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (popupWindowAll != null && popupWindowAll.isShowing()) {
            popupWindowAll.dismiss();
            popupWindowAll = null;
        }
        if (popupWindowComplex != null && popupWindowComplex.isShowing()) {
            popupWindowComplex.dismiss();
            popupWindowComplex = null;
        }
        return super.onTouchEvent(event);
    }

    private void getSellerLists(final boolean isRefresh) {
        if (!checkLoadState(isRefresh)) {
            return;
        }

//        CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, BusinessClassificationActivity.class.getName());
        Map<String, String> data = new HashMap<>();
        data.put("sort", String.valueOf(sort));
        data.put("page", String.valueOf(mPage));
        data.put("id", ids);
        data.put("keyword", "");
        mLoadMore = true;
        ApiUtils.post(this, URLConstants.SELLERLISTS, data, SellerResult.class, new Response.Listener<SellerResult>() {
            @Override
            public void onResponse(SellerResult response) {
                mLoadMore = false;
                mSwipeRefreshLayout.setRefreshing(false);
                if (isRefresh) {
                    listDatas.clear();
                    mAdapter.notifyDataSetChanged();
                }

                if (O2OUtils.checkResponse(BusinessClassificationActivity.this, response)) {
                    if (!ArraysUtils.isEmpty(response.data)) {
                        listDatas.addAll(response.data);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        ToastUtils.show(BusinessClassificationActivity.this, "没有更多");
                    }

                }

//                CustomDialogFragment.dismissDialog();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                CustomDialogFragment.dismissDialog();
                mLoadMore = false;
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void initComplexPop() {
        View convertView = LayoutInflater.from(this).inflate(R.layout.pop_sort, null);
        convertView.setBackgroundColor(Color.WHITE);
        convertView.setBackgroundResource(R.drawable.style_edt_boder);
        int width = mComplex.getWidth();
        popupWindowComplex = new PopupWindow(convertView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindowComplex.setOutsideTouchable(true);
        popupWindowComplex.setFocusable(true);
        popupWindowComplex.setTouchable(true);
        popupWindowComplex.setBackgroundDrawable(new BitmapDrawable());
        int xoffset = 0;
        int yoffset = 10;
        popupWindowComplex.showAsDropDown(mComplex, xoffset, yoffset);
        convertView.findViewById(R.id.tv_zonghe).setOnClickListener(this);
        convertView.findViewById(R.id.tv_xiaoliang).setOnClickListener(this);
        convertView.findViewById(R.id.tv_qisong).setOnClickListener(this);
        convertView.findViewById(R.id.sort_distance).setOnClickListener(this);
        convertView.findViewById(R.id.sort_eva).setOnClickListener(this);
        popupWindowComplex.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                shoePopState(mComplexText, false);
            }
        });
        shoePopState(mComplexText, true);
    }

    private void initAllPop() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.all_cates_pop_activity, null);
        mPopListView = (ListView) contentView.findViewById(R.id.lv_all_cates);
        PopAllCatesAdapter popAdapter = new PopAllCatesAdapter(getApplicationContext(), listSellerCates);
        mPopListView.setAdapter(popAdapter);
        contentView.setBackgroundColor(Color.WHITE);
        contentView.setBackgroundResource(R.drawable.style_edt_boder);
        int width = mAll.getWidth();
        popupWindowAll = new PopupWindow(contentView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindowAll.setOutsideTouchable(true);
        popupWindowAll.setFocusable(true);
        popupWindowAll.setTouchable(true);
        popupWindowAll.setBackgroundDrawable(new BitmapDrawable());
        int xoffset = 0;
        int yoffset = 10;
        popupWindowAll.showAsDropDown(mAll, xoffset, yoffset);
        mPopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAllText.setText(listSellerCates.get(position).name);
                ids = String.valueOf(listSellerCates.get(position).id);
                getSellerLists(true);
                popupWindowAll.dismiss();
            }
        });
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        newTitle = title;
        String titleText = getIntent().getStringExtra(Constants.EXTRA_TITLE);
        if(TextUtils.isEmpty(titleText)) {
            title.setText(R.string.business_classification);
        }else{
            title.setText(titleText);
        }
        ((ImageButton) right).setImageResource(R.drawable.ic_search);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BusinessClassificationActivity.this, SearchActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getBusinessClassik();
    }

    private void initPopListDatas(List<SellerCatesInfo> datas) {
        listSellerCates.clear();
        if (ArraysUtils.isEmpty(datas)) {
            return;
        } else {
            listSellerCates.add(new SellerCatesInfo("全部"));
            listSellerCates.addAll(datas);
        }
        if(TextUtils.isEmpty(ids)){
            ids = "0";
        }
        defaultSelCate(Integer.parseInt(ids));
        mPopMenuButton = (PopMenuButton) findViewById(R.id.pb_cates);

        DoubleListViewHolder holder = new DoubleListViewHolder();

        mPopMenuButton.setContentView(holder.getView());
        mPopMenuButton.setIPopMenu(this);
        holder.refreshView(listSellerCates);
        holder.setIListSel(this);
    }

    private void defaultSelCate(int defaultId) {
        if (ArraysUtils.isEmpty(listSellerCates)) {
            return ;
        }
        boolean isSel = false;

        for (SellerCatesInfo item : listSellerCates) {
            if (item.id == defaultId) {

                item.checked = true;
                item.selected = true;
                return ;
            } else {
                item.checked = false;
                if(ArraysUtils.isEmpty(item.childs)){
                    continue;
                }
                for (SellerCatesInfo child : item.childs){
                    if(child.id == defaultId){
                        child.selected = true;
                        item.checked = true;
                        return ;
                    }

                }
            }
        }
        if (!isSel) {
            listSellerCates.get(0).checked = true;
        }
        return ;
    }



    private void getBusinessClassik() {
        if(!ArraysUtils.isEmpty(listSellerCates)){
            return;
        }
        if (NetworkUtils.isNetworkAvaiable(BusinessClassificationActivity.this)) {
            Map<String, String> data = new HashMap<>();
//            data.put("id",ids);
//            data.put("type",String.valueOf(type));
            ApiUtils.post(BusinessClassificationActivity.this,
                    URLConstants.SELLERCATELISTS, data, SellerCatesResult.class, new Response.Listener<SellerCatesResult>() {
                        @Override
                        public void onResponse(SellerCatesResult response) {
                            initPopListDatas(response.data);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ToastUtils.show(BusinessClassificationActivity.this, R.string.msg_error);
                        }
                    });
        } else {
            ToastUtils.show(BusinessClassificationActivity.this, R.string.msg_error_network);
        }
    }


    @Override
    public void onRefresh() {
        getSellerLists(true);
    }

    protected boolean checkLoadState(boolean isRefresh) {
        if (mLoadMore) {
            return false;
        }
        if (!NetworkUtils.isNetworkAvaiable(BusinessClassificationActivity.this)) {
            ToastUtils.show(BusinessClassificationActivity.this, R.string.msg_error_network);
            mSwipeRefreshLayout.setRefreshing(false);
            return false;
        }
        mLoadMore = true;
        if (isRefresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            mPage = 1;
        } else {
            mPage += 1;
        }
        return true;
    }

    @Override
    public void onItemSel(SellerCatesInfo item) {
        if (mPopMenuButton != null) {
            mPopMenuButton.closeWindow();
        }
        if (item == null) {
            return;
        }
        mAllText.setText(item.name);
        newTitle.setText(item.name);
        ids = String.valueOf(item.id);
        getSellerLists(true);
    }

    @Override
    public void showPopMenu(boolean bShow) {
        if(null == mAllText){
            return;
        }
        shoePopState(mAllText, bShow);
    }

    protected void shoePopState(TextView tv, boolean bShow){
        if(bShow){
            tv.setTextColor(getResources().getColor(R.color.theme_main_text));
            Drawable arrowDown = getResources().getDrawable(R.drawable.ic_menu_arrow_up);
            arrowDown.setBounds(0, 0, arrowDown.getMinimumWidth(), arrowDown.getMinimumHeight());
            tv.setCompoundDrawables(null, null, arrowDown, null);
        }else{

            tv.setTextColor(getResources().getColor(R.color.theme_black_text));
            Drawable arrowDown = getResources().getDrawable(R.drawable.down_arrow);
            arrowDown.setBounds(0, 0, arrowDown.getMinimumWidth(), arrowDown.getMinimumHeight());
            tv.setCompoundDrawables(null, null, arrowDown, null);
        }
    }
}
