package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * @author huyy
 * @Title: SolrImportListener
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/718:21
 */
@Component
public class SolrImportListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;
        try {

            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);

            //调用业务方法，导入solr索引库
            itemSearchService.importItemList(itemList);

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
