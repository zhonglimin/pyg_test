package com.pyg.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.*;
import com.pyg.pojo.*;
import com.pyg.pojo.TbGoodsExample.Criteria;
import com.pyg.pojogroup.Goods;
import com.pyg.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    @Transactional
    public void add(Goods goods) {
        //添加基本信息
        goods.getGoods().setAuditStatus("0");//刚添加的商品是未审核
        goodsMapper.insert(goods.getGoods());
        //添加扩展信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//将基本信息表的id作为扩展信息表的id
        goodsDescMapper.insert(goods.getGoodsDesc());
        //添加详情列表
        insertItem(goods);

    }

    private void insertItem(Goods goods){
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                //SPU+规格组合  meta20  樱粉金 6G+128G
                String title = goods.getGoods().getGoodsName();
                Map<String, String> spec = JSON.parseObject(item.getSpec(), Map.class);//{"网络":"移动4G","机身内存":"32G","颜色":"黑色"}
                for (String key : spec.keySet()) {
                    title += " " + spec.get(key);
                }
                item.setTitle(title);

                setItem(goods, item);
                itemMapper.insert(item);
            }
        } else {//没有启用规格，只有一条数据
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());
            setItem(goods, item);
            item.setPrice(goods.getGoods().getPrice());
            item.setNum(9999);
            item.setStatus("1");
            item.setIsDefault("1");
            item.setSpec("{}");
            itemMapper.insert(item);
        }
    }

    private void setItem(Goods goods, TbItem item) {
        //获取图片列表中第一个图片对象的url
        List<Map> images = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);//[{"color":"红色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINKADo__AAjlKdWCzvg874.jpg"},{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINyAQAXHAAgawLS1G5Y136.jpg"}]
        if (images.size() > 0) {
            item.setImage((String) images.get(0).get("url"));
        }
        //goods表中有第三级分类的id
        item.setCategoryid(goods.getGoods().getCategory3Id());
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(goods.getGoods().getId());//外键
        item.setSellerId(goods.getGoods().getSellerId());
        item.setBrand(brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId()).getName());
        item.setCategory(itemCatMapper.selectByPrimaryKey(item.getCategoryid()).getName());
        item.setSeller(sellerMapper.selectByPrimaryKey(item.getSellerId()).getNickName());
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //修改基本信息
        goods.getGoods().setAuditStatus("0");//修改后的商品需要重新审核
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //修改扩展信息
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

        //修改SKU列表
        //删除数据库中原来的数据
        TbItemExample example=new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());//外键条件
        itemMapper.deleteByExample(example);
        //重新关联页面提交的数据
        insertItem(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //查询基本信息
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        //查询扩展信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);
        //查询SKU列表
        TbItemExample example=new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);//外键条件
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);

        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setIsDelete("1");//默认值是null是正常，1代表删除
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }


        }
        //查询没被删除的商品
        criteria.andIsDeleteIsNull();

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            //查询是基本信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

    /**
     * 根据spu的id列表和状态，查找sku列表数据
     *
     * @param ids
     * @param status
     * @return
     */
    @Override
    public List<TbItem> findItemListByGoodsIdsandStatus(Long[] ids, String status) {

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //状态
        criteria.andStatusEqualTo(status);
        criteria.andGoodsIdIn(Arrays.asList(ids));
        return itemMapper.selectByExample(example);
    }

}
