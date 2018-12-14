package com.jiankunking.logsearch.config;

public class Limits {
    /**
     * 离线任务 线程数 上限
     * 需要考虑es集群的性能
     */
    public static final int OFF_LINE_TASK_THREAD_LIMIT = 5;

    /**
     * 压缩任务 线程数 上限
     */
    public static final int ZIP_TASK_THREAD_LIMIT = 2;
}
