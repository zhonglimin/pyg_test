package com.pyg.page.service.impl;

import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.TbGoods;
import com.pyg.pojo.TbGoodsDesc;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huyy
 * @Title: ItemPageServiceImpl
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/516:06
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;


    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;


    @Value("${pageDir}")
    private String pageDir ;

    /**
     * 根据商品的spuid,生成对应的静态页面
     *
     * @param goodsId ： spuid
     */
    @Override
    public void genItemPage(Long goodsId) {

        try {
            //1.准备数据： tb_goods  tb_goodsDesc  tb_item   tb_item_cat表
            Map dataModel = new HashMap();
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);

            dataModel.put("goods",tbGoods);
            dataModel.put("goodsDesc",tbGoodsDesc);

            //准备面包屑
            String category1Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            String category2Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            String category3Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
            dataModel.put("category1Name",category1Name);
            dataModel.put("category2Name",category2Name);
            dataModel.put("category3Name",category3Name);


            //根据spu查找sku列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);

            //2. 加载模板
            Configuration configuration = freeMarkerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");


            //3.调用freemarker引擎，生成静态页面
            String path = pageDir + goodsId + ".html";
            FileWriter out = new FileWriter(new File(path));
            template.process(dataModel,out);

            //4.关闭流
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据商品id，删除静态页面
     *
     * @param goodsId
     */
    @Override
    public void deleteItemPage(Long goodsId) {
        String path = pageDir + goodsId + ".html";
        new File(path).delete();
    }
}
