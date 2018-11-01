package com.jiankunking.logsearch.model.external.cmdb;

import com.jiankunking.logsearch.model.k8s.Kubernetes;

public class Spec {
    private String org;
    private String name_cn;
    private String icon;
    private String sCode;
    private String almCode;
    private String gitAddr;
    private Kubernetes k8s;
    private Object status;

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getName_cn() {
        return name_cn;
    }

    public void setName_cn(String name_cn) {
        this.name_cn = name_cn;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getsCode() {
        return sCode;
    }

    public void setsCode(String sCode) {
        this.sCode = sCode;
    }

    public String getAlmCode() {
        return almCode;
    }

    public void setAlmCode(String almCode) {
        this.almCode = almCode;
    }

    public String getGitAddr() {
        return gitAddr;
    }

    public void setGitAddr(String gitAddr) {
        this.gitAddr = gitAddr;
    }

    public Kubernetes getK8s() {
        return k8s;
    }

    public void setK8s(Kubernetes k8s) {
        this.k8s = k8s;
    }

    public Object getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status;
    }
}
