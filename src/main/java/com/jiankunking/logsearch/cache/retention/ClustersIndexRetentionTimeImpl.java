package com.jiankunking.logsearch.cache.retention;

import com.jiankunking.logsearch.model.ESIndicesRetentionTime;
import com.jiankunking.logsearch.services.ConsulService;
import com.jiankunking.logsearch.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

/**
 * @author jiankunking.
 * @date：2019/01/11 18:22
 * @description:
 */
@Slf4j
@Component
public class ClustersIndexRetentionTimeImpl implements IClustersIndexRetentionTime {

    @Autowired
    ConsulService consulService;

    @Override
    public HashMap<String, Integer> getClustersIndexRetentionTime() throws UnsupportedEncodingException {
        List<ESIndicesRetentionTime> esIndicesRetentionTimeList = consulService.getAllClustersIndexRetentionTime();
        HashMap<String, Integer> result = new HashMap<>(MapUtils.getSize(5));
        for (ESIndicesRetentionTime esIndicesRetentionTime : esIndicesRetentionTimeList) {
            result.put(esIndicesRetentionTime.getCluster(), esIndicesRetentionTime.getRetentionTime());
        }
        return result;
    }
}
