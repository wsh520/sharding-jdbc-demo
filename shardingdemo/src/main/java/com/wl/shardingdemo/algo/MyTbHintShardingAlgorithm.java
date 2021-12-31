package com.wl.shardingdemo.algo;

import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author 王亮
 * @Description TODO
 * @Date 2021/12/31
 */
public class MyTbHintShardingAlgorithm implements HintShardingAlgorithm<Long> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, HintShardingValue<Long> hintShardingValue) {
        // 这里组装 表名即可
        String logicTableName = hintShardingValue.getLogicTableName();
        Collection<Long> values = hintShardingValue.getValues();
        Iterator<Long> iterator = values.iterator();
        return Arrays.asList("wl_shard_test_1");
    }
}
