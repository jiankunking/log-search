package com.jiankunking.logsearch.dto;


import com.jiankunking.logsearch.model.LocationEnum;

/**
 * @author jiankunking.
 * @date：2018/9/25 14:06
 * @description:
 */
public class ESCluster {
    private String id;
    /**
     * es cluster 名字
     */
    private String name;
    private String project;
    /**
     * es cluster 地址
     */
    private String address;
    private String description;
    /**
     * es-ext部署的位置 青岛机房还是北京机房（因为两地网络不通，北京机房的只加载并初始化北京的es client即可）
     */
    private LocationEnum location;

    public LocationEnum getLocation() {
        return location;
    }

    public void setLocation(LocationEnum location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
