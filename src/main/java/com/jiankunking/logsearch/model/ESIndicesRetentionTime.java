package com.jiankunking.logsearch.model;

/**
 * @author jiankunking.
 * @date：2019/01/11 18:22
 * @description:
 */
public class ESIndicesRetentionTime {
    /**
     * ES集群名称
     */
    private String cluster;
    /**
     * 索引保留时间
     */
    private int retentionTime;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public int getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(int retentionTime) {
        this.retentionTime = retentionTime;
    }


}
