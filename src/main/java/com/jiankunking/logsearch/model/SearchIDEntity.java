package com.jiankunking.logsearch.model;

import lombok.Data;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Data
public class SearchIDEntity {
    private String docID;

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public long getTimeStampSort() {
        return timeStampSort;
    }

    public void setTimeStampSort(long timeStampSort) {
        this.timeStampSort = timeStampSort;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    private long timeStampSort;
    private int offset;
}
