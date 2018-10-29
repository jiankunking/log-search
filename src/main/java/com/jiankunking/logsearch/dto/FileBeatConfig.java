package com.jiankunking.logsearch.dto;


import com.jiankunking.logsearch.model.InitializedStatusEnum;
import com.jiankunking.logsearch.model.OSTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Data
public class FileBeatConfig {
    /**
     * 日志收集类型
     */
    private FileBeatTypeConfig fileBeatTypeConfig;
    /**
     * filebeat cpu 占用限额
     */
    private int maxProcs = 2;
    /**
     * es集群
     */
    private List<String> esHosts;

    /**
     * 项目简称
     */
    private String project;

    /**
     * yml 存储在consul中的id
     */
    private String ymlID;

    /**
     * 存储filebeat其他信息id (与上面id不一样的原因是为了根据前缀的过滤)
     */
    private String extID;
    /***
     * 是否添加 host信息(这个强制开启，以便获取host.id，在日志搜索上下文的时候使用host.id来明确属于哪台机器)
     */
    private boolean addHostMetadata = true;
    private boolean addNetInfo = false;
    /**
     * 批量
     */
    private List<String> ips;
    private String version;
    private String clusterLocation;
    private String agentVersion;
    private InitializedStatusEnum initializedStatus = InitializedStatusEnum.uninitialized;
    private long time = System.currentTimeMillis();
    private OSTypeEnum osType = OSTypeEnum.LINUX;
    /**
     * es Shard (es 默认值即为5)
     */
    private int numberOfShards = 5;


}
