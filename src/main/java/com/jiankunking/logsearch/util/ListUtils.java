package com.jiankunking.logsearch.util;

import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class ListUtils {

    public static String toString(List<String> list, String separator) {
        String result = "";
        if (list == null || list.size() == 0) {
            return result;
        }
        for (int i = 0; i < list.size(); i++) {
            if (StringUtils.isEmpty(result)) {
                result = list.get(i);
                continue;
            }
            result = result + separator + list.get(i);
        }
        return result;
    }

    ///**
    // * 移除offset最小的元素
    // *
    // * @param list
    // * @return
    // */
    //public static void removeMinOffsetItem(List<Map<String, Object>> list) {
    //    //获取最小的offset
    //
    //    if (list == null || list.size() == 0) {
    //        return;
    //    }
    //    int minOffset = (int) list.get(0).get("offset");
    //    for (Map<String, Object> item : list) {
    //        if (minOffset > (int) item.get("offset")) {
    //            minOffset = (int) item.get("offset");
    //        }
    //    }
    //    //移除最小元素
    //    Iterator<Map<String, Object>> iter = list.iterator();
    //    while (iter.hasNext()) {
    //        Map<String, Object> item = iter.next();
    //        if ((int) item.get("offset") == minOffset) {
    //            iter.remove();
    //            return;
    //        }
    //    }
    //}
}
