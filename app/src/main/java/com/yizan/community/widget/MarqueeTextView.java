package com.yizan.community.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-12-15
 * Time: 20:58
 * FIXME
 */
public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
