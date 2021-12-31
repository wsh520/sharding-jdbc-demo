package com.wl.shardingdemo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 王亮
 * @Description TODO
 * @Date 2021/12/30
 */
@Data
@TableName("wl_shard_test")
public class WlShardTestEntity {

    private Long cid;

    private String cname;

    private Long userId;

    private String cstatus;

}
