package com.jiankunking.logsearch.cache.k8s;


import com.jiankunking.logsearch.model.k8s.Kubernetes;

import java.util.HashMap;

public interface IKubernetesTenants {
    HashMap<String, Kubernetes> getTenants();
}
