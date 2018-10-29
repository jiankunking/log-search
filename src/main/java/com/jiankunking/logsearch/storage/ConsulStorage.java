package com.jiankunking.logsearch.storage;


import com.jiankunking.logsearch.client.ConsulClientSingleton;
import com.orbitz.consul.model.kv.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Component
public class ConsulStorage implements IStorage {

    @Override
    public boolean save(String id, String value) {
        return ConsulClientSingleton.getKeyValueClient().putValue(id, value);
    }

    @Override
    public boolean delete(String id) {
        ConsulClientSingleton.getKeyValueClient().deleteKey(id);
        return true;
    }

    @Override
    public Optional<Value> getByID(String id) {
        return ConsulClientSingleton.getKeyValueClient().getValue(id);
    }

    @Override
    public List<Value> getByKeyPrefix(String keyPrefix) {
        return ConsulClientSingleton.getKeyValueClient().getValues(keyPrefix);
    }
}
