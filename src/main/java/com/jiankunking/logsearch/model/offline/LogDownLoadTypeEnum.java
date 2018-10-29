package com.jiankunking.logsearch.model.offline;

/**
 * @author jiankunking.
 * @date：2018/9/29 16:44
 * @description: 区分是关键字搜索下载  按照instance下载(上下文、离线下载都是这个)
 */
public enum LogDownLoadTypeEnum {
    /**
     * 按照关键字下载
     */
    keyword,
    /**
     * 按照instance下载(上下文、离线下载都是这个)下载
     */
    instance;
}
