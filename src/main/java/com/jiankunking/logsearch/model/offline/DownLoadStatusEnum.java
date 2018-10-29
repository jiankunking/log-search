package com.jiankunking.logsearch.model.offline;

/**
 * @author jiankunking.
 * @date：2018/9/29 11:18
 * @description:
 */
public enum DownLoadStatusEnum {
    /**
     * 下载成功
     */
    success,
    /**
     * 下载失败
     */
    fail,
    /**
     * 压缩中
     */
    ziping,
    /**
     * 压缩失败
     */
    zipFail,
    /**
     * 下载中
     */
    downloading;
}
