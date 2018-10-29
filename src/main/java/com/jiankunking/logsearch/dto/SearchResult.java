package com.jiankunking.logsearch.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Data
public class SearchResult {
    private MetaData metadata = new MetaData();

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    private List<Map<String, Object>> items = new ArrayList<>();

    public void setTotal(int total) {
        MetaData metaData = new MetaData();
        metaData.setTotal(total);
        this.setMetadata(metaData);
    }

    public static SearchResult mergeSearchResults(List<SearchResult> list) {
        SearchResult searchResult = new SearchResult();
        List<Map<String, Object>> items = new ArrayList<>();
        int total = list.get(0).getMetadata().getTotal();
        //上下文 前后多少行的时候时候 两次结果的total 均表示符合条件的全部
        //所以不需要再相加
        for (SearchResult item : list) {
            items.addAll(item.getItems());
        }
        searchResult.setItems(items);
        searchResult.setTotal(total);
        return searchResult;
    }


}
