package com.wl.shardingdemo.algo;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;
import java.util.Iterator;

/**
 * 自定义库标准分片
 *
 * @author 王亮
 * @Description 自定义精确分片算法
 * @Date 2021/12/30
 */
public class MyDbPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    /**
     * 精确分片算法
     *
     * @param dbNames           表信息
     * @param preciseShardingValue 分片键
     * @return 表名
     */
    @Override
    public String doSharding(Collection<String> dbNames, PreciseShardingValue<Long> preciseShardingValue) {
        String columnName = preciseShardingValue.getColumnName();
        if ("user_id".equals(columnName)) {
            Long value = preciseShardingValue.getValue();
            int size = dbNames.size();
            int dbSuffix = (int)(value % size + 1);
            Iterator<String> iterator = dbNames.iterator();
            int count = 1;
            // 根据 用户id奇偶 进行数据库路由
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (dbSuffix == count) {
                    return next;
                } else {
                    count++;
                }
            }
        }

        throw new RuntimeException("路由数据库不存在!");
    }

}
