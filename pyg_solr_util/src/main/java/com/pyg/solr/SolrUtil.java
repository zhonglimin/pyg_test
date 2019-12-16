package com.pyg.solr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author huyy
 * @Title: SolrUtil
 * @ProjectName pyg_parent18
 * @Description: 将数据库中tb_item表中的数据导入到solr索引库
 * @date 2018/12/115:03
 */
@Component
public class SolrUtil {


    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;


    /**
     * 将数据库中tb_item表中的数据导入到solr索引库
     */
    public void importItemList(){
        //1. 从tb_item表取数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //已审核
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);

        for (TbItem tbItem : itemList) {
            System.out.println(tbItem.getTitle());

            Map map = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
        }
        //2. 调用solrTemplate的api，导入solr索引库
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }


    public static void main(String[] args) {

        //启动spring容器
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/application*.xml");
        //获取javabean
        SolrUtil solrUtil = applicationContext.getBean(SolrUtil.class);

        // 调用方法
        solrUtil.importItemList();
    }
}
