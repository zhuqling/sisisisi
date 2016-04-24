package com.yizan.community.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.yizan.community.R;
import com.yizan.community.adapter.MessageAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.MessageInfo;
import com.fanwe.seallibrary.model.result.AddressResult;
import com.fanwe.seallibrary.model.result.BaseResult;
import com.fanwe.seallibrary.model.result.MessageResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.LogUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ztl on 2015/9/22.
 */
public class ServerMessageActivity extends BaseActivity implements BaseActivity.TitleListener {

    private MessageAdapter adapter;
    private ListView mListView;
    private List<MessageInfo> listData = new ArrayList<>();
    private View emptyView;
    private boolean mLoadMore;
    private PopupWindow popupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_msg);
        setTitleListener(this);
        initViews();
    }

    private void initViews() {
        mListView = (ListView)findViewById(R.id.message_lv);
        emptyView = findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyView);
        adapter = new MessageAdapter(ServerMessageActivity.this,listData);
        mListView.setAdapter(adapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!mLoadMore && firstVisibleItem + visibleItemCount >= totalItemCount && adapter.getCount() >= Constants.PAGE_SIZE && adapter.getCount() % Constants.PAGE_SIZE == 0) {
                    getMessage(false);
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MessageInfo info = listData.get(position);
                if (info.isRead == Constants.MSG_UNREAD) {
                    //消息已读
                    listData.get(position).isRead = Constants.MSG_READ;
                    adapter.notifyDataSetChanged();
                    sendMsgIsRead(info.id);
                }
                if (info.type == 1) {
                    Intent intent = new Intent(ServerMessageActivity.this, MessageDetailActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable(Constants.EXTRA_DATA, info);
                    intent.putExtras(b);
                    startActivity(intent);
                } else if (listData.get(position).type == 2) {
                    Intent intent = new Intent(ServerMessageActivity.this, WebViewActivity.class);
                    intent.putExtra(Constants.EXTRA_URL, info.args);
                    startActivity(intent);
                } else if (listData.get(position).type == 3) {
                    Intent intent = new Intent(ServerMessageActivity.this, OrderDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.EXTRA_DATA, Integer.parseInt(info.args));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                initPop(view, i);
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow = null;
        }
        return super.onTouchEvent(event);
    }

    private void initPop(View view, final int position){
        View convertView = LayoutInflater.from(this).inflate(R.layout.pop_delete,null);

        convertView.setBackgroundResource(R.drawable.style_edt_boder);
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth()/3;
        popupWindow = new PopupWindow(convertView,width, LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        int xPos = -popupWindow.getWidth() / 2;
        popupWindow.showAsDropDown(view, xPos, 4);
        convertView.findViewById(R.id.pop_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msgDelete(adapter.getItem(position).id);
                popupWindow.dismiss();
            }
        });
    }

    //消息删除
    private void msgDelete(int id){
        List<Integer> integerList = new ArrayList<>();
        integerList.add(id);
        HashMap<String,List<Integer>> data = new HashMap<String,List<Integer>>();
        data.put("id", integerList);
        ApiUtils.post(this, URLConstants.MESSAGE_DELETE, data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
                if(O2OUtils.checkResponse(ServerMessageActivity.this, response)) {
                    getMessage(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtils.show(ServerMessageActivity.this, R.string.msg_error);
            }
        });
    }

    /**
     * 调用接口标记消息为已读
     * @param id
     */
    private void sendMsgIsRead(int id){
        Map<String,Integer> data = new HashMap<>();
        data.put("id", id);
        ApiUtils.post(ServerMessageActivity.this, URLConstants.MESGREAD, data, BaseResult.class, new Response.Listener<BaseResult>() {
            @Override
            public void onResponse(BaseResult response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    /**
     * 消息查询
     * @param isRefresh
     */
    private void getMessage(final boolean isRefresh){
        if (mLoadMore)
            return;
        if (!NetworkUtils.isNetworkAvaiable(ServerMessageActivity.this)) {
            ToastUtils.show(ServerMessageActivity.this, R.string.msg_error_network);
            return;
        }
        if(isRefresh) {
            CustomDialogFragment.show(getSupportFragmentManager(), R.string.msg_loading, ServerMessageActivity.class.getName());
            listData.clear();
        }
        mLoadMore = true;
        Map<String,String> data = new HashMap<>();
        data.put("page",String.valueOf(listData.size()/ Constants.PAGE_SIZE+1));
        ApiUtils.post(this, URLConstants.MSG_LIST, data, MessageResult.class, new Response.Listener<MessageResult>() {
            @Override
            public void onResponse(MessageResult response) {
                if (isRefresh)
                    listData.clear();
                if (O2OUtils.checkResponse(ServerMessageActivity.this, response)) {
                    if (!ArraysUtils.isEmpty(response.data))
                        listData.addAll(response.data);
                }
                adapter.notifyDataSetChanged();
                CustomDialogFragment.dismissDialog();
                mLoadMore = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomDialogFragment.dismissDialog();
                mLoadMore = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMessage(true);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(getResources().getString(R.string.service_msg_title));
    }
}
