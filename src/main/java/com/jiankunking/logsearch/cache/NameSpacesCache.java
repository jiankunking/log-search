package com.jiankunking.logsearch.cache;

import com.jiankunking.logsearch.util.ResourceRenderer;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
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

    /**
     * tenant
     * ------ namespace
     */
    private static HashMap<String, List<String>> tenants = new HashMap<>();
    private ApiClient client = null;
    private CoreV1Api api = null;


    /**
     * 每小时执行一次
     * 测试每五秒执行一次：0/5 * * * * ?
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void init() throws IOException, ApiException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("NameSpacesCache init:" + df.format(new Date()));
        if (api == null) {
            String fileName = "classpath:/k8s/kubectl.kubeconfig";
            InputStream inputStream = ResourceRenderer.resourceLoader(fileName);
            client = Config.fromConfig(inputStream);
            // 五分钟
            client.setConnectTimeout(5 * 60 * 1000);
            Configuration.setDefaultApiClient(client);
            api = new CoreV1Api();
        }
        V1NamespaceList v1NamespaceList = api.listNamespace(null, null, null,
                null, null, 1000,
                null, null, Boolean.FALSE);
        HashMap<String, List<String>> tempTenants = new HashMap<>(50);
        String clusterName, namespace;
        String key = "tenant.tenant.test.io";
        for (V1Namespace item : v1NamespaceList.getItems()) {
            if (item.getMetadata() == null || item.getMetadata().getLabels() == null) {
                continue;
            }
            if (!item.getMetadata().getLabels().containsKey(key)) {
                continue;
            }
            clusterName = item.getMetadata().getLabels().get(key);
            namespace = item.getMetadata().getName();
            if (tempTenants.containsKey(clusterName)) {
                tempTenants.get(clusterName).add(namespace);
            } else {
                List<String> nameSpaces = new ArrayList<>();
                nameSpaces.add(namespace);
                tempTenants.put(clusterName, nameSpaces);
            }
        }
        tenants = tempTenants;
    }

    public List<String> getNameSpaces(String project) {
        return tenants.get(project);
    }
}
