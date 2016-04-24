package com.yizan.community.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yizan.community.R;

/**
 * Created by ztl on 2015/9/22.
 */
public class NewHelpActivity extends BaseActivity implements BaseActivity.TitleListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_help);
        setTitleListener(this);
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.new_help);
    }
}
