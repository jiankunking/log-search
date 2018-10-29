package com.jiankunking.logsearch.dto;

/**
 * @author jiankunking.
 * @date：2018/8/28 10:33
 * @description:
 */
public class VersionEntity {
    private String latestVersion;
    private String agentName;

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
}
