package com.jiankunking.logsearch.model.offline;

/**
 * @author jiankunking.
 * @date：2018/9/29 10:37
 * @description: 保存离线日志的查询条件，登陆人 ，任务状态等信息
 */
public class OffLineLogMetaData {
    /**
     * 任务创建人
     */
    private String creator;
    /**
     * 任务创建时间
     */
    private long creatTime;

    /**
     * 日志下载状态
     */
    private DownLoadStatusEnum downLoadStatus;

    /**
     * 查询条件
     */
    private QueryCondition queryCondition;

    /**
     * 异常原因
     * 下载日志失败的时候 保存异常原因
     */
    private Object error;

    /**
     * 下载总条数
     */
    private int total;
    /**
     * 下载总耗时
     */
    private long downLoadCost = 0;
    /**
     * 压缩后zip文件大小
     */
    private long zipFileSize = 0;

    public long getZipFileSize() {
        return zipFileSize;
    }

    public void setZipFileSize(long zipFileSize) {
        this.zipFileSize = zipFileSize;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public long getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(long creatTime) {
        this.creatTime = creatTime;
    }

    public DownLoadStatusEnum getDownLoadStatus() {
        return downLoadStatus;
    }

    public void setDownLoadStatus(DownLoadStatusEnum downLoadStatus) {
        this.downLoadStatus = downLoadStatus;
    }

    public QueryCondition getQueryCondition() {
        return queryCondition;
    }

    public void setQueryCondition(QueryCondition queryCondition) {
        this.queryCondition = queryCondition;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public long getDownLoadCost() {
        return downLoadCost;
    }

    public void setDownLoadCost(long downLoadCost) {
        this.downLoadCost = downLoadCost;
    }
}
