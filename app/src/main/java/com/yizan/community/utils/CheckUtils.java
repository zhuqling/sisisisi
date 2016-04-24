package com.yizan.community.utils;

import android.text.TextUtils;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-17
 * Time: 11:17
 * FIXME
 */
public class CheckUtils {
    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {

        String telRegex = "[1][345789]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }
}
