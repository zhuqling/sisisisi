package com.yizan.community.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yizan.community.R;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-24
 * Time: 15:43
 * FIXME
 */
public class CommentStarView extends RelativeLayout implements View.OnClickListener{
    private View mRootView;
//    private ImageView []mImageViews = new ImageView[5];
    private ImageView mImageView;
    public CommentStarView(Context context) {
        super(context);
        initView(context);
    }

    public CommentStarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }


    private void initView(Context context){
        if(isInEditMode()){
            return;
        }
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_comment_star, null);

        this.addView(mRootView);
        ViewGroup.LayoutParams layoutParams = mRootView.getLayoutParams();
        mRootView.setLayoutParams(layoutParams);
        mImageView = (ImageView)mRootView.findViewById(R.id.iv_star);
//        mImageViews[0] = (ImageView)mRootView.findViewById(R.id.iv_star1);
//        mImageViews[1] = (ImageView)mRootView.findViewById(R.id.iv_star2);
//        mImageViews[2] = (ImageView)mRootView.findViewById(R.id.iv_star3);
//        mImageViews[3] = (ImageView)mRootView.findViewById(R.id.iv_star4);
//        mImageViews[4] = (ImageView)mRootView.findViewById(R.id.iv_star5);

    }

    public void setStar(double star){
        if(star < 1.0){
            mImageView.setImageResource(R.drawable.ic_star_05);
        }else if (star == 1.0){
            mImageView.setImageResource(R.drawable.ic_star_10);
        }else if (star < 2.0){
            mImageView.setImageResource(R.drawable.ic_star_15);
        }else if (star == 2.0){
            mImageView.setImageResource(R.drawable.ic_star_20);
        }else if (star < 3.0){
            mImageView.setImageResource(R.drawable.ic_star_25);
        }else if (star == 3.0){
            mImageView.setImageResource(R.drawable.ic_star_30);
        }else if (star < 4.0){
            mImageView.setImageResource(R.drawable.ic_star_35);
        }else if (star == 4.0){
            mImageView.setImageResource(R.drawable.ic_star_40);
        }else if (star < 5.0){
            mImageView.setImageResource(R.drawable.ic_star_45);
        }else {
            mImageView.setImageResource(R.drawable.ic_star_50);
        }
    }
    @Override
    public void onClick(View v) {


    }
}
