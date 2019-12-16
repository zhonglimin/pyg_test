package com.pyg.seckill.service.impl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSeckillOrderMapper;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.pojo.TbSeckillOrderExample;
import com.pyg.pojo.TbSeckillOrderExample.Criteria;
import com.pyg.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;


	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;


	@Autowired
	private IdWorker idWorker;

	/**
	 * 提交秒杀订单
	 *
	 * @param username ： 当前登录人
	 * @param id       ： 秒杀商品的id
	 */
	@Override
	public void submitOrder(String username, Long id) {
		//1. 根据商品id,获取秒杀商品对象
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);

        //2. 库存 - 1 ， 判断库存是否为0，为0，更新数据库，删除redis中的秒杀商品
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        if(seckillGoods.getStockCount() == 0){
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            redisTemplate.boundHashOps("seckillGoods").delete(id);
        }else{
            //3.  不为0，更新redis中的秒杀商品
            redisTemplate.boundHashOps("seckillGoods").put(id,seckillGoods);
        }

		//4. 生成秒杀订单，暂存到redis中
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        //未支付
        seckillOrder.setStatus("1");
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setUserId(username);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setSeckillId(id);
        //支付金额
        seckillOrder.setMoney(seckillGoods.getCostPrice());


        //暂存到redis中
        redisTemplate.boundHashOps("seckillOrder").put(username,seckillOrder);
	}

    /**
     * 根据当前登录人，获取redis中的秒杀订单
     *
     * @param username
     * @return
     */
    @Override
    public TbSeckillOrder getSeckillOrderFromRedis(String username) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
    }

    /**
     * 支付成功后，修改订单的状态，记录微信流水号，记录支付时间，删除redis中的秒杀订单
     *
     * @param username
     * @param out_trade_no   ： 秒杀订单号
     * @param transaction_id ： 微信订单号
     * @param time_end       ： 支付完成时间
     */
    @Override
    public void updateOrderStatus(String username, String out_trade_no, String transaction_id, String time_end) throws ParseException {
        //1.根据用户名获取秒杀订单
        TbSeckillOrder seckillOrder = getSeckillOrderFromRedis(username);

        //2. 修改状态为已支付，补全微信订单号，支付时间
        seckillOrder.setStatus("2");
        seckillOrder.setTransactionId(transaction_id);
        Date yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss").parse(time_end);
        seckillOrder.setPayTime(yyyyMMddHHmmss);

        //3. 保存到数据库
        seckillOrderMapper.insert(seckillOrder);


        //4. 删除redis中的秒杀订单
        redisTemplate.boundHashOps("seckillOrder").delete(username);

    }

    /**
     * 支付失败，恢复库存，删除redis秒杀订单
     *
     * @param username
     * @param out_trade_no
     */
    @Override
    public void deleteOrderFromRedis(String username, String out_trade_no) {
        //1. 根据用户名获取秒杀订单
        TbSeckillOrder seckillOrder = getSeckillOrderFromRedis(username);

        //2. 根据秒杀订单中的秒杀商品id ------------> 秒杀商品
        Long seckillId = seckillOrder.getSeckillId();
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);


        //3. 恢复库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
        redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);

        //4. 删除redis中的秒杀订单
        redisTemplate.boundHashOps("seckillOrder").delete(username);
    }

}
