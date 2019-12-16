package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.pojogroup.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author huyy
 * @Title: CartController
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/1115:14
 */
@RestController
@RequestMapping("/cart")
public class CartController {


    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;


    @Autowired
    private HttpServletResponse response;

    /**
     * 查询购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //是否登录
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人：" + name);
        String cartList_string = CookieUtil.getCookieValue(request, "cartList", "utf-8");

        if(StringUtils.isEmpty(cartList_string)){
            cartList_string = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartList_string, Cart.class);
        if("anonymousUser".equals(name)){
            //未登录，操作cookie

            //1.从浏览器的cookie中取出来购物车列表 : List<Cart>
            //2. 返回购物车列表数据
            return cartList_cookie;

        }else{
            //登录了，操作redis购物车
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);



            //购物车的合并，cookie中必须有购物车信息
            if(cartList_cookie.size() > 0){
                try {
                    //购物车的合并
                    //1. 合并两个购物车，返回合并后的购物车列表
                    cartList_redis = cartService.mergeCartList(cartList_redis,cartList_cookie);

                    //2. 清除cookie中购物车列表
                    CookieUtil.deleteCookie(request,response,"cartList");

                    //3. 将合并后 的购物车列表写回到redis中
                    cartService.saveCartListToRedis(cartList_redis,name);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return cartList_redis;
        }

    }



    /**
     *  添加商品到购物车列表
     * @param itemId ： skuid
     * @param num : 购买的数量
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105")
    public Result addGoodsToCartList(Long itemId, Integer num){
        try {

            //处理跨越调用问题
            //允许http://localhost:9105 来跨域调用
//            response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
            //服务端会进行cookie的解析处理
//            response.setHeader("Access-Control-Allow-Credentials","true");


            //判断是否登录
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录人：" + name);
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList,itemId,num);
            if("anonymousUser".equals(name)){
                //未登录，操作cookie购物车
                //1.从浏览器的cookie中取出来购物车列表 : List<Cart>


                //2. 调用service层的逻辑，完成添加商品到购物车列表的业务，返回添加后的购物车列表 ： List<Cart>


                //3. 将添加后的购物车列表写回到客户端的浏览器里的cookie中
                String cartList_json = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request,response,"cartList",cartList_json,3600*24*7,"utf-8");

            }else{
                //登录，操作redis购物车

                //1. 从redis中获取购物车列表
//                List<Cart> cartList = findCartList();

                //2. 调用service层的逻辑，完成添加商品到购物车列表的业务，返回添加后的购物车列表 ： List<Cart>
//                cartList = cartService.addGoodsToCartList(cartList,itemId,num);

                //3. 将添加后的购物车列表写回到redis中
                cartService.saveCartListToRedis(cartList,name);
            }
            return new Result(true,"添加成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
}
