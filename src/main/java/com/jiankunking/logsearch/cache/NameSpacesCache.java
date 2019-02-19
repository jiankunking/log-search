package com.jiankunking.logsearch.cache;

import com.jiankunking.logsearch.cache.k8s.KubernetesTenants;
import com.jiankunking.logsearch.model.k8s.Cluster;
import com.jiankunking.logsearch.model.k8s.Kubernetes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/10/11 14:01
 * @description:
 */
@Slf4j
@Component
public class NameSpacesCache {


    @Autowired
    KubernetesTenants kubernetesTenants;


    private static HashMap<String, Kubernetes> tenants = new HashMap<>();

    /**
     * 每小时执行一次
     * 测试每五秒执行一次：0/5 * * * * ?
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void init() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("NameSpacesCache init:" + df.format(new Date()));
        tenants = kubernetesTenants.getTenants();
        System.out.println("NameSpacesCache init finish:" + df.format(new Date()));
    }

    public List<Cluster> getClusters(String project) {
        if (!tenants.containsKey(project)) {
            return null;
        }
        List<Cluster> clusterList = new ArrayList<>();
        Kubernetes kubernetes = tenants.get(project);
        for (String clusterName : kubernetes.getClusters().keySet()) {
            Cluster cluster = new Cluster();
            cluster.setCluster(clusterName);
            List<String> namespaces = new ArrayList<>();
            for (String namespace : kubernetes.getClusters().get(clusterName)) {
                namespaces.add(namespace.toLowerCase());
            }
            cluster.setNamespaces(namespaces);
            clusterList.add(cluster);
        }

        return clusterList;
    }
}
