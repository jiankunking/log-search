package com.jiankunking.logsearch.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.SearchResult;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.model.SearchIDEntity;
import com.jiankunking.logsearch.services.LogSearchService;
import com.jiankunking.logsearch.util.*;
import com.jiankunking.logsearch.version.IApiVersion;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@Controller
public class LogSearchController implements IApiVersion {

    @Autowired
    LogSearchService logSearchService;

    /**
     * 根据关键字查询
     * 默认是按照时间升序加载即即时间最新的在最下面
     * 当加载更多的时候 会加载时间晚一些的
     *
     * @param fromTime
     * @param toTime
     * @param startID
     * @param keyword
     * @param pageSize
     * @param app
     * @param instance
     * @param cluster
     * @param project
     * @param response
     * @throws IOException
     * @throws ESClusterNotFoundException
     * @throws ESClientNotFoundException
     */
    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/logs")
    public void searchByKeyWord(@RequestParam(name = "fromTime", required = true) long fromTime,
                                @RequestParam(name = "toTime", required = true) long toTime,
                                @RequestParam(name = "startID", required = false) String startID,
                                @RequestParam(name = "keyword", required = false) String keyword,
                                @RequestParam(name = "pageSize", required = false, defaultValue = "100") int pageSize,
                                @PathVariable(name = "app", required = true) String app,
                                @PathVariable(name = "instance", required = true) String instance,
                                @PathVariable(value = "cluster", required = true) String cluster,
                                @PathVariable(value = "project", required = true) String project,
                                HttpServletResponse response) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {

        if (fromTime > toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime ");
        }
        SearchResult result;
        if (StringUtils.isEmpty(startID)) {
            result = logSearchService.queryStringByKeyWord(cluster, project, keyword, app, instance, null, null, pageSize, fromTime, toTime, null, SortOrder.DESC);
        } else {
            SearchIDEntity searchIDEntity = JsonUtils.parse(Base64Utils.decoder(startID), SearchIDEntity.class);
            Object[] searchAfterValues = new Object[]{searchIDEntity.getTimeStampSort(), searchIDEntity.getOffset(), searchIDEntity.getDocID()};
            result = logSearchService.queryStringByKeyWord(cluster, project, keyword, app, instance, null, null, pageSize, fromTime, toTime, searchAfterValues, SortOrder.DESC);
        }
        for (Map<String, Object> item : result.getItems()) {
            this.addStartID(item, project);
        }
        //升序 排序（时间越新 越在下面）
        Collections.sort(result.getItems(), (o1, o2) -> {
            return this.mapCompare(o1, o2);
        });
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    }

