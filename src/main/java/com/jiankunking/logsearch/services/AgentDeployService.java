package com.jiankunking.logsearch.services;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.FileBeatConfig;
import com.jiankunking.logsearch.model.AnsibleExtParameters;
import com.jiankunking.logsearch.model.InitializedStatusEnum;
import com.jiankunking.logsearch.storage.ConsulStorage;
import com.jiankunking.logsearch.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@Component
public class AgentDeployService {

    @Autowired
    ConsulStorage consulStorage;
    @Autowired
    HttpUtils httpUtils;

    /**
     * 获取 安装 调用 ansible api参数
     *
     * @return
     */
    public String getFileBeatInstallParameters(FileBeatConfig fileBeatConfig) {

        AnsibleExtParameters ansibleExtParameters = new AnsibleExtParameters();
        //parms
        Map<String, String> params = new HashMap<>(9);
        // id 斜杠替换为下划线
        params.put("id", fileBeatConfig.getYmlID().replace("/", "_"));
        params.put("kv_id", fileBeatConfig.getYmlID());
        params.put("version", fileBeatConfig.getVersion());
        params.put("consul", EnvionmentVariables.CONSUL_HOST + ":" + EnvionmentVariables.CONSUL_PORT);
        params.put("download_url", EnvionmentVariables.FILEBEAT_DOWNLOAD_URL);
        params.put("project", fileBeatConfig.getProject());
        params.put("type", fileBeatConfig.getFileBeatTypeConfig().getType().name());
        ansibleExtParameters.setParams(params);

        ansibleExtParameters.setHosts(fileBeatConfig.getIps());
        String json = JSON.toJSONString(ansibleExtParameters, true);
        log.info(json);
        return json;
    }


    /**
     * 获取 卸载 调用 ansible api参数
     *
     * @return
     */
    public String getFileBeatUninstallParameters(String ymlID, String version, String ip) {

        AnsibleExtParameters ansibleExtParameters = new AnsibleExtParameters();
        //parms
        Map<String, String> params = new HashMap<>(3);
        params.put("id", ymlID);
        params.put("version", version);
        ansibleExtParameters.setParams(params);

        //host
        List<String> ips = new ArrayList<String>() {{
            add(ip);
        }};
        ansibleExtParameters.setHosts(ips);
        String json = JSON.toJSONString(ansibleExtParameters, true);
        log.info(json);
        return json;
    }

    /**
     * 保存filebeat 配置信息到consul
     *
     * @param fileBeatConfig
     * @return
     */
    public boolean update(FileBeatConfig fileBeatConfig) {
        return consulStorage.save(fileBeatConfig.getExtID(), JSON.toJSONString(fileBeatConfig));
    }

    /**
     * filebeat 安装流处理
     *
     * @param fileBeatConfig
     * @param response
     * @return
     * @throws IOException
     */
    public boolean installStreamProcess(FileBeatConfig fileBeatConfig, HttpServletResponse response) throws IOException {
        String url = getInstallFilebeatUrl();
        log.info(url);
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        okhttp3.Response ansibleResponse = null;
        try {
            printWriter = response.getWriter();
            OkHttpClient okHttpClient = (new OkHttpClient()).newBuilder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES).build();
            String json = this.getFileBeatInstallParameters(fileBeatConfig);
            ansibleResponse = httpUtils.post(url, json, okHttpClient);
            InputStreamReader inputStreamReader = new InputStreamReader(ansibleResponse.body().byteStream(), GlobalConfig.CHARSET);

            bufferedReader = new BufferedReader(inputStreamReader);
            response.setCharacterEncoding(GlobalConfig.CHARSET);

            String line;
            Map result;
            Object status;
            String type;
            Boolean state;
            while ((line = bufferedReader.readLine()) != null) {
                result = (Map) JSON.parseObject(line).get("result");
                status = result.get("status");
                type = String.valueOf(result.get("type"));
                if (status != null && "recap".equals(type.toLowerCase())) {
                    if ("ok".equals(status.toString().toLowerCase())) {
                        fileBeatConfig.setInitializedStatus(InitializedStatusEnum.success);
                    } else if ("fatal".equals(status.toString().toLowerCase())) {
                        fileBeatConfig.setInitializedStatus(InitializedStatusEnum.fail);
                    } else if ("invalid".equals(status.toString().toLowerCase())) {
                        fileBeatConfig.setInitializedStatus(InitializedStatusEnum.invalid);
                    }
                    state = this.update(fileBeatConfig);
                    if (!state) {
                        throw new InternalError(" filebeat config save failed");
                    }
                }
                printWriter.println(line);
                printWriter.flush();
                log.info(line);
            }
            return true;
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (ansibleResponse != null) {
                ansibleResponse.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    /**
     * 卸载filebeat agent 流处理
     *
     * @param ymlID
     * @param version
     * @param ip
     * @param response
     * @return
     * @throws IOException
     */
    public boolean uninstallStreamProcess(String ymlID, String version, String ip, HttpServletResponse response) throws IOException {
        String url = getUninstallFilebeatUrl();
        log.info(url);
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        okhttp3.Response ansibleResponse = null;
        try {
            printWriter = response.getWriter();
            OkHttpClient okHttpClient = (new OkHttpClient()).newBuilder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES).build();
            String json = this.getFileBeatUninstallParameters(ymlID, version, ip);
            ansibleResponse = httpUtils.post(url, json, okHttpClient);
            InputStreamReader inputStreamReader = new InputStreamReader(ansibleResponse.body().byteStream(), GlobalConfig.CHARSET);

            bufferedReader = new BufferedReader(inputStreamReader);
            response.setCharacterEncoding(GlobalConfig.CHARSET);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                printWriter.println(line);
                printWriter.flush();
                log.info(line);
            }
            return true;
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (ansibleResponse != null) {
                ansibleResponse.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    /**
     * 获取安装ansible url
     *
     * @return
     */
    public String getInstallFilebeatUrl() {
        return "http://" + EnvionmentVariables.ANSIBLE_EXT_URL + "/api/v1/ansible/play/install_filebeat/" + EnvionmentVariables.INSTALL_FILEBEAT_PLAYBOOK_VERSION;
    }

    /**
     * 获取卸载ansible url
     *
     * @return
     */
    public String getUninstallFilebeatUrl() {
        return "http://" + EnvionmentVariables.ANSIBLE_EXT_URL + "/api/v1/ansible/play/uninstall_filebeat/" + EnvionmentVariables.UNINSTALL_FILEBEAT_PLAYBOOK_VERSION;
    }
}
