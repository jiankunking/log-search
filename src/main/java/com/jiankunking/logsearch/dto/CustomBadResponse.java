package com.jiankunking.logsearch.dto;


import java.io.Serializable;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class CustomBadResponse implements Serializable {

    private static final long serialVersionUID = -750644833749014618L;

    private Integer code;
    private String msg;


    public static CustomBadResponse fail(Integer code, String error) {
        CustomBadResponse resp = new CustomBadResponse();
        resp.setCode(code);
        resp.setMsg(error);
        return resp;
    }

    public static CustomBadResponse fail(Integer code, Exception error) {
        CustomBadResponse resp = new CustomBadResponse();
        resp.setCode(code);
        resp.setMsg(error.getMessage());
        return resp;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
