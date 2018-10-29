package com.jiankunking.logsearch.exception;

/**
 * @author jiankunking.
 * @date：2018/9/28 14:41
 * @description:
 */
public class ESClientNotFoundException extends Exception {

    public ESClientNotFoundException() {
        super();
    }

    public ESClientNotFoundException(String message) {
        super(message);
    }

    public ESClientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESClientNotFoundException(Throwable cause) {
        super(cause);
    }
}
