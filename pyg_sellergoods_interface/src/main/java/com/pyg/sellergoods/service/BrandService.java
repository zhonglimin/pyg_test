package com.pyg.sellergoods.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {
    /*
    *@Desc 查询所有品牌列表数据
    *@param
    *@return java.util.List<com.pyg.pojo.TbBrand>
    **/
    public List<TbBrand> findAll();


    /*
    *@Desc 分页查询
    *@param pageNum 当前页码
    *@param pageSize 每页显示条数
    *@return entity.PageResult
    **/
    public PageResult findPage(int pageNum,int pageSize);

    /*
    *@Desc  添加品牌
    *@param brand web层传递过来的，web层封装的json对象
    *@return void
    **/
    public void add(TbBrand brand);

    /*
    *@Desc 查询某个品牌对象
    *@param id 主键
    *@return com.pyg.pojo.TbBrand
    **/
    public TbBrand findOne(Long id);


    public void update(TbBrand brand);

    /*
    *@Desc 批量删除
    *@param ids
    *@return void
    **/
    public void delete(Long[] ids);


    /*
    *@Desc 分页条件查询
    *@param brand web层封装的前端搜索条件
    *@param pageNum 当前页码
    *@param pageSize 每页显示条数
    *@return entity.PageResult
    **/
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    public  List<Map> selectOptionList();
}
