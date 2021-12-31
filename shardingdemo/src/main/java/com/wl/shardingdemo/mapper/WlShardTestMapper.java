package com.wl.shardingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.shardingdemo.entity.WlShardTestEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 王亮
 * @Description TODO
 * @Date 2021/12/30
 */
@Mapper
public interface WlShardTestMapper extends BaseMapper<WlShardTestEntity> {
}
