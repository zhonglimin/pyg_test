package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @author huyy
 * @Title: PageListener
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/815:16
 */
@Component
public class PageListener implements MessageListener{

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {

        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();

            //生成静态页面
            for (Long goodsId : ids) {
                itemPageService.genItemPage(goodsId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
