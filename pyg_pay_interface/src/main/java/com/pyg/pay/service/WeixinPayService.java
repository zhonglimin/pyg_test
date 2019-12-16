package com.pyg.pay.service;

import java.util.Map;

/**
 * @author huyy
 * @Title: WeixinPayService
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/1415:33
 */
public interface WeixinPayService {
    /**
     * 根据订单号和总金额生成预支付url
     * @param out_trade_no ： 订单号
     * @param total_fee  ： 总金额
     * @return 响应结果map
     */
    Map createNative(String out_trade_no, String total_fee) throws Exception;


    /**
     * 查询订单支付状态
     * @param out_trade_no ：订单号
     * @return
     */
    Map<String,String> queryOrderStatus(String out_trade_no) throws Exception;

    /**
     * 根据订单号关闭微信订单
     * @param out_trade_no ： 订单号
     * @return
     */
    Map<String,String> closeOrder(String out_trade_no) throws Exception;
}
