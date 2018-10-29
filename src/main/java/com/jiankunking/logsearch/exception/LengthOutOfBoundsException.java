package com.jiankunking.logsearch.exception;


/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description: 长度 超长异常
 */
public class LengthOutOfBoundsException extends Exception {

    public LengthOutOfBoundsException() {
        super();
    }

    public LengthOutOfBoundsException(String message) {
        super(message);
    }

    public LengthOutOfBoundsException(String message, Throwable cause) {
        super(message, cause);
    }

    public LengthOutOfBoundsException(Throwable cause) {
        super(cause);
    }

}
