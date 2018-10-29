package com.jiankunking.logsearch.dto;


import com.jiankunking.logsearch.model.LogSourceEnum;
import lombok.Data;

import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Data
public class FileBeatTypeConfig {
    /**
     * 收集类型：docker、log文件、k8s
     */
    private LogSourceEnum type;
    /**
     * 是否多行合并
     */
    private boolean multiline = false;
    /**
     * 文本日志路径列表
     */
    private List<String> paths;
    /**
     * 多行合并正则表达式
     */
    private String multilinePattern;

    /**
     * k8s cluster
     */
    private String k8sCluster;
}
