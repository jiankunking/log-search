package com.jiankunking.logsearch.model.k8s;

import java.util.List;
import java.util.Map;

public class Kubernetes {
    private String tenant;
    private Map<String, List<String>> clusters;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Map<String, List<String>> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, List<String>> clusters) {
        this.clusters = clusters;
    }

}
