package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @author huyy
 * @Title: PageDeleteListener
 * @ProjectName pyg_parent18
 * @Description: 商品删除，删除静态页的监听类
 * @date 2018/12/815:36
 */
@Component
public class PageDeleteListener implements MessageListener{

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();

            //删除静态页面
            for (Long goodsId : ids) {
                itemPageService.deleteItemPage(goodsId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
