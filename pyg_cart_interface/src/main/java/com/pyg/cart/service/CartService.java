package com.pyg.cart.service;

import com.pyg.pojogroup.Cart;

import java.util.List;

/**
 * @author huyy
 * @Title: CartService
 * @ProjectName pyg_parent18
 * @Description: 购物车接口
 * @date 2018/12/1115:04
 */
public interface CartService {
    /**
     * 添加商品到购物车列表
     * @param cartList ： 添加前的购物车列表
     * @param itemId ： 要添加的商品skuid
     * @param num : 要添加商品的数量
     * @return  添加后的购物车列表
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) throws Exception;

    /**
     * 查询redis购物车
     * @return
     */
    List<Cart> findCartListFromRedis(String name);

    /**
     * 将购物车列表存放到redis中
     * @param cartList_redis
     * @param name
     */
    void saveCartListToRedis(List<Cart> cartList_redis, String name);

    /**
     * 两个购物车的合并
     * @param cartList_redis ： redis购物车
     * @param cartList_cookie ： cookie购物车
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) throws  Exception;
}
