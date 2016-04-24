package com.yizan.community.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.DoorKeysInfo;
import com.fanwe.seallibrary.model.event.DoorUpdateEvent;
import com.fanwe.seallibrary.model.result.DoorKeyResult;
import com.fanwe.seallibrary.model.result.SellerCatesResult;
import com.yizan.community.R;
import com.yizan.community.fragment.CustomDialogFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.O2OUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-12-01
 * Time: 16:47
 * FIXME
 */
public class DoorNameDialog extends Dialog implements View.OnClickListener {
    private FragmentActivity context;
    private TextView mTvName;
    private EditText mEtAlias;
    private DoorKeysInfo mDoorKeysInfo;

    public DoorNameDialog(FragmentActivity context, DoorKeysInfo item) {
        super(context, R.style.customer_dialog);
        this.context = context;
        mDoorKeysInfo = item;
        setTitle(R.string.title_dialog_door_name);
        //加载布局文件
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_door_name, null);
        mTvName = (TextView) view.findViewById(R.id.tv_name);
        mEtAlias = (EditText) view.findViewById(R.id.et_alias);
        view.findViewById(R.id.btn_cancle).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        mTvName.setText(mDoorKeysInfo.doorname);
        if(TextUtils.isEmpty(mDoorKeysInfo.remark)){
            mEtAlias.setText(mDoorKeysInfo.doorname);
        }else {
            mEtAlias.setText(mDoorKeysInfo.remark);
        }
        //dialog添加视图
        setContentView(view);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                dismiss();
                break;
            case R.id.btn_ok:
                commitData();
                break;
        }
    }

    private void commitData() {
        if (!NetworkUtils.isNetworkAvaiable(context)) {
            ToastUtils.show(context, R.string.msg_error_network);
        }
        String name = mEtAlias.getText().toString();
        if(TextUtils.isEmpty(name)){
            name = mDoorKeysInfo.doorname;
        }
        CustomDialogFragment.show(context.getSupportFragmentManager(), R.string.loading, context.getClass().getName());
        Map<String, String> data = new HashMap<>();
        data.put("doorid", String.valueOf(mDoorKeysInfo.doorid));
        data.put("doorname", name);
        ApiUtils.post(context,
                URLConstants.USER_EDIT_DOOR, data, DoorKeyResult.class, new Response.Listener<DoorKeyResult>() {
                    @Override
                    public void onResponse(DoorKeyResult response) {
                        CustomDialogFragment.dismissDialog();
                        if(O2OUtils.checkResponse(context, response) && response.data != null){
                            EventBus.getDefault().post(new DoorUpdateEvent(mDoorKeysInfo.doorid, response.data.remark));
                            dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(context, R.string.msg_error);
                        CustomDialogFragment.dismissDialog();
                    }
                });

    }

    public static void show(FragmentActivity context, DoorKeysInfo doorKeysInfo) {
        DoorNameDialog dlg = new DoorNameDialog(context, doorKeysInfo);
        dlg.setTitle(R.string.title_dialog_door_name);
        Window dialogWindow = dlg.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        lp.width = wm.getDefaultDisplay().getWidth() - 40;

        dialogWindow.setAttributes(lp);
        dlg.show();
    }
}
