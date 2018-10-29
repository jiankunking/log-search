package com.jiankunking.logsearch.client;


import com.jiankunking.logsearch.cache.ESClientLRUCache;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.dto.ESCluster;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.services.ConsulService;
import com.jiankunking.logsearch.util.MapUtils;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class ESClients {

    private static ConsulService consulService = new ConsulService();
    /**
     * 超时时间设为2分钟
     */
    private static int Maximum_Timeout = 2 * 60 * 1000;

    private ESClients() {
    }

    public static RestClient getInstance(String project, String cluster) throws UnsupportedEncodingException, ESClusterNotFoundException, ESClientNotFoundException {
        ConcurrentMap<String, RestClient> projectClients = ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.get(project);
        if (projectClients == null) {
            synchronized (ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache) {
                projectClients = ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.get(project);
                if (projectClients == null) {
                    List<ESCluster> esClusters = consulService.getESClustersByLocation(project, EnvionmentVariables.ES_EXT_DEPLOY_LOCATION);
                    if (esClusters == null || esClusters.size() == 0) {
                        throw new ESClusterNotFoundException("The cluster configuration for the projext not found in the consul");
                    }
                    //检验是否有对应集群的es
                    boolean exist = false;
                    for (ESCluster esCluster : esClusters) {
                        if (cluster.equals(esCluster.getName())) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        throw new ESClusterNotFoundException("the es cluster:" + cluster + " cluster configuration was not found in the consul");
                    }
                    //初始化对应client
                    initProjectESClients(project, esClusters);
                    projectClients = ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.get(project);
                }
            }
        }
        for (Map.Entry<String, RestClient> clientEntry : projectClients.entrySet()) {
            if (clientEntry.getKey().equals(cluster)) {
                return clientEntry.getValue();
            }
        }
        //走到这里说明 某个项目下 找不到对应的集群
        synchronized (ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache) {
            boolean exist = false;
            for (Map.Entry<String, RestClient> clientEntry : projectClients.entrySet()) {
                if (clientEntry.getKey().equals(cluster)) {
                    exist = true;
                }
            }
            if (!exist) {
                //补充项目下某个集群client
                initESClient(project, cluster);
            }
        }
        for (Map.Entry<String, RestClient> clientEntry : projectClients.entrySet()) {
            if (clientEntry.getKey().equals(cluster)) {
                return clientEntry.getValue();
            }
        }
        throw new ESClientNotFoundException();
    }

    public static void initAllESClients() throws UnsupportedEncodingException {
        List<ESCluster> esClusters = consulService.getAllESClusters(EnvionmentVariables.ES_EXT_DEPLOY_LOCATION);
        for (ESCluster esCluster : esClusters) {
            RestClient restClient = RestClient
                    .builder(EnvionmentVariables.getHttpHostArray(esCluster.getAddress()))
                    .setMaxRetryTimeoutMillis(Maximum_Timeout)
                    .build();
            if (ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.get(esCluster.getProject()) != null) {
                ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.get(esCluster.getProject()).put(esCluster.getName(), restClient);
            } else {
                ConcurrentMap<String, RestClient> items = new ConcurrentHashMap<>(5);
                items.put(esCluster.getName(), restClient);
                ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.put(esCluster.getProject(), items);
            }
        }
    }

    private static void initProjectESClients(String project, List<ESCluster> esClusters) {
        int size = MapUtils.getSize(esClusters.size());
        ConcurrentMap<String, RestClient> items = new ConcurrentHashMap<>(size);
        for (ESCluster esCluster : esClusters) {
            RestClient restClient = RestClient
                    .builder(EnvionmentVariables.getHttpHostArray(esCluster.getAddress()))
                    .setMaxRetryTimeoutMillis(Maximum_Timeout)
                    .build();
            items.put(esCluster.getName(), restClient);
        }
        ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.put(project, items);
    }

    private static void initESClient(String project, String cluster) throws UnsupportedEncodingException, ESClusterNotFoundException {
        ESCluster esCluster = consulService.getESCluster(project, cluster);
        RestClient restClient = RestClient
                .builder(EnvionmentVariables.getHttpHostArray(esCluster.getAddress()))
                .setMaxRetryTimeoutMillis(Maximum_Timeout)
                .build();
        ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.get(project).put(cluster, restClient);
    }

    public static void closeESClients() throws IOException {
        if (ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache != null) {
            for (ConcurrentMap<String, RestClient> value : ESClientLRUCacheSingleton.INSTANCE.esClientLRUCache.values()) {
                for (RestClient client : value.values()) {
                    if (client != null) {
                        client.close();
                    }
                }
            }
        }
    }

    /**
     * es client lru 单例
     */
    private enum ESClientLRUCacheSingleton {
        INSTANCE;
        /**
         * cache 结构：
         * project
         * ----------cluster client
         * <p>
         * 每个es client 内部链接是有限制的 所以采取某个项目、某个集群分配client
         * 而不是单纯以集群的维度
         */
        private ESClientLRUCache<String, ConcurrentMap<String, RestClient>> esClientLRUCache;

        ESClientLRUCacheSingleton() {
            esClientLRUCache = new ESClientLRUCache<>(20);
        }

        public ESClientLRUCache<String, ConcurrentMap<String, RestClient>> getInstance() {
            return esClientLRUCache;
        }
    }
}
