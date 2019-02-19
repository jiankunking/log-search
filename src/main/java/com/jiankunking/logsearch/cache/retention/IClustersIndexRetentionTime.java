package com.jiankunking.logsearch.cache.retention;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * @author jiankunking.
 * @date：2019/01/11 18:22
 * @description:
 */
public interface IClustersIndexRetentionTime {
    /**
     * 获取集群 index  保存时间
     *
     * @return
     */
    HashMap<String, Integer> getClustersIndexRetentionTime() throws UnsupportedEncodingException;
}
