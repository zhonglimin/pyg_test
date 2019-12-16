package com.pyg.order.service;
import java.text.ParseException;
import java.util.List;
import com.pyg.pojo.TbOrder;

import com.pyg.pojo.TbPayLog;
import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface OrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbOrder order);
	
	
	/**
	 * 修改
	 */
	public void update(TbOrder order);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbOrder findOne(Long id);
	
	
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
	public PageResult findPage(TbOrder order, int pageNum, int pageSize);

	/**
	 * 根据当前登录人，获取支付日志
	 * @param username
	 * @return
	 */
    TbPayLog getPayLogFromRedis(String username);

	/**
	 * 支付成功后，修改父子订单的状态，记录微信交易流水号，记录交易时间
	 * @param out_trade_no ： 订单号
	 * @param transaction_id : 微信订单号
	 * @param time_end ： 支付完成时间
	 */
	void updateOrderStatus(String out_trade_no, String transaction_id, String time_end) throws ParseException;
}
