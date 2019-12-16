package com.pyg.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huyy
 * @Title: WeixinPayServiceImpl
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/1415:56
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {


    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;


    /**
     * 根据订单号和总金额生成预支付url
     *
     * @param out_trade_no ： 订单号
     * @param total_fee    ： 总金额
     * @return 响应结果map
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) throws Exception {
        //1.封装请求参数
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body","品优购双十二秒杀商品");
        param.put("out_trade_no",out_trade_no);
        param.put("total_fee",total_fee);
        param.put("spbill_create_ip","192.168.127.145");
        param.put("notify_url","https://www.jd.com");
        param.put("trade_type","NATIVE");

        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        HttpClient client=new HttpClient(url);

        //https的协议
        client.setHttps(true);

        //2.设置请求参数
        String xmlParam = WXPayUtil.generateSignedXml(param,partnerkey);
        System.out.println("请求参数：");
        System.out.println(xmlParam);
        client.setXmlParam(xmlParam);

        //3.发送请求
        client.post();
        //4. 返回响应结果
        String content = client.getContent();
        System.out.println("响应结果：");
        System.out.println(content);

        Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("out_trade_no",out_trade_no);
        returnMap.put("total_fee",total_fee);
        returnMap.put("code_url",resultMap.get("code_url"));
        return returnMap;
    }

    /**
     * 查询订单支付状态
     *
     * @param out_trade_no ：订单号
     * @return
     */
    @Override
    public Map<String, String> queryOrderStatus(String out_trade_no) throws Exception {

        //1. 封装请求参数
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("out_trade_no",out_trade_no);

        //2. 设置请求参数
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        HttpClient client = new HttpClient(url);
        client.setHttps(true);

        String xmlParam = WXPayUtil.generateSignedXml(param,partnerkey);
        System.out.println(xmlParam);
        client.setXmlParam(xmlParam);

        //3. 发送请求
        client.post();


        //4. 返回响应结果
        String content = client.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
        return resultMap;
    }

    /**
     * 根据订单号关闭微信订单
     *
     * @param out_trade_no ： 订单号
     * @return
     */
    @Override
    public Map<String, String> closeOrder(String out_trade_no) throws Exception {

        //1. 封装请求参数
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("out_trade_no",out_trade_no);

        //2. 设置请求参数
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        HttpClient client = new HttpClient(url);
        client.setHttps(true);

        String xmlParam = WXPayUtil.generateSignedXml(param,partnerkey);
        System.out.println(xmlParam);
        client.setXmlParam(xmlParam);

        //3. 发送请求
        client.post();


        //4. 返回响应结果
        String content = client.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
        return resultMap;
    }
}
