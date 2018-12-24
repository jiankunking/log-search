package com.jiankunking.logsearch.services;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.SearchResult;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.exception.ESClustersResponseTimeoutException;
import com.jiankunking.logsearch.model.SearchIDEntity;
import com.jiankunking.logsearch.util.*;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.jiankunking.logsearch.config.EnvionmentVariables.DOWNLOAD_PAGE_SIZE;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@Service
public class LogDownloadService {

    @Autowired
    LogSearchService logSearchService;
    @Autowired
    ESFilterService esFilterService;
    @Autowired
    IndexPrefixService indexPrefixService;

    public void downloadByKeyWord(String cluster, String project, String keyWord, String app, String instance,
                                  String hostID, String source,
                                  long fromTime, long toTime, HttpServletResponse res) throws IOException, ESClusterNotFoundException, ESClientNotFoundException, ESClustersResponseTimeoutException {
        int total = this.getTotal(cluster, project, keyWord, app, instance, hostID, source, fromTime, toTime);

        String fileName = String.format("%s-%s-%s-%d-%d.txt", project, app, keyWord, fromTime, toTime);
        String downloadFielName = new String(fileName.getBytes("UTF-8"), "UTF-8");
        log.info("downloadFielName:" + downloadFielName);
        res.addHeader("Content-Disposition", "attachment;fileName=" + downloadFielName);
        res.setContentType("text/plain;charset=UTF-8");
        int length = 0;
        if (total == 0) {
            res.setHeader("Content-Length", String.valueOf(length));
            return;
        }
        if (total > EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE) {
            total = EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE;
            log.info("Maximum download allowed: " + EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE);
        }

        OutputStream outputStream = res.getOutputStream();
        int cycleIndex = IntUtils.getCycleCount(total, DOWNLOAD_PAGE_SIZE);
        log.info("cycleIndex: " + cycleIndex);
        Object[] searchAfterValues;
        SearchIDEntity searchIDEntity;
        int initialCapacity = MapUtils.getSize(total);
        Map<String, Object> item = new HashMap<>(initialCapacity);
        for (int i = 0; i < cycleIndex; i++) {
            if (i == 0) {
                item = downLoadByPage(cluster, project, keyWord, app, instance, hostID, source, 1000, fromTime, toTime, null, outputStream);
                length = length + (int) item.get("length");
                continue;
            }
            searchIDEntity = (SearchIDEntity) item.get("searchAfter");
            searchAfterValues = new Object[]{searchIDEntity.getTimeStampSort(), searchIDEntity.getOffset(), searchIDEntity.getDocID()};
            item = downLoadByPage(cluster, project, keyWord, app, instance, hostID, source, 1000, fromTime, toTime, searchAfterValues, outputStream);
            length = length + (int) item.get("length");
        }
        res.setHeader("Content-Length", String.valueOf(length));
        return;
    }

    private Map<String, Object> downLoadByPage(String cluster, String project, String keyWord, String app, String instance,
                                               String hostID, String source,
                                               int pageSize, long fromTime, long toTime,
                                               Object[] searchAfterValues, OutputStream outputStream) throws IOException, ESClusterNotFoundException, ESClientNotFoundException, ESClustersResponseTimeoutException {
        //识别是否是首次搜索
        boolean firstSearch = false;
        if (searchAfterValues == null) {
            firstSearch = true;
        } else {
            //根据上次结果搜索 结果中会带有上次的最后一条数据
            //所以加一 保持真正的pageSize
            pageSize = pageSize + 1;
        }
        SearchResult searchResult = logSearchService.queryStringByKeyWord(cluster, project, keyWord, app, instance, hostID, source, pageSize, fromTime, toTime, searchAfterValues, null);
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, Object> item;
        int initialCapacity = MapUtils.getSize(searchResult.getItems().size());
        Map<String, Object> result = new HashMap<>(initialCapacity);
        SearchIDEntity searchIDEntity = new SearchIDEntity();
        for (int i = 0; i < searchResult.getItems().size(); i++) {
            //迭代的时候 会有一条上次结果中的数据
            //所以 移除掉
            if (!firstSearch && i == 0) {
                continue;
            }
            item = searchResult.getItems().get(i);
            if (StringUtils.isEmpty(app) || app.equals(GlobalConfig.ALL_APPS)) {
                stringBuilder.append(item.get("app"));
                stringBuilder.append(GlobalConfig.SPACE);
            }
            if (StringUtils.isEmpty(instance) || instance.equals(GlobalConfig.ALL_INSTANCES)) {
                stringBuilder.append(item.get("instance"));
                stringBuilder.append(GlobalConfig.SPACE);
            }
            stringBuilder.append(item.get("message"));
            stringBuilder.append(GlobalConfig.LINE_BREAK);
            if (i + 1 == searchResult.getItems().size()) {
                searchIDEntity.setDocID(String.valueOf(item.get("id")));
                searchIDEntity.setOffset(Integer.valueOf(String.valueOf(item.get("offset"))));
                searchIDEntity.setTimeStampSort(ObjectUtils.objectToLong(item.get("time")));
            }
        }
        result.put("searchAfter", searchIDEntity);

        int bytesRead;
        int length = 0;
        byte[] buffer = new byte[1024];
        InputStream inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            length = length + bytesRead;
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        result.put("length", length);
        return result;
    }

