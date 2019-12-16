package com.pyg.task.service;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author huyy
 * @Title: SeckillTask
 * @ProjectName pyg_parent18
 * @Description: 定时任务的演示
 * @date 2018/12/1714:50
 */
@Component
public class SeckillTask {


    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 定时任务
     */
    /*@Scheduled(cron = "10,20,30,40,50 * * * * ?")
    public void refreshSeckillGoods(){
        //任务，定时爬去某个网站，定时增量更新缓存
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("执行了任务调度"+sdf.format(new Date()));
    }*/

    /**
     * 增量更新数据库中新增的秒杀商品，更新到redis缓存中
     */
    @Scheduled(cron = "10,30 * * * * ?")
    public void refreshSeckillGoods(){
        try {
            //任务，定时爬去某个网站，定时增量更新缓存
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("执行了任务调度"+sdf.format(new Date()));


            //1.查询redis中的秒杀商品的id集合
            Set seckillGoodsIds = redisTemplate.boundHashOps("seckillGoods").keys();

            TbSeckillGoodsExample example = new TbSeckillGoodsExample();
            TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
            //2. 排除已经在redis中存在的秒杀商品
            if(seckillGoodsIds != null && seckillGoodsIds.size() > 0){
                criteria.andIdNotIn(new ArrayList<>(seckillGoodsIds));
            }

            //3. 审核通过了，库存大于0 ， 开始时间<= 当前时间     结束时间>当前时间
            criteria.andStatusEqualTo("1");
            criteria.andStockCountGreaterThan(0);
            criteria.andStartTimeLessThanOrEqualTo(new Date());
            criteria.andEndTimeGreaterThan(new Date());


            List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGoods.getId(),tbSeckillGoods);
            }
            System.out.println("将"+seckillGoodsList.size()+"条商品数据放入redis缓存");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除过期的秒杀商品
     */
    @Scheduled(cron = "30,50 * * * * ?")
    public void removeSeckillGoods(){

        //1. 查询redis中的秒杀商品
        System.out.println("移除秒杀商品任务在执行");
        //扫描缓存中秒杀商品列表，发现过期的移除
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();


        //2. 判断是否满足移除条件
        for (TbSeckillGoods seckill : seckillGoodsList) {
            if(seckill.getEndTime().getTime()<new Date().getTime()  ){//如果结束日期小于当前日期，则表示过期
                seckillGoodsMapper.updateByPrimaryKey(seckill);//向数据库保存记录[更新秒杀商品到数据库，需要同步库存信息，不一定库存为0]
                redisTemplate.boundHashOps("seckillGoods").delete(seckill.getId());//移除缓存数据
                System.out.println("移除秒杀商品"+seckill.getId());
            }
        }
    }



}
