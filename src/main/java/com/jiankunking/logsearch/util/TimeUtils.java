package com.jiankunking.logsearch.util;


import java.util.Calendar;
import java.util.Date;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class TimeUtils {
    /**
     * timestamp 转 date
     *
     * @param timestamp
     * @return
     */
    public static Date toDate(long timestamp) {
        Date date = new Date(timestamp);
        //SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        //bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return date;
    }

    /**
     * 日期减一
     * 2017-05-15
     *
     * @param date
     * @return
     */
    public static Date getYesterdayDayDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return date;
    }
}
