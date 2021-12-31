package com.wl.shardingdemo.algo;

import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author 王亮
 * @Description TODO
 * @Date 2021/12/31
 */
public class MyTbRangeShardingAlgorithm implements RangeShardingAlgorithm<Long> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {

//        String logicTableName = rangeShardingValue.getLogicTableName();
        return Arrays.asList("wl_shard_test_1","wl_shard_test_2");
    }

}
