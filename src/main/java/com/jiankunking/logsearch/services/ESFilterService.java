package com.jiankunking.logsearch.services;


import com.jiankunking.logsearch.cache.NameSpacesCache;
import com.jiankunking.logsearch.util.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/10/11 14:40
 * @description:
 */
@Component
public class ESFilterService {

    @Autowired
    NameSpacesCache nameSpacesCache;

    /**
     * @param project
     * @param boolQueryBuilder
     */
    public void addProjectFilter(String project, BoolQueryBuilder boolQueryBuilder) {
        if (StringUtils.isNotEmpty(project)) {
            List<String> namespaces = nameSpacesCache.getNameSpaces(project);
            if (namespaces == null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("fields.project", project));
            } else {
                boolQueryBuilder.should(QueryBuilders.termQuery("fields.project", project));
                for (String namespace : namespaces) {
                    if (namespace.equalsIgnoreCase(project)) {
                        continue;
                    }
                    boolQueryBuilder.should(QueryBuilders.termQuery("fields.project", namespace));
                }
                boolQueryBuilder.minimumShouldMatch(1);
            }
        }
    }
}
