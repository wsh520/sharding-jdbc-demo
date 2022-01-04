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
public class MyDbComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

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
        Set<String> result = new LinkedHashSet<>();
        if (columnNameAndShardingValuesMap.size() > 0) {
            Collection<Long> userIdColl = columnNameAndShardingValuesMap.get("user_id");
            if (userIdColl.size() > 0) {
                for (Long aLong : userIdColl) {
                    if (aLong % 2 == 0) {
                        result.add("ds0");
                    } else {
                        result.add("ds1");
                    }
                }
            }

        }
        Map<String, Range<Long>> columnNameAndRangeValuesMap = complexKeysShardingValue.getColumnNameAndRangeValuesMap();
        Range<Long> longRange = columnNameAndRangeValuesMap.get("user_id");
        if (null != longRange) {

            Long lowerEndpoint = longRange.lowerEndpoint();
            Long upperEndpoint = longRange.upperEndpoint();
            //  确定范围路由的表
            for (long i = lowerEndpoint; i <= upperEndpoint; i++) {
                result.add("ds" + (i % 2));
            }
        }

        return result;
    }
}
