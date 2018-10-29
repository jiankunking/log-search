package com.jiankunking.logsearch.util;

import java.util.concurrent.*;

/**
 * @author jiankunking.
 * @date：2018/9/10 11:11
 * @description:
 */
public class ThreadPoolUtils {

    private ExecutorService exec = null;

    public ThreadPoolUtils(int poolSize) {
        exec = new ThreadPoolExecutor(poolSize, poolSize, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10000),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void execute(Runnable command) {
        exec.execute(command);
    }

    /**
     * 子线程执行结束future.get()返回null，若没有执行完毕，主线程将会阻塞等待
     *
     * @param command
     * @return
     */
    public Future submit(Runnable command) {
        return exec.submit(command);
    }

    /**
     * 子线程中的返回值可以从返回的future中获取：future.get();
     *
     * @param command
     * @return
     */
    public Future submit(Callable command) {
        return exec.submit(command);
    }
}