    /**
     * 获取符合搜索条件记录总数
     *
     * @param cluster
     * @param project
     * @param keyWord
     * @param app
     * @param instance
     * @param hostID
     * @param source
     * @param fromTime
     * @param toTime
     * @return
     * @throws IOException
     * @throws ESClusterNotFoundException
     * @throws ESClientNotFoundException
     */
    public int getTotal(String cluster, String project, String keyWord, String app, String instance,
                        String hostID, String source, long fromTime, long toTime) throws IOException, ESClusterNotFoundException, ESClientNotFoundException, ESClustersResponseTimeoutException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        esFilterService.addProjectFilter(project, boolQueryBuilder);
        if (StringUtils.isNotEmpty(app) && !app.equals(GlobalConfig.ALL_APPS)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("fields.app", app));
        }
        if (StringUtils.isNotEmpty(instance) && !instance.equals(GlobalConfig.ALL_INSTANCES)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("fields.instance", instance));
        }
        if (StringUtils.isNotEmpty(hostID)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("host.id", hostID));
        }
        if (StringUtils.isNotEmpty(source)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("source", source));
        }
        if (!StringUtils.isEmpty(keyWord)) {
            QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(keyWord);
            boolQueryBuilder.must(queryBuilder);
        }

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp")
                .gte(TimeUtils.toDate(fromTime))
                .lte(TimeUtils.toDate(toTime));
        boolQueryBuilder.must(rangeQueryBuilder);
        sourceBuilder.size(0).query(boolQueryBuilder);
        String queryJson = sourceBuilder.toString();
        //log.info(queryJson);
        String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(indexPrefixService.getIndexPrefix(fromTime, toTime)), queryJson);
        Map responseMap = (Map) JSON.parse(resultJson);
        return (int) (((Map) responseMap.get("hits")).get("total"));
    }

    //public int getTotalBySource(String cluster, String project, String app, String instance, String source, String hostID, long fromTime, long toTime) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
    //    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    //    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    //
    //    if (StringUtils.isNotEmpty(project)) {
    //        boolQueryBuilder.must(QueryBuilders.termQuery("fields.project", project));
    //    }
    //    if (StringUtils.isNotEmpty(app) && !app.equals(Config.allApps)) {
    //        boolQueryBuilder.must(QueryBuilders.termQuery("fields.app", app));
    //    }
    //    if (StringUtils.isNotEmpty(instance) && !instance.equals(Config.allInstances)) {
    //        boolQueryBuilder.must(QueryBuilders.termQuery("fields.instance", instance));
    //    }
    //    if (StringUtils.isNotEmpty(hostID)) {
    //        boolQueryBuilder.must(QueryBuilders.termQuery("host.id", hostID));
    //    }
    //    if (StringUtils.isNotEmpty(source)) {
    //        boolQueryBuilder.must(QueryBuilders.termQuery("source", source));
    //    }
    //
    //    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp")
    //            .gte(TimeUtils.toDate(fromTime))
    //            .lte(TimeUtils.toDate(toTime));
    //    boolQueryBuilder.must(rangeQueryBuilder);
    //    //默认日志按照时间升序排列（即时间最新的在最下面）
    //    //for (String sortField : logSearchService.searchAfterSort) {
    //    //    sourceBuilder.sort(sortField, SortOrder.ASC);
    //    //}
    //    sourceBuilder.size(0).query(boolQueryBuilder);
    //    String queryJson = sourceBuilder.toString();
    //    log.info(queryJson);
    //    String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(EnvionmentVariables.INDEX_PREFIX), queryJson);
    //    Map responseMap = (Map) JSON.parse(resultJson);
    //    return (int) (((Map) responseMap.get("hits")).get("total"));
    //}

    //public int getTotalBySource(String cluster, String project, String source, String hostID, long fromTime, long toTime) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
    //    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    //    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    //
    //    if (StringUtils.isEmpty(source)) {
    //        throw new IllegalArgumentException(" source are not allowed to be empty ");
    //    }
    //    if (StringUtils.isEmpty(hostID)) {
    //        throw new IllegalArgumentException(" hostID are not allowed to be empty ");
    //    }
    //    boolQueryBuilder.must(QueryBuilders.termQuery("host.id", hostID));
    //    boolQueryBuilder.must(QueryBuilders.termQuery("source", source));
    //
    //    if (StringUtils.isNotEmpty(project)) {
    //        boolQueryBuilder.must(QueryBuilders.termQuery("fields.project", project));
    //    }
    //    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp")
    //            .gte(TimeUtils.toDate(fromTime))
    //            .lte(TimeUtils.toDate(toTime));
    //    boolQueryBuilder.must(rangeQueryBuilder);
    //    //默认日志按照时间升序排列（即时间最新的在最下面）
    //    //for (String sortField : logSearchService.searchAfterSort) {
    //    //    sourceBuilder.sort(sortField, SortOrder.ASC);
    //    //}
    //    sourceBuilder.size(0).query(boolQueryBuilder);
    //    String queryJson = sourceBuilder.toString();
    //    log.info(queryJson);
    //    String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(EnvionmentVariables.INDEX_PREFIX), queryJson);
    //    Map responseMap = (Map) JSON.parse(resultJson);
    //    return (int) (((Map) responseMap.get("hits")).get("total"));
    //}

    //public void downloadBySource(String cluster, String project, String docID, long fromTime, long toTime, HttpServletResponse res) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
    //    //根据doc id 获取source
    //    SourceSortEntity sourceSortEntity = logSearchService.getSortAndSourceByID(cluster, project, docID, SortOrder.DESC);
    //    //获取 该source一共有多少的docs
    //    int total = getTotalBySource(project, cluster, sourceSortEntity.getSource(), sourceSortEntity.getHostID(), fromTime, toTime);
    //
    //    String fileName = String.format("content-%d-%d-%s.txt", fromTime, toTime, docID);
    //    String downloadFielName = new String(fileName.getBytes("UTF-8"), "UTF-8");
    //    log.info("downloadFielName:" + downloadFielName);
    //    res.addHeader("Content-Disposition", "attachment;fileName=" + downloadFielName);
    //    res.setContentType("text/plain;charset=UTF-8");
    //    int length = 0;
    //    if (total == 0) {
    //        res.setHeader("Content-Length", String.valueOf(length));
    //        return;
    //    }
    //    if (total > EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE) {
    //        total = EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE;
    //        log.info("Maximum download allowed: " + EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE);
    //    }
    //
    //    OutputStream outputStream = res.getOutputStream();
    //    int cycleIndex = IntUtils.getCycleCount(total, pageSize);
    //    log.info("cycleIndex: " + cycleIndex);
    //    SearchResult searchResult;
    //    Map<String, Object> item;
    //    int bytesRead;
    //    byte[] buffer = new byte[1024];
    //    StringBuilder stringBuilder = new StringBuilder();
    //    for (int i = 0; i < cycleIndex; i++) {
    //        searchResult = logSearchService.getContextByID(cluster, project, null, null, fromTime, toTime, null, SortOrder.ASC,
    //                sourceSortEntity.getHostID(), sourceSortEntity.getSource(), pageSize);
    //        for (int j = 0; j < searchResult.getItems().size(); j++) {
    //            item = searchResult.getItems().get(j);
    //            stringBuilder.append(item.get("message"));
    //            stringBuilder.append(GlobalConfig.LINE_BREAK);
    //        }
    //        InputStream inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
    //        while ((bytesRead = inputStream.read(buffer)) != -1) {
    //            length = length + bytesRead;
    //            outputStream.write(buffer, 0, bytesRead);
    //        }
    //        stringBuilder.setLength(0);
    //        inputStream.close();
    //    }
    //    res.setHeader("Content-Length", String.valueOf(length));
    //    return;
    //}
}
