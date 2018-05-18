package com.cc.wenda.interceptor;

import com.cc.wenda.dao.LoginTicketDAO;
import com.cc.wenda.dao.UserDAO;
import com.cc.wenda.model.HostHolder;
import com.cc.wenda.model.LoginTicket;
import com.cc.wenda.model.User;
import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class PassprotIntercepter implements HandlerInterceptor{

    @Autowired
    LoginTicketDAO loginTicketDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    HostHolder hostHolder;

    //看是不是有效的ticket
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if(httpServletRequest.getCookies() != null){
            for(Cookie cookie : httpServletRequest.getCookies()){
                if(cookie.getName().equals("ticket")){
                    ticket = cookie.getValue();
                    break;
                }
            }
        }

        if(ticket!= null){
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
            //如果ticket不存在或者超时，或者state不为0，则没有登录
            if(loginTicket == null ||loginTicket.getExpired().before(new Date())||loginTicket.getStatus() != 0 ){
                return true;
            }
            User user = userDAO.selectById(loginTicket.getUserId());
            hostHolder.setUser(user);
        }

        return true;
    }



    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if(modelAndView != null && hostHolder.getUser() != null ){
            modelAndView.addObject("user",hostHolder.getUser());
        }
    }


    //所有渲染完了
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
