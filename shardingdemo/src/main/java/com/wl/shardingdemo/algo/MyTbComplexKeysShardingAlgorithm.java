package com.wl.shardingdemo.algo;

import com.google.common.collect.Range;
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
        Map<String, Range<Long>> columnNameAndRangeValuesMap = complexKeysShardingValue.getColumnNameAndRangeValuesMap();
        Collection<Long> cidCollection = columnNameAndShardingValuesMap.get("cid");
        String logicTableName = complexKeysShardingValue.getLogicTableName();
        Set<String> result = new LinkedHashSet<>();
        if (null != cidCollection && cidCollection.size() > 0) {
            for (Long next : cidCollection) {
                result.add(logicTableName + "_" + (next % 2 + 1));
            }
        }
        Range<Long> longRange = columnNameAndRangeValuesMap.get("cid");
        if (null != longRange) {

            Long lowerEndpoint = longRange.lowerEndpoint();
            Long upperEndpoint = longRange.upperEndpoint();
            //  确定范围路由的表
//            for (long i = lowerEndpoint; i <= upperEndpoint; i++) {
//                result.add(logicTableName + "_" + (i % 2 + 1));
//            }
            result.add("wl_shard_test_1");
            result.add("wl_shard_test_2");
        }
        return result;
    }
}
