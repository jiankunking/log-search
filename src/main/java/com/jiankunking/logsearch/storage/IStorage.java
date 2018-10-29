package com.jiankunking.logsearch.storage;

import com.orbitz.consul.model.kv.Value;

import java.util.List;
import java.util.Optional;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public interface IStorage {
    
    /**
     * 保存（新增 修改）
     *
     * @param id
     * @param value
     * @return
     */
    boolean save(String id, String value);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    boolean delete(String id);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    Optional<Value> getByID(String id);

    /**
     * 根据前缀查询
     *
     * @param keyPrefix
     * @return
     */
    List<Value> getByKeyPrefix(String keyPrefix);

}
