package com.jiankunking.logsearch.model.k8s;


import java.util.List;

public class Cluster {
    private String cluster;
    private List<String> namespaces;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }
}

