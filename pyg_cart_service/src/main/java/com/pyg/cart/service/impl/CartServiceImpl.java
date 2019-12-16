package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huyy
 * @Title: CartServiceImpl
 * @ProjectName pyg_parent18
 * @Description: 购物车的服务实现
 * @date 2018/12/1115:08
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 添加商品到购物车列表
     *
     * @param cartList ： 添加前的购物车列表
     * @param itemId   ： 要添加的商品skuid
     * @param num      : 要添加商品的数量
     * @return 添加后的购物车列表
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) throws Exception {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);

        //2.根据sku商品数据，找到商家id，商家名称
        String sellerName = tbItem.getSeller();
        String sellerId = tbItem.getSellerId();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartFromCartList(cartList,sellerId);

        if(cart == null){
            //4. 不存在该商家的购物车

            //4.1 新建购物车，同时将要买的商品加到购物车中，同时将该购物车添加到购物车列表中
            cart = new Cart();
            cart.setSellerName(sellerName);
            cart.setSellerId(sellerId);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(tbItem,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            cartList.add(cart);
        }else{

            //5. 存在该商家的购物车

            //6. 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(itemId,cart.getOrderItemList());

            if(orderItem == null){
                //6.1 不存在该商品，新增购物车明细，添加到购物车明细列表中
                orderItem = createOrderItem(tbItem,num);
                cart.getOrderItemList().add(orderItem);


            }else{
                //6.2 存在该商品，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));

                //商品为0，移除该商品
                if(orderItem.getNum() == 0){
                    cart.getOrderItemList().remove(orderItem);
                }


                //商家的购物明细列表数据为0，移除该商家的购物车
                if(cart.getOrderItemList().size() == 0){
                    cartList.remove(cart);
                }
            }

        }

        return cartList;
    }



    /**
     * 根据tbItem对象创建购物明细对象
     * @param tbItem
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem tbItem, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);
        orderItem.setTitle(tbItem.getTitle());
        orderItem.setSellerId(tbItem.getSellerId());
        orderItem.setPrice(tbItem.getPrice());
        orderItem.setPicPath(tbItem.getImage());
        //skuid
        orderItem.setItemId(tbItem.getId());
        orderItem.setGoodsId(tbItem.getGoodsId());
        //小计
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * num));

        return orderItem;
    }

    /**
     * 查询购物车明细列表中是否存在该商品
     * @param itemId
     * @param orderItemList
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(Long itemId, List<TbOrderItem> orderItemList) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据商家ID判断购物车列表中是否存在该商家的购物车
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartFromCartList(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询redis购物车
     *
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        Object cartList = redisTemplate.boundHashOps("cartList").get(name);
        if(cartList == null){
            return  new ArrayList<>();
        }
        return (List<Cart>) cartList;
    }

    /**
     * 将购物车列表存放到redis中
     *
     * @param cartList_redis
     * @param name
     */
    @Override
    public void saveCartListToRedis(List<Cart> cartList_redis, String name) {
        redisTemplate.boundHashOps("cartList").put(name,cartList_redis);
    }

    /**
     * 两个购物车的合并
     *
     * @param cartList_redis  ： redis购物车
     * @param cartList_cookie ： cookie购物车
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) throws Exception {
        for (Cart cart : cartList_cookie) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList_redis = addGoodsToCartList(cartList_redis,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList_redis;
    }
}
