package com.cc.wenda.service;

import com.cc.wenda.dao.LoginTicketDAO;
import com.cc.wenda.dao.UserDAO;
import com.cc.wenda.model.LoginTicket;
import com.cc.wenda.model.User;
import com.cc.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    public User selectByName(String name) {
        return userDAO.selectByName(name);
    }

    public User getUser(int id) {
        return userDAO.selectById(id);
    }

    public Map<String,Object> register(String username,String password){
        Map<String,Object> map  = new HashMap<String,Object>();
        if(StringUtils.isBlank(username)||StringUtils.isBlank(password)){
            map.put("msg","用户名或密码不能为空");
        }
        User user = userDAO.selectByName(username);
        if(user!=null){
            map.put("msg","用户名已经被注册");
        }
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setPassword(WendaUtil.MD5(password + user.getSalt()));
        userDAO.addUser(user);
        return map;
    }

    public Map<String,Object> login(String username,String password){
        Map<String,Object> map  = new HashMap<String,Object>();
        if(StringUtils.isBlank(username)||StringUtils.isBlank(password)){
            map.put("msg","用户名或密码不能为空");
        }
        User user = userDAO.selectByName(username);
        if(user == null){
            map.put("msg","用户名不存在");
            return map;
        }
        if(!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码错误");
            return map;
        }
        String ticket = addLoginTicket(user.getId());
        map.put("userId",user.getId());
        //登录
        map.put("ticket",ticket);
        return map;
    }

    public String addLoginTicket(int userId){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date now = new Date();
        now.setTime(3600 * 24 *100 + now.getTime());
        loginTicket.setExpired(now);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        loginTicketDAO.addTicket(loginTicket);
        return loginTicket.getTicket();
    }

    //将ticket的状态设置为1就可以使其失效
    public void logout(String ticket){
        loginTicketDAO.updateStatus(ticket,1);
    }

}
