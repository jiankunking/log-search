package com.jiankunking.logsearch.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author jiankunking.
 * @date：2018/10/19 7:51
 * @description:
 */
@Slf4j
@Service
public class IndexPrefixService {

    public String getIndexPrefix(long fromTime, long toTime) {
        String prefix = "filebeat-";
        String format = "yyyy.MM.dd";

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
            prefix = prefix + commonPrefix + "*";
        } else {
            //fromDate toDate相同时 不拼接*
            prefix = prefix + commonPrefix;
        }
        //log.info("IndexPrefix:" + prefix);
        return prefix;

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
}
