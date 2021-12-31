package com.wl.shardingdemo.algo;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.*;

/**
 * 复杂分片 库分片
 *
 * @author 王亮
 * @Description 复杂分片 库分片
 * @Date 2021/12/31
 */
public class MyTbComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    /**
     * 复制分片 库分片算法
     *
     * @param dbNames                  库名
     * @param complexKeysShardingValue 分片键
     * @return 库名
     */
    @Override
    public Collection<String> doSharding(Collection<String> dbNames, ComplexKeysShardingValue<Long> complexKeysShardingValue) {
        Map<String, Collection<Long>> columnNameAndShardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        Collection<Long> cidCollection = columnNameAndShardingValuesMap.get("cid");
        Set<String> result = new LinkedHashSet<>();
        Iterator<Long> iterator = cidCollection.iterator();
        String logicTableName = complexKeysShardingValue.getLogicTableName();
        // 确定范围路由的表
        while (iterator.hasNext()) {
            Long next = iterator.next();
            result.add(logicTableName + "_" + (next % 2 + 1));
        }
        return result;
    }
}
