package com.jiankunking.logsearch.config;


import com.jiankunking.logsearch.model.LocationEnum;
import org.apache.http.HttpHost;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class EnvionmentVariables {
    ///**
    // * 10.238.116.108:9200,10.238.116.109:9200,10.238.116.110:9200
    // */
    //public static String ES_URL;
    /**
     * consul host
     */
    public static String CONSUL_HOST;
    /**
     * consul port
     */
    public static int CONSUL_PORT = 8500;

    /**
     * 索引前缀
     */
    //public static String INDEX_PREFIX = "filebeat-*";

    /**
     * ansible 拓展 接口
     */
    public static String ANSIBLE_EXT_URL;

    /**
     * filebeat 版本
     */
    public static String FILEBEAT_VERSION;

    /**
     * 应用监听端口
     */
    public static int LISTEN_PORT = 8080;
    /**
     * 日志批量下载限制
     */
    public static int ON_LINE_DOWNLOAD_SIZE = 10 * 10000;
    /**
     * filebeat 下载地址   "10.138.16.192:8081"
     */
    public static String FILEBEAT_DOWNLOAD_URL;

    /**
     * 离线下载日志默认路径
     */
    public static String OFF_LINE_LOG_DOWNLOAD_PATH = "/data/offline/logs/";

    /**
     * 是否启用离线下载
     * 启用的话 会启用线程池 线程数 = CPU核心数/(1-阻塞系数)
     * 此处阻塞系数取为 0.2
     */
    public static boolean ENABLE_OFFLINE_DOWNLOAD_FUNCTION = false;

    /**
     * 离线下载服务器地址
     */
    public static String OFFLINE_DOWNLOAD_SERVER;
    public static int OFFLINE_DOWNLOAD_SERVER_PORT = 80;

    /**
     * es-ext 部署机房
     */
    public static LocationEnum ES_EXT_DEPLOY_LOCATION = LocationEnum.QD;

    /**
     * 安装filebeat playbook版本
     */
    public static final String INSTALL_FILEBEAT_PLAYBOOK_VERSION = "v0.0.1";
    /**
     * 卸载filebeat playbook版本
     */
    public static final String UNINSTALL_FILEBEAT_PLAYBOOK_VERSION = "v0.0.1";


    /**
     * 按照约定格式解析字符串
     * 格式：10.238.116.108:9200,10.238.116.109:9200,10.238.116.110:9200
     *
     * @return
     */
    public static HttpHost[] getHttpHostArray(String address) {
        String[] hosts = address.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String[] arr = hosts[i].split(":");
            httpHosts[i] = new HttpHost(arr[0], Integer.parseInt(arr[1]), "http");
        }
        return httpHosts;
    }
}

