package com.jiankunking.logsearch.model.external.cmdb;

public class Response {
    private String message;
    private String detail;
    private ListObject data;

    public ListObject getData() {
        return data;
    }

    public void setData(ListObject data) {
        this.data = data;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }


}
