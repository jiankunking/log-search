package com.jiankunking.logsearch.cache;

import com.jiankunking.logsearch.cache.retention.IClustersIndexRetentionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * @author jiankunking.
 * @date：2019/01/11 18:22
 * @description:
 */
@Slf4j
@Component
public class IndexRetentionTimeCache {

    @Autowired
    IClustersIndexRetentionTime iClustersIndexRetentionTime;

    private static HashMap<String, Integer> indexRetentionTimeMap = new HashMap<>();

    /**
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void init() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("IndexRetentionTimeCache init:" + df.format(new Date()));
        try {
            HashMap<String, Integer> result = iClustersIndexRetentionTime.getClustersIndexRetentionTime();
            indexRetentionTimeMap = result;
        } catch (UnsupportedEncodingException e) {
            log.error("IndexRetentionTimeCache init error:", e);
        }
        System.out.println("IndexRetentionTimeCache init finish:" + df.format(new Date()));
    }

    public HashMap<String, Integer> getIndexRetentionTimeMap() {
        return indexRetentionTimeMap;
    }
}
