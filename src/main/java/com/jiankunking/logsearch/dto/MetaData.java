package com.jiankunking.logsearch.dto;

import lombok.Data;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Data
public class MetaData {
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    private int total;
}
