package com.pyg.seckill.service;
import java.text.ParseException;
import java.util.List;
import com.pyg.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	/**
	 * 提交秒杀订单
	 * @param username ： 当前登录人
	 * @param id ： 秒杀商品的id
	 */
    void submitOrder(String username, Long id);

	/**
	 * 根据当前登录人，获取redis中的秒杀订单
	 * @param username
	 * @return
	 */
	TbSeckillOrder getSeckillOrderFromRedis(String username);

	/**
	 * 支付成功后，修改订单的状态，记录微信流水号，记录支付时间，删除redis中的秒杀订单
	 * @param username
	 * @param out_trade_no ： 秒杀订单号
	 * @param transaction_id ： 微信订单号
	 * @param time_end ： 支付完成时间
	 */
	void updateOrderStatus(String username, String out_trade_no, String transaction_id, String time_end) throws ParseException;

	/**
	 * 支付失败，恢复库存，删除redis秒杀订单
	 * @param username
	 * @param out_trade_no
	 */
	void deleteOrderFromRedis(String username, String out_trade_no);
}
