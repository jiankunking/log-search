package com.jiankunking.logsearch.util;

/**
 * @author jiankunking.
 * @date：2018/9/30 17:12
 * @description:
 */
public class IntUtils {
    /**
     * 获取循环次数
     *
     * @param total
     * @param pageSize
     * @return
     */
    public static int getCycleCount(int total, int pageSize) {
        return (total / pageSize) + 1;
    }

}
