package com.yizan.community.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yizan.community.R;


/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-08-21
 * Time: 18:05
 * FIXME
 */
public class LoadingFooter {
    protected View mLoadingFooter;

    TextView mLoadingText;
    ProgressBar mProgressBar;

    protected State mState = State.Idle;

    public static enum State {
        Idle, TheEnd, Loading
    }

    public LoadingFooter(Context context) {

        mLoadingFooter = LayoutInflater.from(context).inflate(R.layout.loading_footer, null);
        mLoadingFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 屏蔽点击
            }
        });
        mLoadingText = (TextView) mLoadingFooter.findViewById(R.id.tv_state);
        mProgressBar = (ProgressBar)mLoadingFooter.findViewById(R.id.pb_loading);

        setState(State.Idle);
    }

    public View getView() {
        return mLoadingFooter;
    }

    public State getState() {
        return mState;
    }

    public void setState(final State state, long delay) {
        mLoadingFooter.postDelayed(new Runnable() {
            @Override
            public void run() {
                setState(state);
            }
        }, delay);
    }

    public void setState(State status) {
        if (mState == status) {
            return;
        }
        mState = status;

        mLoadingFooter.setVisibility(View.VISIBLE);
        switch (status) {
            case Loading:
                mProgressBar.setVisibility(View.VISIBLE);
                mLoadingText.setVisibility(View.VISIBLE);
                mLoadingText.setText(R.string.footer_loading);
                break;
            case TheEnd:
                mLoadingText.setVisibility(View.VISIBLE);
                mLoadingText.setText(R.string.footer_load_end);
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
                mLoadingFooter.setVisibility(View.GONE);
                break;
        }
    }
}
