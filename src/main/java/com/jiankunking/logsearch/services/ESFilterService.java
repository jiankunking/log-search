package com.jiankunking.logsearch.services;


import com.jiankunking.logsearch.cache.NameSpacesCache;
import com.jiankunking.logsearch.model.k8s.Cluster;
import com.jiankunking.logsearch.util.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
            List<Cluster> clusters = nameSpacesCache.getClusters(project);
            if (clusters == null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("fields.project", project));
            } else {
                // k8s 是使用namespace来充当的project
                boolQueryBuilder.should(QueryBuilders.termQuery("fields.project", project));
                //去重
                HashMap<String, Object> map = new HashMap<>(10);
                for (Cluster cluster : clusters) {
                    boolQueryBuilder.should(QueryBuilders.termQuery("fields.k8sCluster", cluster.getCluster()));
                    for (String namespace : cluster.getNamespaces()) {
                        if (namespace.equalsIgnoreCase(project)) {
                            continue;
                        }
                        if (map.containsKey(namespace)) {
                            continue;
                        }
                        map.put(namespace, null);
                        boolQueryBuilder.should(QueryBuilders.termQuery("fields.project", namespace));
                    }
                }
                boolQueryBuilder.minimumShouldMatch(2);
            }
        }
    }
}
