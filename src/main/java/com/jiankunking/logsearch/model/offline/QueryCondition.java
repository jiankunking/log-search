package com.jiankunking.logsearch.model.offline;

/**
 * @author jiankunking.
 * @date：2018/9/29 11:19
 * @description:
 */
public class QueryCondition {
    private long fromTime;
    private long toTime;
    private String cluster;
    private String project;
    private String app;
    private String instance;
    private String keyword;

    /**
     * 只有上下文下载的时候 会有这个条件
     */
    private String source;
    private String hostID;
    private String docID;
    /**
     * 区分是关键字搜索下载 还是 上下文 下载
     */
    private LogDownLoadTypeEnum logDownLoadType;

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public LogDownLoadTypeEnum getLogDownLoadType() {
        return logDownLoadType;
    }

    public void setLogDownLoadType(LogDownLoadTypeEnum logDownLoadType) {
        this.logDownLoadType = logDownLoadType;
    }

    public long getFromTime() {
        return fromTime;
    }

    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public long getToTime() {
        return toTime;
    }

    public void setToTime(long toTime) {
        this.toTime = toTime;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
