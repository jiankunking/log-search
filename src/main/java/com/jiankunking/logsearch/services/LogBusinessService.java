package com.jiankunking.logsearch.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.exception.ESClustersResponseTimeoutException;
import com.jiankunking.logsearch.util.ESQueryUtils;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@Service
public class LogBusinessService {

    @Autowired
    ESFilterService esFilterService;
    @Autowired
    IndexPrefixService indexPrefixService;

    /**
     * 查询某段时间内 应用名称
     *
     * @param cluster
     * @param project
     * @param start
     * @param end
     * @param size
     * @return
     * @throws IOException
     * @throws ESClusterNotFoundException
     * @throws ESClientNotFoundException
     */
    public ArrayList<String> getAppList(String cluster, String project, long start, long end, int size) throws IOException, ESClusterNotFoundException, ESClientNotFoundException, ESClustersResponseTimeoutException {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.fetchSource("fields", null);

        CollapseBuilder collapseBuilder = new CollapseBuilder("fields.app");
        sourceBuilder.collapse(collapseBuilder);

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp")
                .gte(TimeUtils.toDate(start))
                .lte(TimeUtils.toDate(end));
        //sourceBuilder.query(rangeQueryBuilder);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        esFilterService.addProjectFilter(project, boolQueryBuilder);

        boolQueryBuilder.must(rangeQueryBuilder);
        sourceBuilder.size(size).query(boolQueryBuilder);
        String queryJson = sourceBuilder.toString();
        //log.info(queryJson);

        String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(indexPrefixService.getIndexPrefix(cluster, start, end)), queryJson);

        //遍历获取 app
        Map responseMap = (Map) JSON.parse(resultJson);
        ArrayList<String> apps = new ArrayList();
        JSONArray jsonArray = (JSONArray) (((Map) responseMap.get("hits")).get("hits"));
        JSONObject itemJsonObj;
        JSONArray appArray;
        for (Object item : jsonArray) {
            itemJsonObj = (JSONObject) item;
            if (!itemJsonObj.containsKey("fields") || !((Map) itemJsonObj.get("fields")).containsKey("fields.app")) {
                continue;
            }
            appArray = (JSONArray) ((Map) itemJsonObj.get("fields")).get("fields.app");
            for (Object app : appArray) {
                apps.add(String.valueOf(app));
            }
        }
        return apps;
    }

    /**
     * 获取实例名称
     *
     * @param cluster
     * @param project
     * @param start
     * @param end
     * @param size
     * @param app
     * @return
     * @throws IOException
     * @throws ESClusterNotFoundException
     * @throws ESClientNotFoundException
     */
    public ArrayList<String> getInstances(String cluster, String project, long start, long end, int size, String app) throws IOException, ESClusterNotFoundException, ESClientNotFoundException, ESClustersResponseTimeoutException {
        ArrayList<String> instances = new ArrayList();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.fetchSource("fields", null);

        CollapseBuilder collapseBuilder = new CollapseBuilder("fields.instance");
        sourceBuilder.collapse(collapseBuilder);

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp")
                .gte(TimeUtils.toDate(start))
                .lte(TimeUtils.toDate(end));
        sourceBuilder.query(rangeQueryBuilder);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        esFilterService.addProjectFilter(project, boolQueryBuilder);
        if (!StringUtils.isEmpty(app) && !app.equals(GlobalConfig.ALL_APPS)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("fields.app", app));
        }
        boolQueryBuilder.must(rangeQueryBuilder);
        sourceBuilder.size(size).query(boolQueryBuilder);
        String queryJson = sourceBuilder.size(size).toString();
        //log.info(queryJson);

        String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(indexPrefixService.getIndexPrefix(cluster, start, end)), queryJson);

        //遍历获取 app
        Map responseMap = (Map) JSON.parse(resultJson);
        //log.info(resultJson);
        JSONArray jsonArray = (JSONArray) (((Map) responseMap.get("hits")).get("hits"));
        JSONObject itemJsonObj;
        JSONArray instanceArray;
        for (Object item : jsonArray) {
            itemJsonObj = (JSONObject) item;
            if (!itemJsonObj.containsKey("fields") || !((Map) itemJsonObj.get("fields")).containsKey("fields.instance")) {
                continue;
            }
            instanceArray = (JSONArray) ((Map) itemJsonObj.get("fields")).get("fields.instance");
            for (Object instance : instanceArray) {
                instances.add(String.valueOf(instance));
            }
        }
        return instances;
    }
}
