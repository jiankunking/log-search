package com.jiankunking.logsearch.services;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.FileBeatConfig;
import com.jiankunking.logsearch.model.InitializedStatusEnum;
import com.jiankunking.logsearch.storage.ConsulStorage;
import com.jiankunking.logsearch.util.HttpUtils;
import com.jiankunking.logsearch.util.MapUtils;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author jiankunking.
 * @date：2018/9/10 11:25
 * @description: 暂时没有使用
 */
@Slf4j
@Service
public class AgentBatchDeployService {

    @Autowired
    AgentDeployService agentDeployService;
    @Autowired
    HttpUtils httpUtils;
    @Autowired
    YmlService ymlService;
    @Autowired
    IDService idService;
    @Autowired
    ConsulStorage consulStorage;

    /**
     * 单次批量最大数量
     */
    private int batchMaxSize = 100;
    /**
     * 线程池最大数量
     */
    private int threadPoolMaxSize = 10;


    public boolean batchUpdateFileBeatOptions(String project, FileBeatConfig fileBeatConfig, HttpServletResponse response) throws IOException, InterruptedException {
        if (fileBeatConfig.getIps() == null || fileBeatConfig.getIps().size() == 0) {
            throw new IllegalArgumentException(" ips has to have a value!");
        }
        if (fileBeatConfig.getIps().size() > batchMaxSize) {
            throw new IllegalArgumentException(" The number of ips must be less than 100!");
        }
        int poolSize;
        int ipCount = fileBeatConfig.getIps().size();
        if (ipCount < threadPoolMaxSize) {
            poolSize = ipCount;
        } else {
            poolSize = threadPoolMaxSize;
        }
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();
        ConcurrentMap<String, String> statesMap = new ConcurrentHashMap<>(MapUtils.getSize(ipCount));
        ThreadPoolUtils threadPoolUtils = new ThreadPoolUtils(poolSize);

        for (int i = 0; i < ipCount; i++) {
            String ip = fileBeatConfig.getIps().get(i);
            threadPoolUtils.submit(() -> {
                try {
                    //深拷贝
                    String json = JSON.toJSONString(fileBeatConfig);
                    FileBeatConfig fileBeatConfigTemp = JSON.parseObject(json, FileBeatConfig.class);

                    this.updateFileBeatOptions(project, ip, fileBeatConfigTemp, msgQueue, statesMap);
                } catch (Exception e) {
                    log.error("thread error:", e);
                }
            });
        }
        String line;
        //线程池 流合并
        while (true) {
            //获取数据 等待60s
            //超时会返回null
            line = msgQueue.poll(60, TimeUnit.SECONDS);
            if (line == null) {
                response.getWriter().close();
                return true;
            }
            response.getWriter().println(line);
            response.getWriter().flush();
            log.info(line);
            if (statesMap.size() == ipCount) {
                response.getWriter().close();
                return true;
            }
        }
    }

    public boolean batchUninstallFileBeatOptions(String project, List<String> ips, HttpServletResponse response) {
        if (ips == null || ips.size() == 0) {
            throw new IllegalArgumentException(" ips has to have a value!");
        }
        int ipCount = ips.size();
        if (ipCount > batchMaxSize) {
            throw new IllegalArgumentException(" The number of ips must be less than 100!");
        }
        int poolSize;
        if (ipCount < threadPoolMaxSize) {
            poolSize = ipCount;
        } else {
            poolSize = threadPoolMaxSize;
        }
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();
        int initialCapacity = MapUtils.getSize(ipCount);
        ConcurrentMap<String, String> statesMap = new ConcurrentHashMap<>(initialCapacity);
        ThreadPoolUtils threadPoolUtils = new ThreadPoolUtils(poolSize);
        return true;
    }

    /**
     * filebeat 安装流处理
     *
     * @param ip
     * @param fileBeatConfig
     * @param msgQueue
     * @param statesMap
     * @return
     * @throws IOException
     */
    private boolean installStreamProcess(String ip, FileBeatConfig fileBeatConfig, BlockingQueue<String> msgQueue, ConcurrentMap<String, String> statesMap) throws IOException {
        String url = agentDeployService.getInstallFilebeatUrl();
        log.info(url);
        BufferedReader bufferedReader = null;
        okhttp3.Response ansibleResponse = null;
        try {
            OkHttpClient okHttpClient = (new OkHttpClient()).newBuilder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES).build();
            String json = agentDeployService.getFileBeatInstallParameters(fileBeatConfig);
            ansibleResponse = httpUtils.post(url, json, okHttpClient);
            InputStreamReader inputStreamReader = new InputStreamReader(ansibleResponse.body().byteStream(), GlobalConfig.CHARSET);

            bufferedReader = new BufferedReader(inputStreamReader);

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
                        statesMap.put(ip, "ok");
                    } else if ("fatal".equals(status.toString().toLowerCase())) {
                        fileBeatConfig.setInitializedStatus(InitializedStatusEnum.fail);
                        statesMap.put(ip, "fatal");
                    } else if ("invalid".equals(status.toString().toLowerCase())) {
                        fileBeatConfig.setInitializedStatus(InitializedStatusEnum.invalid);
                        statesMap.put(ip, "invalid");
                    }
                    state = agentDeployService.update(fileBeatConfig);
                    if (!state) {
                        throw new InternalError(" filebeat config save failed");
                    }
                }
                msgQueue.add(line);
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

    private boolean updateFileBeatOptions(String project, String ip, FileBeatConfig fileBeatConfig, BlockingQueue<String> msgQueue, ConcurrentMap<String, String> statesMap) throws IOException {
        //校验
        if (StringUtils.isEmpty(project)) {
            throw new IllegalArgumentException(" project has to have a value!");
        }
        if (StringUtils.isEmpty(ip)) {
            throw new IllegalArgumentException(" ip has to have a value!");
        }
        if (fileBeatConfig.getEsHosts() == null || fileBeatConfig.getEsHosts().size() == 0) {
            throw new IllegalArgumentException(" esHosts has to have a value!");
        }
        if (fileBeatConfig.getFileBeatTypeConfig() == null) {
            throw new IllegalArgumentException(" fileBeatTypeConfig has to have a value!");
        }
        if (StringUtils.isEmpty(fileBeatConfig.getVersion())) {
            throw new IllegalArgumentException(" version has to have a value!");
        }

        //强制添加 host id信息
        fileBeatConfig.setAddHostMetadata(true);

        fileBeatConfig.setProject(project);
        fileBeatConfig.setIps(Arrays.asList(ip));
        fileBeatConfig.setYmlID(idService.getFileBeatYmlID(project, ip));
        fileBeatConfig.setExtID(idService.getFileBeatExtID(project, ip));
        //创建yml
        String yml = ymlService.toYml(fileBeatConfig, ip);
        //保存yml到consul中
        if (!consulStorage.save(fileBeatConfig.getYmlID(), yml)) {
            throw new InternalError(" filebeat yml save failed");
        }

        //流处理
        this.installStreamProcess(ip, fileBeatConfig, msgQueue, statesMap);
        return true;
    }
}
