package com.jiankunking.logsearch.exception;

/**
 * @author jiankunking.
 * @date：2018/9/28 13:58
 * @description: consul中没有找到对应的es集群配置
 */
public class ESClusterNotFoundException extends Exception {
    public ESClusterNotFoundException() {
        super();
    }

    public ESClusterNotFoundException(String message) {
        super(message);
    }

    public ESClusterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESClusterNotFoundException(Throwable cause) {
        super(cause);
    }
}
