package com.yizan.community.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yizan.community.R;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-12-16
 * Time: 10:01
 * FIXME
 */
public class NoticeDialog extends Dialog{
    private String mNotice;
    public NoticeDialog(Context context) {
        super(context, R.style.Dialog_Fullscreen);
    }

    public NoticeDialog(Context context, int theme) {
        super(context, theme);
    }

    protected NoticeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setNotice(String notice){
        this.mNotice = notice;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_notice);
        //设置全屏
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView tv = (TextView)findViewById(R.id.tv_notice);
        if(!TextUtils.isEmpty(mNotice)){
            tv.setText(mNotice);
        }
        findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
