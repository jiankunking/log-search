package com.jiankunking.logsearch.exception;

/**
 * @author jiankunking.
 * @date：2018/12/19 08:41
 * @description:
 */
public class ESClustersResponseTimeoutException extends Exception {

    public ESClustersResponseTimeoutException() {
        super();
    }

    public ESClustersResponseTimeoutException(String message) {
        super(message);
    }

    public ESClustersResponseTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESClustersResponseTimeoutException(Throwable cause) {
        super(cause);
    }
}
