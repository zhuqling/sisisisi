package com.yizan.community.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yizan.community.R;
import com.yizan.community.fragment.MerchandiseFragment;
import com.yizan.community.fragment.ShopFragment;
import com.zongyou.library.app.FragmentUtil;

/**
 * 收藏ListActivity
 * Created by ztl on 2015/9/21.
 */
public class MyCollectionActivity extends BaseActivity implements BaseActivity.TitleListener ,View.OnClickListener{

    private RelativeLayout mMerchandise,mShop;
    private FragmentManager fragmentManager;
    private MerchandiseFragment merchandiseFragment;
    private ShopFragment shopFragment;
    private TextView mTextMerchandise,mTextShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collect);
        setTitleListener(this);

        initViews();
        fragmentManager = getSupportFragmentManager();

        //开始默认选中商品
        setTabSelection(0);
    }

    private void initViews() {
        mMerchandise = (RelativeLayout)findViewById(R.id.merchandise);
        mMerchandise.setOnClickListener(this);
        mShop = (RelativeLayout)findViewById(R.id.shop);
        mShop.setOnClickListener(this);
        mTextMerchandise = (TextView)findViewById(R.id.text_merchandise);
        mTextShop = (TextView)findViewById(R.id.text_shop);
    }

    private void setTabSelection(int index){
        clearSelection();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragment(transaction);
        switch (index){
            case 0:
                FragmentUtil.turnToFragment(getSupportFragmentManager(),R.id.frame_content,MerchandiseFragment.class,null,false);
                mMerchandise.setBackgroundResource(R.drawable.cornor_border_theme_meichandise);
                mTextMerchandise.setTextColor(getResources().getColor(R.color.white));
                break;
            case 1:
                FragmentUtil.turnToFragment(getSupportFragmentManager(),R.id.frame_content,ShopFragment.class,null,false);
                mShop.setBackgroundResource(R.drawable.cornor_border_theme_shop);
                mTextShop.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }
    //清除选中状态
    private void clearSelection(){
        mMerchandise.setBackgroundResource(R.drawable.cornor_border);
        mTextMerchandise.setTextColor(getResources().getColor(R.color.theme_black_text));
        mShop.setBackgroundResource(R.drawable.cornor_border);
        mTextShop.setTextColor(getResources().getColor(R.color.theme_black_text));
    }
    //将所有的Fragment都设置为隐藏状态
    private void hideFragment(FragmentTransaction transaction){
        if(merchandiseFragment != null){
            transaction.hide(merchandiseFragment);
        }
        if(shopFragment != null){
            transaction.hide(shopFragment);
        }
    }



    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(getResources().getString(R.string.my_collection_title));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.merchandise:
                setTabSelection(0);
                break;
            case R.id.shop:
                setTabSelection(1);
                break;
        }
    }
}
