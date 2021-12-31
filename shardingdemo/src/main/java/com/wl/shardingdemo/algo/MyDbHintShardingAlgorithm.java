package com.wl.shardingdemo.algo;

import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 王亮
 * @Description TODO
 * @Date 2021/12/31
 */
public class MyDbHintShardingAlgorithm implements HintShardingAlgorithm<Long> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, HintShardingValue<Long> hintShardingValue) {
        String logicTableName = hintShardingValue.getLogicTableName();
        Collection<Long> values = hintShardingValue.getValues();
        Set<String> set = new HashSet<>();
        set.add("ds0");
        return set;
    }
}
