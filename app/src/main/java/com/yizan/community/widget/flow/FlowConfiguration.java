package com.yizan.community.widget.flow;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.yizan.community.R;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-29
 * Time: 17:00
 * FIXME
 */
public class FlowConfiguration {
    private static final int DEFAULT_LINE_SPACING = 5;
    private static final int DEFAULT_TAG_SPACING = 10;

    private int lineSpacing;
    private int tagSpacing;

    public FlowConfiguration(Context context,AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        try {
            lineSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_lineSpacing, DEFAULT_LINE_SPACING);
            tagSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_tagSpacing, DEFAULT_TAG_SPACING);
        } finally {
            a.recycle();
        }
    }

    public int getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public int getTagSpacing() {
        return tagSpacing;
    }

    public void setTagSpacing(int tagSpacing) {
        this.tagSpacing = tagSpacing;
    }
}
