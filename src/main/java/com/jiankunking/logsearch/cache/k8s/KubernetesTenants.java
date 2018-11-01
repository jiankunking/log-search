package com.jiankunking.logsearch.cache.k8s;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.model.external.cmdb.Project;
import com.jiankunking.logsearch.model.external.cmdb.Response;
import com.jiankunking.logsearch.model.k8s.Kubernetes;
import com.jiankunking.logsearch.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
@Component
public class KubernetesTenants implements IKubernetesTenants {

    @Autowired
    HttpUtils httpUtils;

    @Override
    public HashMap<String, Kubernetes> getTenants() {
        HashMap<String, Kubernetes> tenants = new HashMap<>();
        String url = "http://" + EnvionmentVariables.CMDB_URL + "/api/v1/projects";
        okhttp3.Response response = null;
        try {
            response = httpUtils.get(url, null);
            if (response == null || response.code() != HttpServletResponse.SC_OK) {
                return tenants;
            }
            Response cmdbResponse = JSON.parseObject(response.body().string(), Response.class);
            if (cmdbResponse == null || cmdbResponse.getData() == null ||
                    cmdbResponse.getData().getItems() == null || cmdbResponse.getData().getItems().size() == 0) {
                return tenants;
            }
            for (Project project : cmdbResponse.getData().getItems()) {
                if (project.getSpec().getK8s() == null) {
                    continue;
                }
                tenants.put(project.getSpec().getAlmCode().toLowerCase(), project.getSpec().getK8s());
            }
        } catch (IOException e) {
            log.error("getTenants:", e);
        }
        return tenants;
    }

}
