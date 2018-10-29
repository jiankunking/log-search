package com.jiankunking.logsearch.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jiankunking.
 * @date：2018/9/28 10:57
 * @description:
 */
public class ESClientLRUCache<K, V> extends LinkedHashMap<K, V> {

    /**
     * 缓存大小
     */
    private int cacheSize;

    public ESClientLRUCache(int cacheSize) {
        // 传入的第三个参数accessOrder为true的时候，就按访问顺序对LinkedHashMap排序，
        // 为false的时候就按插入顺序，默认是为false的。
        // 当把accessOrder设置为true后，就可以将最近访问的元素置于最前面
        super(10, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    /**
     * LinkedHashMap自带的判断是否删除最老的元素方法，默认返回false，即不删除老数据
     * 要做的就是重写这个方法，当满足一定条件时删除老数据
     * <p>
     * 缓存是否已满
     *
     * @param eldest
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        boolean r = size() > cacheSize;
        if (r) {
            System.out.println("清除缓存key：" + eldest.getKey());
        }
        return r;
    }
}
