package com.wl.shardingdemo.algo;

import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.*;

/**
 * @author 王亮
 * @Description TODO
 * @Date 2021/12/31
 */
public class MyTbHintShardingAlgorithm implements HintShardingAlgorithm<Integer> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, HintShardingValue<Integer> hintShardingValue) {
        // 这里组装 表名即可
        String logicTableName = hintShardingValue.getLogicTableName();
        Collection<Integer> values = hintShardingValue.getValues();
        Set<String> set = new HashSet();
        for (Integer value : values) {
            set.add(logicTableName + "_" + value);
        }
        return set;
    }
}
