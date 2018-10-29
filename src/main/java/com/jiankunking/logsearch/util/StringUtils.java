package com.jiankunking.logsearch.util;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class StringUtils {
    /**
     * 判断字符串是否为空
     *
     * @param string
     * @return
     */
    public static boolean isEmpty(String string) {
        boolean falg = false;
        if (string == null || string.trim().length() == 0) {
            falg = true;
        }
        return falg;
    }


    /**
     * 判断字符串是否不为空
     *
     * @param string
     * @return
     */
    public static boolean isNotEmpty(String string) {
        boolean falg = false;
        if (string != null && string.trim().length() > 0) {
            falg = true;
        }
        return falg;
    }
}
