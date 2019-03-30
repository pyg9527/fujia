package com.pinyougou.user.service.impl;

import com.pinyougou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

@Service
public class UserServiceListener implements MessageListener {

    @Autowired
    private UserService userService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage m= (ObjectMessage) message;
        try {
            String phone = (String) m.getObject();
            userService.sendSms(phone);
            System.out.println("收到消息了");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
