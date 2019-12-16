package com.pyg.search.service.impl;

import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

/**
 * @author huyy
 * @Title: SolrDeleteListener
 * @ProjectName pyg_parent18
 * @Description: 删除solr索引库的监听类
 * @date 2018/12/814:47
 */
@Component
public class SolrDeleteListener implements MessageListener{

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {


        try {
            //1.获取消息
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long [] ids = (Long[]) objectMessage.getObject();


            //2.调用业务
            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
