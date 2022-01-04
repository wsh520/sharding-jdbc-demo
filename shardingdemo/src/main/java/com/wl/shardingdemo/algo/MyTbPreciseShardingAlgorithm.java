package com.wl.shardingdemo.algo;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;
import java.util.Iterator;

/**
 * 自定义表标准分片
 *
 * @author 王亮
 * @Description 自定义精确分片算法
 * @Date 2021/12/30
 */
public class MyTbPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    /**
     * 精确分片算法
     *
     * @param tbNames           表信息
     * @param preciseShardingValue 分片键
     * @return 表名
     */
    @Override
    public String doSharding(Collection<String> tbNames, PreciseShardingValue<Long> preciseShardingValue) {
        String columnName = preciseShardingValue.getColumnName();
        if ("cid".equals(columnName)) {
            Long value = preciseShardingValue.getValue();
            int dbSuffix = (int)(value % 2 + 1);
            Iterator<String> iterator = tbNames.iterator();
            String logicTableName = preciseShardingValue.getLogicTableName();
            return logicTableName + "_" + dbSuffix;
        }

        throw new RuntimeException("路由数据库不存在!");
    }

}
