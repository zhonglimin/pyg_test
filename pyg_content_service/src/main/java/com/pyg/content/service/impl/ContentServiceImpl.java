package com.pyg.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.content.service.ContentService;
import com.pyg.mapper.TbContentMapper;
import com.pyg.pojo.TbContent;
import com.pyg.pojo.TbContentExample;
import com.pyg.pojo.TbContentExample.Criteria;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        contentMapper.insert(content);
        redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
    }

    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());//页面提交，现在的 活动专区
        TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());//数据库的数据
        redisTemplate.boundHashOps("contentList").delete(tbContent.getCategoryId());//删除原来  轮播图
        contentMapper.updateByPrimaryKey(content);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbContent content = contentMapper.selectByPrimaryKey(id);
            redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
            contentMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<TbContent> findListByCategoryId(Long categoryId) {
        //优先从缓存中查询，如果缓存中有数据，直接返回，如果没有，需要查询数据库，缓存到redis中，在进行返回
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("contentList").get(categoryId);
        if (contentList == null || contentList.size() == 0) {
            TbContentExample example = new TbContentExample();
            Criteria criteria = example.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId);//外键条件
            criteria.andStatusEqualTo("1");//广告状态正常
            example.setOrderByClause("sort_order");

            contentList = contentMapper.selectByExample(example);
            redisTemplate.boundHashOps("contentList").put(categoryId, contentList);
            System.out.println("从数据库中查询");
        } else {
            System.out.println("从redis中查询");
        }

        return contentList;
    }

}
