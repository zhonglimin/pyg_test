package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huyy
 * @Title: ItemSearchServiceImpl
 * @ProjectName pyg_parent18
 * @Description: 搜索的服务实现
 * @date 2018/12/115:46
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;


    @Autowired
    private RedisTemplate redisTemplate;


     /* //1.解析出来关键字
        String keywords = (String) searchMap.get("keywords");

        //2.封装查询条件
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);


        //3.调用solrTemplate的api查询
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);

        List<TbItem> itemList = scoredPage.getContent();
        resultMap.put("rows",itemList);*/


    /**
     * 搜索的方法
     *
     * @param searchMap ： map类型 keywords关键词
     * @return List<TbItem></>  map封装
     */
    @Override
    public Map search(Map searchMap) {

        Map<String,Object> resultMap = new HashMap();

        //处理空格
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        //1.高亮列表查询
        Map highMap = searchHighlightList(searchMap);
        resultMap.putAll(highMap);

        //2.根据关键字找分类列表
        Map categoryMap = searchCategoryList(searchMap);
        resultMap.putAll(categoryMap);

        //3. 根据第一个商品分类，查找品牌列表和规格列表
        Object category = searchMap.get("category");
        if(category == null || "".equals(category)){
            //用户未传递的有商品分类
            List<String> categoryList = (List<String>) categoryMap.get("categoryList");
            category = categoryList.get(0);
        }
        Map brandAndSpecMap = searchBrandAndSpecByCategory((String) category);
        resultMap.putAll(brandAndSpecMap);

        //4.返回结果
        return resultMap;
    }


    /**
     * 根据商品分类，查找品牌列表和规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecByCategory(String category) {
        Map resultMap = new HashMap();
        //根据商品分类名称-------------------类型模板
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //根据类型模板id--------------------品牌列表
        Object brandList = redisTemplate.boundHashOps("brandList").get(typeId);

        //根据类型模板id---------------------规格列表
        Object specList = redisTemplate.boundHashOps("specList").get(typeId);

        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);
        return resultMap;
    }

    /**
     * 根据关键字找分类列表 : 分组查询
     * @param searchMap
     * @return
     */
    private Map searchCategoryList(Map searchMap) {
        Map categoryMap = new HashMap();
        List<String> categoryList = new ArrayList<>();

        //1.获取关键字条件
        String keywords = (String) searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords").is(keywords);

        //2.组装分组查询条件
        Query query = new SimpleQuery();
        query.addCriteria(criteria);
        GroupOptions groupOptions = new GroupOptions();
        //分组的域
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //3.进行分组查询
        //得到分组页
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");

        //获取分组入口
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<TbItem> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue();
            System.out.println(groupValue);
            categoryList.add(groupValue);
        }

        categoryMap.put("categoryList",categoryList);
        return categoryMap;

    }

    /**
     * 高亮列表查询的方法
     * @param searchMap
     * @return  resultMap
     *
     */
    private Map searchHighlightList(Map searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        //高亮查询
        //3.调用solrTemplate的api查询
        HighlightQuery highlightQuery = new SimpleHighlightQuery();

        //操作方式：默认是并，设置交集
        highlightQuery.setDefaultOperator(Query.Operator.AND);

        //设置高亮选项
        HighlightOptions highlightOptions = new HighlightOptions();

        //高亮的域和前缀和后缀
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        highlightQuery.setHighlightOptions(highlightOptions);

        //关键字条件
        String keywords = (String) searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        highlightQuery.addCriteria(criteria);

        //---------------------------------过滤条件的构建：分类 品牌 规格-----------------------------------------
        //2.1 分类的过滤
        Object category = searchMap.get("category");
        if(category != null && StringUtils.isNotEmpty((String)category)){
            FilterQuery categoryFilter = new SimpleFilterQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(category);
            categoryFilter.addCriteria(categoryCriteria);
            highlightQuery.addFilterQuery(categoryFilter);
        }

        //2.2 品牌的过滤
        Object brand = searchMap.get("brand");
        if(brand != null && StringUtils.isNotEmpty((String)brand)){
            FilterQuery brandFilter = new SimpleFilterQuery();
            Criteria brandCriteria = new Criteria("item_brand").is(brand);
            brandFilter.addCriteria(brandCriteria);
            highlightQuery.addFilterQuery(brandFilter);
        }

        //2.3 规格过滤条件
        Object spec = searchMap.get("spec");
        if(spec != null ){
            Map<String,String> specMap = (Map<String, String>) spec;

            for (String key : specMap.keySet()) {

                FilterQuery specFilter = new SimpleFilterQuery();
                //TODO  需要从specMap中获取对应的value值
                Criteria specCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                specFilter.addCriteria(specCriteria);
                highlightQuery.addFilterQuery(specFilter);
            }
        }

        //2.4 价格区间过滤
        Object price = searchMap.get("price");
        if(price != null && StringUtils.isNotEmpty((String) price)){
            String priceSting = (String) price;
            String[] splitPrice = priceSting.split("-");
            //500-1000
            if(!"0".equals(splitPrice[0])){
                //设置最低价格的过滤条件
                FilterQuery lowPriceFilter = new SimpleFilterQuery();
                Criteria lowCriteria = new Criteria("item_price").greaterThanEqual(splitPrice[0]);
                lowPriceFilter.addCriteria(lowCriteria);
                highlightQuery.addFilterQuery(lowPriceFilter);
            }

            if(!"*".equals(splitPrice[1])){
                //设置最高价格的限制
                FilterQuery highPriceFilter = new SimpleFilterQuery();
                Criteria highCriteria = new Criteria("item_price").lessThanEqual(splitPrice[1]);
                highPriceFilter.addCriteria(highCriteria);
                highlightQuery.addFilterQuery(highPriceFilter);
            }

        }

        //2.5 分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");

        //开始索引  偏移量
        highlightQuery.setOffset((pageNo - 1) * pageSize);
        highlightQuery.setRows(pageSize);


        //2.6  排序处理
        Object sortField = searchMap.get("sortField");
        Object sortS = searchMap.get("sort");
        if(sortField != null && StringUtils.isNotEmpty((String)sortField)){

            if("ASC".equals(sortS)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_" + sortField);
                highlightQuery.addSort(sort);
            }
            if("DESC".equals(sortS)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_" + sortField);
                highlightQuery.addSort(sort);
            }
        }

        //---------------------------------高亮列表查询----------------------------------------------------------
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);

        //获取高亮信息，进行替换
        //获取高亮的入口集合
        List<HighlightEntry<TbItem>> highlightEntries = highlightPage.getHighlighted();
        for (HighlightEntry<TbItem> highlightEntry : highlightEntries) {
            TbItem tbItem = highlightEntry.getEntity();

            //可以设置多个域： item_title item_brand
            List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
            for (HighlightEntry.Highlight highlight : highlights) {
                //一个域可以多值
                List<String> snipplets = highlight.getSnipplets();
                String hiTitle = snipplets.get(0);
                System.out.println(hiTitle);
                //高亮的替换
                tbItem.setTitle(hiTitle);
            }
        }
        List<TbItem> content = highlightPage.getContent();
        resultMap.put("rows",content);
        //总记录数
        long totalElements = highlightPage.getTotalElements();
        resultMap.put("total",totalElements);

        //总页数
        int totalPages = highlightPage.getTotalPages();
        resultMap.put("totalPages",totalPages);
        return resultMap;
    }



    /**
     * 根据itemList导入solr索引库
     *
     * @param itemList
     */
    @Override
    public void importItemList(List itemList) {
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    /**
     * 根据传递的spu列表，删除solr索引库
     *
     * @param goodsIds
     */
    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
