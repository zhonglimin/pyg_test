package com.pyg.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author huyy
 * @Title: SearchController
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/116:03
 */
@RestController
@RequestMapping("/itemSearch")
public class SearchController {

    @Reference
    private ItemSearchService itemSearchService;


    /**
     * 商品搜索
     * @param searchMap
     * @return
     */
    @RequestMapping("/search")
    public Map search(@RequestBody Map searchMap){
        return itemSearchService.search(searchMap);
    }
}
