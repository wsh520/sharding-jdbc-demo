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
public class MyDbHintShardingAlgorithm implements HintShardingAlgorithm<Integer> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, HintShardingValue<Integer> hintShardingValue) {
        Collection<Integer> values = hintShardingValue.getValues();
        Set<String> set = new HashSet<>();
        for (Integer value : values) {
            set.add("ds" + value);
        }
        return set;
    }
}
