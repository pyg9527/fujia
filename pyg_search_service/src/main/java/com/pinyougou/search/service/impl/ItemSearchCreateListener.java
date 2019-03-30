package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class ItemSearchCreateListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage m= (ObjectMessage) message;
            Long[] ids = (Long[]) m.getObject();
            itemSearchService.importItemToSolr(ids);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
