package com.wl.shardingdemo.algo;

import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 范围库分片算法
 *
 * @author 王亮
 * @Description 范围库分片算法
 * @Date 2021/12/31
 */
public class MyDbRangeShardingAlgorithm implements RangeShardingAlgorithm<Integer> {

    /**
     * 范围库分片
     *
     * @param dbNames            库名
     * @param rangeShardingValue 值范围
     * @return 库名
     */
    @Override
    public Collection<String> doSharding(Collection<String> dbNames, RangeShardingValue<Integer> rangeShardingValue) {
        Range<Integer> valueRange = rangeShardingValue.getValueRange();
        Integer lowerValue = valueRange.lowerEndpoint();
        Integer upperValue = valueRange.upperEndpoint();
        Set<String> result = new LinkedHashSet<>();
        int size = dbNames.size();
        for (int i = lowerValue; i <= upperValue; i++) {
            for (String dbName : dbNames) {
                if (dbName.endsWith(i % size + "")) {
                    result.add(dbName);
                }
            }
        }
        return result;
    }
}
