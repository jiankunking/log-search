package com.jiankunking.logsearch.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.SearchResult;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.model.SearchIDEntity;
import com.jiankunking.logsearch.util.ESQueryUtils;
import com.jiankunking.logsearch.util.MapUtils;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description: query_string http://cwiki.apachecn.org/pages/viewpage.action?pageId=4883355
 * search_after 为null 不传递的时候，会返回排序结果最上面的，比如排序方式为ASC（升序），那么返回的就是时间最早的
 */
@Slf4j
@Service
public class LogSearchService {

    @Autowired
    ESFilterService esFilterService;
    @Autowired
    IndexPrefixService indexPrefixService;

    /**
     * elasticsearch 6.x _uid 废弃 替换为_id
     */
    public String[] searchAfterSort = new String[]{"@timestamp", "offset", "_uid"};
    //public String allApps = "all";

    /**
     * 基于query string 搜索
     *
     * @param cluster
     * @param project
     * @param keyWord
     * @param app
     * @param instance
     * @param hostID
     * @param source
     * @param pageSize
     * @param fromTime
     * @param toTime
     * @param searchAfterValues
     * @return
     * @throws IOException
     * @throws ESClusterNotFoundException
     * @throws ESClientNotFoundException
     */
    public SearchResult queryStringByKeyWord(String cluster, String project, String keyWord, String app, String instance,
                                             String hostID, String source,
                                             int pageSize, long fromTime, long toTime,
                                             Object[] searchAfterValues, SortOrder sortOrder) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        esFilterService.addProjectFilter(project, boolQueryBuilder);
        if (!StringUtils.isEmpty(app) && !app.equals(GlobalConfig.ALL_APPS)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("fields.app", app));
        }
        if (!StringUtils.isEmpty(instance) && !instance.equals(GlobalConfig.ALL_INSTANCES)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("fields.instance", instance));
        }
        if (StringUtils.isNotEmpty(hostID)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("host.id", hostID));
        }
        if (StringUtils.isNotEmpty(source)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("source", source));
        }

        if (searchAfterValues != null && searchAfterValues.length > 0) {
            sourceBuilder.searchAfter(searchAfterValues);
        }

        if (StringUtils.isNotEmpty(keyWord)) {
            QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(keyWord);
            boolQueryBuilder.must(queryBuilder);

            HighlightBuilder highlightBuilder = new HighlightBuilder()
                    .field("message")
                    .requireFieldMatch(false)
                    .preTags("<logHighlight>")
                    .postTags("</logHighlight>")
                    .fragmentSize(800000)
                    .numOfFragments(0);
            sourceBuilder.highlighter(highlightBuilder);
        }

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp")
                .gte(TimeUtils.toDate(fromTime))
                .lte(TimeUtils.toDate(toTime));
        boolQueryBuilder.must(rangeQueryBuilder);

        //默认日志按照时间升序排列（即时间最新的在最下面）
        //首次返回时间最早的
        for (String sortField : searchAfterSort) {
            if (sortOrder == null) {
                sourceBuilder.sort(sortField, SortOrder.ASC);
                continue;
            }
            sourceBuilder.sort(sortField, sortOrder);
        }

        String[] includes = new String[]{"message", "fields.type", "offset", "fields.app", "fields.instance"};
        String[] excludes = new String[]{"beat", "host", "docker", "input", "prospector", "fields.project"};
        sourceBuilder.fetchSource(includes, excludes);

        sourceBuilder.size(pageSize).query(boolQueryBuilder);
        String queryJson = sourceBuilder.toString();
        String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(indexPrefixService.getIndexPrefix(fromTime, toTime)), queryJson);

        Map responseMap = (Map) JSON.parse(resultJson);

        int total = (int) (((Map) responseMap.get("hits")).get("total"));
        JSONArray jsonArray = (JSONArray) (((Map) responseMap.get("hits")).get("hits"));
        JSONObject itemJsonObj;
        JSONArray sortArray;

        SearchResult searchResult = new SearchResult();
        List<Map<String, Object>> items = new ArrayList<>();

        for (Object item : jsonArray) {
            itemJsonObj = (JSONObject) item;
            sortArray = ((JSONArray) itemJsonObj.get("sort"));
            HashMap<String, Object> tempMap = new HashMap(MapUtils.getSize(9));
            for (Object sortItem : sortArray) {
                if (String.valueOf(sortItem).contains("#")) {
                    continue;
                }
                tempMap.put("time", String.valueOf(sortItem));
                break;
            }
            tempMap.put("message", ((Map) itemJsonObj.get("_source")).get("message"));
            tempMap.put("id", itemJsonObj.get("_id"));
            tempMap.put("index", itemJsonObj.get("_index"));
            tempMap.put("offset", ((Map) itemJsonObj.get("_source")).get("offset"));
            //处理 highlight
            if (itemJsonObj.containsKey("highlight") && ((Map<Object, Object>) itemJsonObj.get("highlight")).containsKey("message")) {
                tempMap.put("message", ((JSONArray) ((Map) itemJsonObj.get("highlight")).get("message")).get(0));
            }
            if (!itemJsonObj.containsKey("_source") || !((Map) itemJsonObj.get("_source")).containsKey("fields")) {
                items.add(tempMap);
                continue;
            }
            if (((Map) ((Map) itemJsonObj.get("_source")).get("fields")).containsKey("type")) {
                tempMap.put("type", ((Map) ((Map) itemJsonObj.get("_source")).get("fields")).get("type"));
            }
            if (((Map) ((Map) itemJsonObj.get("_source")).get("fields")).containsKey("app")) {
                tempMap.put("app", ((Map) ((Map) itemJsonObj.get("_source")).get("fields")).get("app"));
            }
            if (((Map) ((Map) itemJsonObj.get("_source")).get("fields")).containsKey("instance")) {
                tempMap.put("instance", ((Map) ((Map) itemJsonObj.get("_source")).get("fields")).get("instance"));
            }
            items.add(tempMap);
        }
        searchResult.setItems(items);
        searchResult.setTotal(total);
        return searchResult;
    }

    private SearchResult getContextByID(String cluster, String project, String app, String instance, long fromTime, long toTime,
                                        Object[] searchAfterValues, SortOrder order,
                                        String hostID, String source, int lines) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
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

        String[] includes = new String[]{"message", "fields.type", "offset"};
        String[] excludes = new String[]{"beat", "host", "docker", "input", "prospector", "fields.project"};
        sourceBuilder.fetchSource(includes, excludes);

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("@timestamp")
                .gte(TimeUtils.toDate(fromTime))
                .lte(TimeUtils.toDate(toTime));
        boolQueryBuilder.must(rangeQueryBuilder);
        sourceBuilder.size(lines).query(boolQueryBuilder);
        //排序
        for (String sortField : searchAfterSort) {
            sourceBuilder.sort(sortField, order);
        }
        if (searchAfterValues != null) {
            sourceBuilder.searchAfter(searchAfterValues);
        }

        String queryJson = sourceBuilder.toString();
        //log.info(queryJson);
        String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(indexPrefixService.getIndexPrefix(fromTime, toTime)), queryJson);

        Map responseMap = (Map) JSON.parse(resultJson);
        int total = (int) (((Map) responseMap.get("hits")).get("total"));
        JSONArray jsonArray = (JSONArray) (((Map) responseMap.get("hits")).get("hits"));
        JSONObject itemJsonObj;
        JSONArray sortArray;
        SearchResult searchResult = new SearchResult();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object item : jsonArray) {
            itemJsonObj = (JSONObject) item;
            HashMap<String, Object> temp = new HashMap(9);

            temp.put("id", itemJsonObj.get("_id"));
            temp.put("index", itemJsonObj.get("_index"));
            temp.put("message", ((Map) itemJsonObj.get("_source")).get("message"));

            if (itemJsonObj.containsKey("_source") && ((Map) itemJsonObj.get("_source")).containsKey("fields")
                    && ((Map) ((Map) itemJsonObj.get("_source")).get("fields")).containsKey("type")) {
                temp.put("type", ((Map) ((Map) itemJsonObj.get("_source")).get("fields")).get("type"));
            }
            temp.put("offset", ((Map) itemJsonObj.get("_source")).get("offset"));
            sortArray = ((JSONArray) itemJsonObj.get("sort"));
            for (Object sortItem : sortArray) {
                if (String.valueOf(sortItem).contains("#")) {
                    continue;
                }
                temp.put("time", String.valueOf(sortItem));
                break;
            }
            items.add(temp);
        }
        searchResult.setItems(items);
        searchResult.setTotal(total);
        return searchResult;
    }

    public SearchResult getContextLines(String cluster, String project, String app, String instance,
                                        long fromTime, long toTime,
                                        SortOrder order,
                                        SearchIDEntity searchIDEntity,
                                        int lines) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
        ArrayList<Object> sorts = new ArrayList<>();
        sorts.add(searchIDEntity.getTimeStampSort());
        sorts.add(searchIDEntity.getOffset());
        sorts.add(searchIDEntity.getDocID());

        return this.getContextByID(cluster, project, app, instance, fromTime, toTime,
                sorts.toArray(), order,
                null, null, lines);
    }

    //public SearchResult getContextLines(String cluster, String project, String app, String instance,
    //                                    long fromTime, long toTime,
    //                                    SortOrder order,
    //                                    String id,
    //                                    int lines) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
    //    SourceSortEntity sourceSortEntity = this.getSortAndSourceByID(cluster, project, id, order);
    //    if (sourceSortEntity.getSorts() == null || sourceSortEntity.getSorts().size() == 0) {
    //        return new SearchResult();
    //    }
    //    return this.getContextByID(cluster, project, app, instance, fromTime, toTime,
    //            sourceSortEntity.getSorts().toArray(), order,
    //            sourceSortEntity.getHostID(), sourceSortEntity.getSource(), lines);
    //}

    //public SourceSortEntity getSortAndSourceByID(String cluster, String project, String id, SortOrder sortOrder) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
    //    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    //
    //    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    //    boolQueryBuilder.must(QueryBuilders.termQuery("_id", id));
    //
    //    sourceBuilder.size(1).query(boolQueryBuilder);
    //    //排序
    //    if (sortOrder != null) {
    //        for (String sortField : searchAfterSort) {
    //            sourceBuilder.sort(sortField, sortOrder);
    //        }
    //    }
    //    String[] includes = new String[]{"sort", "source", "host.id"};
    //    sourceBuilder.fetchSource(includes, null);
    //    String queryJson = sourceBuilder.toString();
    //    logger.info(queryJson);
    //    String resultJson = ESQueryUtils.performRequest(cluster, project, HttpMethod.GET.name(), ESQueryUtils.getEndpoint(EnvionmentVariables.INDEX_PREFIX), queryJson);
    //
    //    Map responseMap = (Map) JSON.parse(resultJson);
    //    JSONArray jsonArray = (JSONArray) (((Map) responseMap.get("hits")).get("hits"));
    //    JSONObject itemJsonObj;
    //    JSONArray sortArray;
    //    ArrayList<Object> sortList = new ArrayList<>();
    //    SourceSortEntity sourceSortEntity = new SourceSortEntity();
    //    for (Object item : jsonArray) {
    //        itemJsonObj = (JSONObject) item;
    //        if (sortOrder != null) {
    //            sortArray = ((JSONArray) itemJsonObj.get("sort"));
    //            for (Object sortItem : sortArray) {
    //                if (String.valueOf(sortItem).contains("#")) {
    //                    continue;
    //                }
    //                sortList.add(sortItem);
    //            }
    //        }
    //        //获取_id
    //        sortList.add(itemJsonObj.get("_id"));
    //
    //        itemJsonObj = ((JSONObject) itemJsonObj.get("_source"));
    //        sourceSortEntity.setSource(String.valueOf(itemJsonObj.get("source")));
    //        itemJsonObj = ((JSONObject) itemJsonObj.get("host"));
    //        sourceSortEntity.setHostID(String.valueOf(itemJsonObj.get("id")));
    //    }
    //    sourceSortEntity.setSorts(sortList);
    //    return sourceSortEntity;
    //}
}
