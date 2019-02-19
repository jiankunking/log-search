package com.jiankunking.logsearch.services;

import com.jiankunking.logsearch.cache.IndexRetentionTimeCache;
import com.jiankunking.logsearch.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * @author jiankunking.
 * @date：2018/10/19 7:51
 * @description:
 */
@Slf4j
@Service
public class IndexPrefixService {

    @Autowired
    IndexRetentionTimeCache indexRetentionTimeCache;

    private String prefix = "filebeat-";
    private String format = "yyyy.MM.dd";

    public String getIndexPrefix(String cluster, long fromTime, long toTime) {
        StringBuilder indexNames = new StringBuilder();

        HashMap<String, Integer> indexRetentionTimeMap = indexRetentionTimeCache.getIndexRetentionTimeMap();
        //consul 中没有维护 索引过期时间
        if (!indexRetentionTimeMap.containsKey(cluster)) {
            indexNames.append(prefix);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(fromTime));
            //起始时间 回退8小时 规避docker 日志 索引不存在问题
            calendar.add(Calendar.HOUR, -8);
            Date date = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String fromDate = sdf.format(date);
            String toDate = sdf.format(new Date(toTime));
            String commonPrefix = this.getMaxCommonPrefix(fromDate, toDate);
            if (commonPrefix.length() != format.length()) {
                indexNames.append(commonPrefix);
                indexNames.append("*");
            } else {
                //fromDate toDate相同时 不拼接*
                indexNames.append(commonPrefix);
            }
            //log.info("IndexPrefix:" + indexNames);
            return indexNames.toString();
        }
        long eightHours = 1000 * 60 * 60 * 8;
        fromTime = fromTime - eightHours;

        long fromTimeDay = this.getDayTimestamp(fromTime);
        long toTimeDay = this.getDayTimestamp(toTime);
        int indexRetentionTime = indexRetentionTimeMap.get(cluster);
        HashMap<Long, String> lastDaysTimestamp = this.getLastDaysTimestamp(indexRetentionTime);
        for (Long day : lastDaysTimestamp.keySet()) {
            if (day >= fromTimeDay && day <= toTimeDay) {
                if (indexNames.length() == 0) {
                    indexNames.append(lastDaysTimestamp.get(day));
                    continue;
                }
                indexNames.append(",");
                indexNames.append(lastDaysTimestamp.get(day));
            }
        }

        //1、如果查询时间不在es index保留时间内 为防止报错 则返回当天的index 这时不会查询到数据
        if (indexNames.length() == 0) {
            indexNames.append(getCurrentDayIndex(lastDaysTimestamp));
            //log.warn("Query time exceeded limit or query today :" + indexNames.toString());
            return indexNames.toString();
        }
        //log.info("IndexPrefix:" + indexNames);
        return indexNames.toString();
    }

    private String getCurrentDayIndex(HashMap<Long, String> lastDaysTimestamp) {
        Long maxKey = new Long(0);
        for (Long key : lastDaysTimestamp.keySet()) {
            if (key > maxKey) {
                maxKey = key;
            }
        }
        return lastDaysTimestamp.get(maxKey);
    }


    private String getMaxCommonPrefix(String str1, String str2) {
        if (str1.equals(str2)) {
            return str1;
        }
        String temp1, temp2;
        for (int index = 1; index < str1.length(); index++) {
            temp1 = str1.substring(0, str1.length() - index);
            temp2 = str2.substring(0, str2.length() - index);
            if (temp1.equals(temp2)) {
                return temp1;
            }
        }
        return "";
    }

    private HashMap<Long, String> getLastDaysTimestamp(int indexRetentionTime) {
        HashMap<Long, String> result = new HashMap<>(MapUtils.getSize(7));
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < indexRetentionTime; i++) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            DateFormat dateFormat = new SimpleDateFormat(format);
            String date = dateFormat.format(calendar.getTime());
            String dates = String.format("%d-%d-%d 00:00:00", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            result.put(Timestamp.valueOf(dates).getTime(), prefix + date);
        }
        return result;
    }

    private Long getDayTimestamp(Long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timestamp));
        String dates = String.format("%d-%d-%d 00:00:00", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        return Timestamp.valueOf(dates).getTime();
    }
}
