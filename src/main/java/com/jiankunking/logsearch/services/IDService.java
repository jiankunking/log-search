package com.jiankunking.logsearch.services;

import org.springframework.stereotype.Service;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Service
public class IDService {

    /**
     * 获取filebeat yml 存储consul id
     *
     * @param project
     * @param ip
     * @return
     */
    public String getFileBeatYmlID(String project, String ip) {
        return String.format("filebeat/%s/yml/%s", project, ip);
    }

    /**
     * 获取filebeat 存储consul id
     *
     * @param project
     * @param ip
     * @return
     */
    public String getFileBeatExtID(String project, String ip) {
        return String.format("filebeat/%s/ext/%s", project, ip);
    }

    /**
     * 获取filebeat ext 存储前缀
     *
     * @param project
     * @return
     */
    public String getFileBeatExtPrefix(String project) {
        return String.format("filebeat/%s/ext", project);
    }
}
