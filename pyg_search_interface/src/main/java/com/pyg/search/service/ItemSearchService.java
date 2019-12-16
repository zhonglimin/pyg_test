package com.pyg.search.service;

import java.util.List;
import java.util.Map;

/**
 * @author huyy
 * @Title: ItemSearchService
 * @ProjectName pyg_parent18
 * @Description: 搜索的接口
 * @date 2018/12/115:42
 */
public interface ItemSearchService {

    /**
     * 搜索的方法
     * @param searchMap ： map类型 keywords关键词
     * @return List<TbItem></>  map封装
     */
    public Map search(Map searchMap);


    /**
     * 根据itemList导入solr索引库
     * @param itemList
     */
    public void importItemList(List itemList);


    /**
     * 根据传递的spu列表，删除solr索引库
     * @param goodsIds
     */
    public void deleteByGoodsIds(List goodsIds);
}
