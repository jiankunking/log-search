package com.jiankunking.logsearch.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Data
public class AnsibleExtParameters {
    private Map<String, String> params = new HashMap<>();
    private List<String> hosts = new ArrayList<>();
    //private List<Map<String, Object>> register = new ArrayList<>();
}
