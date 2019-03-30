package com.pinyougou.user.service.impl;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.Date;
import java.util.List;

import com.pinyougou.utils.HttpClientUtil;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.swing.text.html.FormSubmitEvent;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Component
@Service(timeout = 10000)
public class UserServiceImpl implements UserService {

    @Autowired
    private TbUserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQQueue queueDestination;
    @Override
    public void sendSms(String phone) {
        //使用随机工具产生六位验证码
        String code = RandomStringUtils.randomNumeric(6);
        System.out.println("===随机产生的验证码====="+code);
        //将验证码放入redis中,注意通过phone作为key，唯一不重复。
        redisTemplate.boundHashOps("phoneCode").put(phone,code);
        String code1 = (String) redisTemplate.boundHashOps("phoneCode").get(phone);
        System.out.println("==存入redis中的验证码==="+code1);
        //可以是设置验证码在redis中清除的时间        //redisTemplate.boundHashOps("test").expire(5000, TimeUnit.MILLISECONDS);
        //用httpclientutil进行跳转
       /* HttpClientUtil util = new HttpClientUtil("http://localhost:9002/sms.do?phone=" + phone + "&code=" + code);
        try {
            util.get();
            System.out.println(util.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
       //用activeMq进行发送短信
        jmsTemplate.send(queueDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage m = session.createMapMessage();
                m.setString("phone",phone);
                m.setString("code",code);
                return m;
            }
        });
    }

    //验证验证码是否正确
    @Override
    public boolean checkCode(String phone, String inputCode) {
        String phoneCode = (String) redisTemplate.boundHashOps("phoneCode").get(phone);
        System.out.println(phoneCode);
        //首先判断得到的redis中时候为null，
        if(phoneCode!=null && inputCode.equals(phoneCode)){
            return true;
        }
        return false;
    }
    /**
     * 增加
     */
    @Override
    public void add(TbUser user) {
        user.setCreated(new Date());
        user.setUpdated(new Date());

        /*BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
        user.setPassword( bCryptPasswordEncoder.encode(user.getPassword()));*/
        //MD5加密
        String password = DigestUtils.md5Hex(user.getPassword());//对密码加密，apache框架的
        user.setPassword(password);
        userMapper.insert(user);
    }

    /**
     * 查询全部
     */
    @Override
    public List<TbUser> findAll() {
        return userMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }



    /**
     * 修改
     */
    @Override
    public void update(TbUser user) {
        userMapper.updateByPrimaryKey(user);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbUser findOne(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            userMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbUser user, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbUserExample example = new TbUserExample();
        Criteria criteria = example.createCriteria();

        if (user != null) {
            if (user.getUsername() != null && user.getUsername().length() > 0) {
                criteria.andUsernameLike("%" + user.getUsername() + "%");
            }
            if (user.getPassword() != null && user.getPassword().length() > 0) {
                criteria.andPasswordLike("%" + user.getPassword() + "%");
            }
            if (user.getPhone() != null && user.getPhone().length() > 0) {
                criteria.andPhoneLike("%" + user.getPhone() + "%");
            }
            if (user.getEmail() != null && user.getEmail().length() > 0) {
                criteria.andEmailLike("%" + user.getEmail() + "%");
            }
            if (user.getSourceType() != null && user.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + user.getSourceType() + "%");
            }
            if (user.getNickName() != null && user.getNickName().length() > 0) {
                criteria.andNickNameLike("%" + user.getNickName() + "%");
            }
            if (user.getName() != null && user.getName().length() > 0) {
                criteria.andNameLike("%" + user.getName() + "%");
            }
            if (user.getStatus() != null && user.getStatus().length() > 0) {
                criteria.andStatusLike("%" + user.getStatus() + "%");
            }
            if (user.getHeadPic() != null && user.getHeadPic().length() > 0) {
                criteria.andHeadPicLike("%" + user.getHeadPic() + "%");
            }
            if (user.getQq() != null && user.getQq().length() > 0) {
                criteria.andQqLike("%" + user.getQq() + "%");
            }
            if (user.getIsMobileCheck() != null && user.getIsMobileCheck().length() > 0) {
                criteria.andIsMobileCheckLike("%" + user.getIsMobileCheck() + "%");
            }
            if (user.getIsEmailCheck() != null && user.getIsEmailCheck().length() > 0) {
                criteria.andIsEmailCheckLike("%" + user.getIsEmailCheck() + "%");
            }
            if (user.getSex() != null && user.getSex().length() > 0) {
                criteria.andSexLike("%" + user.getSex() + "%");
            }

        }

        Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
