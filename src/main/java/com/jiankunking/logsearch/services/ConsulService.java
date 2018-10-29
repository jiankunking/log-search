package com.jiankunking.logsearch.services;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.dto.ESCluster;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.model.LocationEnum;
import com.jiankunking.logsearch.storage.ConsulStorage;
import com.jiankunking.logsearch.util.Base64Utils;
import com.orbitz.consul.model.kv.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jiankunking.logsearch.config.GlobalConfig.ES_CLUSTER_PREFIX;


/**
 * @author jiankunking.
 * @date：2018/9/28 13:41
 * @description:
 */
@Slf4j
@Component
public class ConsulService {

    /**
     * 获取部署在某个机房的es信息
     *
     * @param locationEnum
     * @return
     * @throws UnsupportedEncodingException
     */
    public List<ESCluster> getAllESClusters(LocationEnum locationEnum) throws UnsupportedEncodingException {
        ConsulStorage consulStorage = new ConsulStorage();
        List<Value> list = consulStorage.getByKeyPrefix(ES_CLUSTER_PREFIX);
        List<ESCluster> result = new ArrayList<>();
        for (Value item : list) {
            if (!item.getValue().isPresent()) {
                continue;
            }
            ESCluster esCluster = JSON.parseObject(Base64Utils.decoder(item.getValue().get()), com.jiankunking.logsearch.dto.ESCluster.class);
            if (esCluster.getLocation() != locationEnum) {
                continue;
            }
            result.add(esCluster);
        }
        return result;
    }

    /***
     * 获取某个项目下所有的es信息
     * @param project
     * @return
     * @throws UnsupportedEncodingException
     */
    public List<ESCluster> getESClusters(String project) throws UnsupportedEncodingException {
        String prefix = ES_CLUSTER_PREFIX + project + "/";
        ConsulStorage consulStorage = new ConsulStorage();
        List<Value> list = consulStorage.getByKeyPrefix(prefix);
        List<ESCluster> result = new ArrayList<>();
        for (Value item : list) {
            if (!item.getValue().isPresent()) {
                continue;
            }
            ESCluster esCluster = JSON.parseObject(Base64Utils.decoder(item.getValue().get()), com.jiankunking.logsearch.
                    dto.ESCluster.class);
            result.add(esCluster);
        }
        return result;
    }

    /**
     * 获取某个项目部署在某个机房的es信息
     *
     * @param project
     * @param locationEnum
     * @return
     * @throws UnsupportedEncodingException
     */
    public List<ESCluster> getESClustersByLocation(String project, LocationEnum locationEnum) throws UnsupportedEncodingException {
        String prefix = ES_CLUSTER_PREFIX + project + "/";
        ConsulStorage consulStorage = new ConsulStorage();
        List<Value> list = consulStorage.getByKeyPrefix(prefix);
        List<ESCluster> result = new ArrayList<>();
        for (Value item : list) {
            if (!item.getValue().isPresent()) {
                continue;
            }
            ESCluster esCluster = JSON.parseObject(Base64Utils.decoder(item.getValue().get()), com.jiankunking.logsearch.dto.ESCluster.class);
            if (esCluster.getLocation() != locationEnum) {
                continue;
            }
            result.add(esCluster);
        }
        return result;
    }

    /**
     * 获取某个项目下某个es cluster的信息
     *
     * @param project
     * @param cluster
     * @return
     * @throws UnsupportedEncodingException
     * @throws ESClusterNotFoundException
     */
    public ESCluster getESCluster(String project, String cluster) throws UnsupportedEncodingException, ESClusterNotFoundException {
        String prefix = ES_CLUSTER_PREFIX + project + "/" + cluster + "/";
        ConsulStorage consulStorage = new ConsulStorage();
        Optional<Value> valueOptional = consulStorage.getByID(prefix);
        if (valueOptional.isPresent()) {
            return JSON.parseObject(Base64Utils.decoder(valueOptional.get().toString()), com.jiankunking.logsearch.dto.ESCluster.class);
        }
        throw new ESClusterNotFoundException("The cluster configuration for the projext not found in the consul：" + cluster);
    }
}
