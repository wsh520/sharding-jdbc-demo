package com.wl.shardingdemo.adapter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wl.shardingdemo.entity.WlShardTestEntity;
import com.wl.shardingdemo.mapper.WlShardTestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 王亮
 * @Description 分库分表adapter
 * @Date 2021/12/30
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestAdapter {


    private final WlShardTestMapper wlShardTestMapper;

    /**
     * 测试数据入库
     *
     * @return 初始化状态
     */
    @PostMapping("/initDbData")
    public String initDbData() {
        int dataSize = 100;
        for (int i = 0; i < dataSize; i++) {
            WlShardTestEntity wlShardTestEntity = new WlShardTestEntity();
            wlShardTestEntity.setCname("test" + i);
            wlShardTestEntity.setUserId((long) (Math.random() * 100));
            wlShardTestEntity.setCstatus(i + "");
            wlShardTestMapper.insert(wlShardTestEntity);
        }

        return "init success";
    }


    /**
     *  获取单一数据
     *
     * @return WlShardTestEntity
     */
    @GetMapping("/one")
    public WlShardTestEntity getOne() {
        LambdaQueryWrapper<WlShardTestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WlShardTestEntity::getCid, 683339473181736960L);
        return wlShardTestMapper.selectOne(wrapper);
    }

    /**
     *  in 语句
     *
     * @return list
     */
    @GetMapping("/list/in")
    public List<WlShardTestEntity> getList() {
        LambdaQueryWrapper<WlShardTestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WlShardTestEntity::getUserId, 46L);
        wrapper.eq(WlShardTestEntity::getCid, 683622610776358912L);
        return wlShardTestMapper.selectList(wrapper);
    }

    /**
     *  between语句
     *  必须涉及到 对应的表分库字段
     *
     * @return list
     */
    @GetMapping("/list/between")
    public List<WlShardTestEntity> getBetweenList() {
        LambdaQueryWrapper<WlShardTestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WlShardTestEntity::getUserId,18L);
        wrapper.between(WlShardTestEntity::getCid,683679938234023936L, 683679951433498624L);
        return wlShardTestMapper.selectList(wrapper);
    }
}
