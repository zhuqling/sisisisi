package com.yizan.community.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by Administrator on 2015/11/26.
 */
public class NumFormat {

    public static String formatPrice(double v) {
        DecimalFormat df = new DecimalFormat("0.0#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(v);
    }

}
