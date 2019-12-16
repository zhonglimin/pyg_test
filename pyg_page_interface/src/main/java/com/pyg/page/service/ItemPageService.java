package com.pyg.page.service;

/**
 * @author huyy
 * @Title: ItemPageService
 * @ProjectName pyg_parent18
 * @Description: 静态页面生成的接口
 * @date 2018/12/516:05
 */
public interface ItemPageService {


    /**
     *
     * 根据商品的spuid,生成对应的静态页面
     * @param goodsId ： spuid
     */
    public void genItemPage(Long goodsId);

    /**
     * 根据商品id，删除静态页面
     * @param goodsId
     */
    void deleteItemPage(Long goodsId);
}