    /**
     * 根据id获取对应日志文件中某行的上下文
     * 首次加载的时候应该是某行的前后多少行（这时需要包含当前行）
     * 当再次滚动加载更多的时候 应该只加载前多少行或者后多少行（这时不需要包含当前行）
     *
     * @param fromTime
     * @param toTime
     * @param beforeLines
     * @param afterLines
     * @param startID
     * @param app
     * @param instance
     * @param cluster
     * @param project
     * @param response
     * @throws InterruptedException
     * @throws IOException
     * @throws ESClusterNotFoundException
     * @throws ESClientNotFoundException
     */
    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/contexts")
    public void getContextByStartID(@RequestParam(name = "fromTime", required = true) long fromTime,
                                    @RequestParam(name = "toTime", required = true) long toTime,
                                    @RequestParam(name = "beforeLines", required = true) int beforeLines,
                                    @RequestParam(name = "afterLines", required = true) int afterLines,
                                    @RequestParam(name = "startID", required = true) String startID,
                                    @PathVariable(name = "app", required = true) String app,
                                    @PathVariable(name = "instance", required = true) String instance,
                                    @PathVariable(value = "cluster", required = true) String cluster,
                                    @PathVariable(value = "project", required = true) String project,
                                    HttpServletResponse response) throws InterruptedException, IOException, ESClusterNotFoundException, ESClientNotFoundException {
        if (StringUtils.isEmpty(startID)) {
            throw new IllegalArgumentException(" startID are not allowed to be empty ");
        }
        if (fromTime > toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime ");
        }
        if (StringUtils.isEmpty(app) || app.equals(GlobalConfig.ALL_APPS)) {
            throw new IllegalArgumentException(" must specify the application name ");
        }
        if (StringUtils.isEmpty(instance) || app.equals(GlobalConfig.ALL_INSTANCES)) {
            throw new IllegalArgumentException("must specify the instance");
        }
        List<SearchResult> searchResultList = new ArrayList<>();
        if (beforeLines <= 0 && afterLines <= 0) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, searchResultList);
            return;
        }
        // afterLines 会将当前行计算在内 所以 afterLines应该默认加一
        int afterLinesAdjust = afterLines + 1;

        SearchIDEntity searchIDEntity = JsonUtils.parse(Base64Utils.decoder(startID), SearchIDEntity.class);
        boolean finish = false;
        if (beforeLines > 0 && afterLinesAdjust > 1) {
            CountDownLatch latch = new CountDownLatch(2);
            //前多少行 降序
            Thread threadBeforeLines = new Thread(() -> {
                try {
                    searchResultList.add(logSearchService.getContextLines(cluster, project, app, instance, fromTime, toTime, SortOrder.DESC, searchIDEntity, beforeLines));
                } catch (Exception ex) {
                    log.error("getContextByID beforeLines:", ex);
                } finally {
                    latch.countDown();
                }
            });
            //后多少行 升序
            Thread threadAfterLines = new Thread(() -> {
                try {
                    searchResultList.add(logSearchService.getContextLines(cluster, project, app, instance, fromTime, toTime, SortOrder.ASC, searchIDEntity, afterLinesAdjust));
                } catch (Exception ex) {
                    log.error("getContextByID afterLines:", ex);
                } finally {
                    latch.countDown();
                }
            });
            threadBeforeLines.start();
            threadAfterLines.start();
            latch.await();
            finish = true;
        }

        if (beforeLines > 0 && !finish) {
            searchResultList.add(logSearchService.getContextLines(cluster, project, app, instance, fromTime, toTime, SortOrder.DESC, searchIDEntity, beforeLines));
        }

        if (afterLinesAdjust > 1 && !finish) {
            SearchResult temp = logSearchService.getContextLines(cluster, project, app, instance, fromTime, toTime, SortOrder.ASC, searchIDEntity, afterLinesAdjust);
            searchResultList.add(temp);
        }

        SearchResult searchResult = SearchResult.mergeSearchResults(searchResultList);
        for (Map<String, Object> item : searchResult.getItems()) {
            this.addStartID(item, project);
        }
        //afterlines 移除当前行
        if (afterLinesAdjust > 1 && !finish) {
            this.removeStartLine(searchResult.getItems(), startID);
        }

        //升序 排序（时间越新 越在下面）
        Collections.sort(searchResult.getItems(), (o1, o2) -> {
            return this.mapCompare(o1, o2);
        });
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, searchResult);
    }

    private void removeStartLine(List<Map<String, Object>> list, String startID) {
        Iterator<Map<String, Object>> iter = list.iterator();
        while (iter.hasNext()) {
            if (iter.next().get("id").equals(startID)) {
                iter.remove();
                return;
            }
        }
    }

    private int mapCompare(Map<String, Object> o1, Map<String, Object> o2) {
        long map1time = Long.parseLong(o1.get("time").toString());
        long map2time = Long.parseLong(o2.get("time").toString());
        if (map1time != map2time) {
            return (int) (map1time - map2time);
        }
        int map1offset = (int) o1.get("offset");
        int map2offset = (int) o2.get("offset");
        if (map1offset != map2offset) {
            return (map1offset - map2offset);
        }
        String map1docID = (String) o1.get("docID");
        String map2docID = (String) o2.get("docID");
        return map1docID.compareTo(map2docID);
    }

    private void addStartID(Map<String, Object> item, String project) throws JsonProcessingException, UnsupportedEncodingException {
        SearchIDEntity temp = new SearchIDEntity();
        temp.setDocID(String.valueOf(item.get("id")));
        temp.setOffset(Integer.valueOf(String.valueOf(item.get("offset"))));
        temp.setTimeStampSort(ObjectUtils.objectToLong(item.get("time")));
        item.put("docID", String.valueOf(item.get("id")));
        item.put("id", Base64Utils.encoder(JsonUtils.toJSON(temp)));
        item.put("project", project);
    }

    //@TimeAnnotation
    //@RequestMapping(method = RequestMethod.GET, value = "/projects/{project}/apps/{app}/contexts")
    //public void getContextByID(@RequestParam(name = "fromTime", required = true) long fromTime,
    //                           @RequestParam(name = "toTime", required = true) long toTime,
    //                           @RequestParam(name = "beforeLines", required = true) int beforeLines,
    //                           @RequestParam(name = "afterLines", required = true) int afterLines,
    //                           @RequestParam(name = "id", required = true) String id,
    //                           @PathVariable(name = "app", required = true) String app,
    //                           @PathVariable(value = "project", required = true) String project,
    //                           HttpServletResponse response) throws InterruptedException, IOException {
    //    if (StringUtils.isEmpty(id)) {
    //        throw new IllegalArgumentException(" id are not allowed to be empty ");
    //    }
    //    if (fromTime > toTime) {
    //        throw new IllegalArgumentException(" fromTime must be smaller than the toTime ");
    //    }
    //    List<Map> result = new ArrayList<>();
    //
    //    if (beforeLines <= 0 && afterLines <= 0) {
    //        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    //        return;
    //    }
    //
    //    if (beforeLines > 0 && afterLines > 0) {
    //        CountDownLatch latch = new CountDownLatch(2);
    //        //前多少行 降序
    //        Thread threadBeforeLines = new Thread() {
    //            @Override
    //            public void run() {
    //                try {
    //                    result.addAll(logSearchService.getLines(project, app, fromTime, toTime, SortOrder.DESC, id, beforeLines));
    //                } catch (Exception ex) {
    //                    log.error("getContextByID beforeLines:", ex);
    //                } finally {
    //                    latch.countDown();
    //                }
    //            }
    //        };
    //        //后多少行 升序
    //        Thread threadAfterLines = new Thread() {
    //            @Override
    //            public void run() {
    //                try {
    //                    result.addAll(logSearchService.getLines(project, app, fromTime, toTime, SortOrder.ASC, id, afterLines));
    //                } catch (Exception ex) {
    //                    log.error("getContextByID afterLines:", ex);
    //                } finally {
    //                    latch.countDown();
    //                }
    //            }
    //        };
    //        threadBeforeLines.start();
    //        threadAfterLines.start();
    //        latch.await();
    //        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    //        return;
    //    }
    //
    //    if (beforeLines > 0) {
    //        result.addAll(logSearchService.getLines(project, app, fromTime, toTime, SortOrder.DESC, id, beforeLines));
    //        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    //        return;
    //    }
    //
    //    if (afterLines > 0) {
    //        result.addAll(logSearchService.getLines(project, app, fromTime, toTime, SortOrder.ASC, id, afterLines));
    //        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    //        return;
    //    }
    //}
}
