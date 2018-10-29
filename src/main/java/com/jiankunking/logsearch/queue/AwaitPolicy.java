package com.jiankunking.logsearch.queue;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jiankunking.
 * @date：2018/10/15 16:54
 * @description: 希望线程池处理不过来的时候 进行等待
 */
public class AwaitPolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        executor.getQueue().add(r);
    }
}
