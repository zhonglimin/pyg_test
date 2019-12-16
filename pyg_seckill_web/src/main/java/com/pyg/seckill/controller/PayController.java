package com.pyg.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.Map;

/**
 * @author huyy
 * @Title: PayController
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/1415:50
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private IdWorker idWorker;

    @Reference
    private WeixinPayService weixinPayService;


    /*@Reference
    private OrderService orderService;*/

    //秒杀订单服务
    @Reference
    private SeckillOrderService seckillOrderService;


    /**
     * 生成预支付url的方法
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        try {
            //假设父订单已经存在
//            String out_trade_no = idWorker.nextId()+"";

            //总金额，单位是分，不能有小数
//            String total_fee = "1";

            //调用订单服务，从redis中取出来支付日志，获取订单号和总金额
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            /*TbPayLog payLog = orderService.getPayLogFromRedis(username);
            String out_trade_no = payLog.getOutTradeNo();
            String total_fee = payLog.getTotalFee() + "";*/

            TbSeckillOrder seckillOrder = seckillOrderService.getSeckillOrderFromRedis(username);
            String out_trade_no = seckillOrder.getId() + "";
            //总金额，单位是分，不能有小数
            String total_fee = (long)(seckillOrder.getMoney().doubleValue() * 100) + "";

            //调用服务层的方法，获取预支付url
            return weixinPayService.createNative(out_trade_no,total_fee);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 查询订单的支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryOrderStatus")
    public Result queryOrderStatus(String out_trade_no){

        Result result = null;
        //获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            int count = 1;
            while(true){
                //查询订单的支付状态
                Map<String,String> map = weixinPayService.queryOrderStatus(out_trade_no);
                if(map == null){
                    //失败了
                    result= new Result(false,"支付失败");
                    break;
                }

                if("SUCCESS".equals(map.get("return_code"))){
                    //成功

                    if("SUCCESS".equals(map.get("trade_state"))){
                        //支付成功
                        //TODO 修改订单状态，记录微信交易流水号  记录支付时间
//                        orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"),map.get("time_end"));
                        seckillOrderService.updateOrderStatus(username, out_trade_no,map.get("transaction_id"),map.get("time_end"));
                        result= new Result(true,"支付成功");
                        break;
                    }
                    //已关闭
                    //已撤销
                }

                //间隔3秒
                Thread.sleep(3000);

                count++;
                if(count >= 100){
                    result= new Result(false,"PAY_TIME_OUT");
                    //立即关闭微信的订单
                    Map<String,String> closeMap = weixinPayService.closeOrder(out_trade_no);
                    if(!"SUCCESS".equals(closeMap.get("result_code"))){
                        //关闭订单不成功
                        if("ORDERPAID".equals(closeMap.get("err_code"))){
                            //已支付，不能关闭，按照支付成功处理
                            result= new Result(true,"支付成功");
                            seckillOrderService.updateOrderStatus(username,out_trade_no,map.get("transaction_id"),map.get("time_end"));
                        }
                    }
                    if(!result.isSuccess()){
                        //没成功,恢复库存，删除redis秒杀订单
                        seckillOrderService.deleteOrderFromRedis(username,out_trade_no);
                    }

                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
