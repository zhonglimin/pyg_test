package com.pyg.order.service.impl;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.order.service.OrderService;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbOrderMapper;
import com.pyg.pojo.TbOrder;
import com.pyg.pojo.TbOrderExample;
import com.pyg.pojo.TbOrderExample.Criteria;
import com.pyg.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbOrderItemMapper orderItemMapper;


	@Autowired
	private TbPayLogMapper payLogMapper;



	@Autowired
	private IdWorker idWorker;

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//1.从redis中获取当前登录人的购物车列表数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		//2.遍历购物车列表：一个cart---tb_order---一个订单-----tb_order_item----多个订单明细

        double totalFee = 0.0;
        List<Long> ids = new ArrayList<>();
        for (Cart cart : cartList) {
            //一个cart---tb_order---一个订单
            //分布式id------分布式id生成器
            Long orderId = idWorker.nextId();

            ids.add(orderId);

            //订单的id
            order.setOrderId(orderId);
            order.setStatus("1");
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            order.setSellerId(cart.getSellerId());
            order.setSourceType("2");

            double payment = 0.0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //一个订单-----tb_order_item----多个订单明细
                Long id = idWorker.nextId();
                orderItem.setId(id);
                orderItem.setOrderId(orderId);
                //计算实付金额
                payment += orderItem.getTotalFee().doubleValue();
                orderItemMapper.insert(orderItem);
            }

            //计算总金额
            totalFee += payment;

            //实付金额
            order.setPayment(new BigDecimal(payment));
            //订单保存
            orderMapper.insert(order);

        }

        //添加生成父订单的逻辑，保存到数据，同时写入redis中一份
		if(order.getPaymentType().equals("1")){
        	//生成支付日志
			TbPayLog payLog = new TbPayLog();
            String out_trade_no = idWorker.nextId() + "";
            payLog.setOutTradeNo(out_trade_no);

            payLog.setUserId(order.getUserId());
            payLog.setCreateTime(new Date());
            //未支付状态
            payLog.setTradeState("0");
            payLog.setPayType(order.getPaymentType());
            //将单位转换成分
            Long totalFeeLong = (long)(totalFee * 100);
            payLog.setTotalFee(totalFeeLong);
            String orderList = ids.toString().replace("[","").replace("]","").replace(" ","");
            //子订单id列表
            payLog.setOrderList(orderList);


            //保存到数据库
            payLogMapper.insert(payLog);


            //写入redis中一份
            redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
		}


        //3.清除redis中购物车列表数据
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    /**
     * 根据当前登录人，获取支付日志
     *
     * @param username
     * @return
     */
    @Override
    public TbPayLog getPayLogFromRedis(String username) {

        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(username);
    }

    /**
     * 支付成功后，修改父子订单的状态，记录微信交易流水号，记录交易时间
     *
     * @param out_trade_no   ： 订单号
     * @param transaction_id : 微信订单号
     * @param time_end       ： 支付完成时间
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id, String time_end) throws ParseException {
        //1.根据订单号，从数据库取出支付日志，修改状态，记录微信流水，记录支付完成时间
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setTransactionId(transaction_id);
        //修改成已支付
        payLog.setTradeState("1");
        Date yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss").parse(time_end);
        //支付的完成时间
        payLog.setPayTime(yyyyMMddHHmmss);
        payLogMapper.updateByPrimaryKey(payLog);


        //2. 获取子订单列表，修改子订单的状态，记录支付完成时间
        String orderList = payLog.getOrderList();
        String[] split = orderList.split(",");
        for (String id : split) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(id));
            tbOrder.setStatus("2");
            tbOrder.setPaymentTime(yyyyMMddHHmmss);
            orderMapper.updateByPrimaryKey(tbOrder);
        }

        //3. 销毁redis中的支付日志
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }

}
