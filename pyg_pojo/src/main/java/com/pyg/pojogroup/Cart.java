package com.pyg.pojogroup;

import com.pyg.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * @author huyy
 * @Title: Cart
 * @ProjectName pyg_parent18
 * @Description: 整体购物车：  List<Cart>
 * @date 2018/12/1114:58
 */
public class Cart implements Serializable{
    //商家名称
    private String sellerName;

    //商家id
    private String sellerId;

    //购物明细列表
    private List<TbOrderItem> orderItemList;


    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
