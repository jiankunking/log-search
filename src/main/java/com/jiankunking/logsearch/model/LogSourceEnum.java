package com.jiankunking.logsearch.model;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:日志类型
 */
public enum LogSourceEnum {
    /**
     * 文本日志
     */
    log("log", "文本日志"),
    /**
     * docker日志
     */
    docker("docker", "docker日志"),
    /**
     * kubernetes日志
     */
    k8s("k8s", "kubernetes日志");
    private String name;
    private String description;

    LogSourceEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
