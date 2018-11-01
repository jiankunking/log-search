package com.jiankunking.logsearch.queue;

import com.jiankunking.logsearch.config.Limits;
import org.springframework.stereotype.Component;

@Component
public class PoolSizeService {


    public int getZipTaskPoolSize() {
        int poolSize = 1;
        if (Runtime.getRuntime().availableProcessors() >= 6) {
            poolSize = Runtime.getRuntime().availableProcessors() / 3;
        }
        if (poolSize < Limits.ZIP_TASK_THREAD_LIMIT) {
            return poolSize;
        }
        return Limits.ZIP_TASK_THREAD_LIMIT;
    }

    public int getOffLineTaskPoolSize() {
        int poolSize = Runtime.getRuntime().availableProcessors() * 5;
        if (poolSize < Limits.OFF_LINE_TASK_THREAD_LIMIT) {
            return poolSize;
        }
        return Limits.OFF_LINE_TASK_THREAD_LIMIT;
    }
}
