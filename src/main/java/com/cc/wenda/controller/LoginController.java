package com.cc.wenda.controller;


import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventProducer;
import com.cc.wenda.async.EventType;
import com.cc.wenda.service.UserService;
import com.cc.wenda.service.WendaService;
import com.cc.wenda.util.JedisAdapter;
import com.cc.wenda.util.MailSender;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext;
import com.sun.xml.internal.ws.api.pipe.NextAction;
import org.apache.commons.lang.StringUtils;
import org.aspectj.apache.bcel.classfile.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    MailSender mailSender;

    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.POST})
    public String reg(Model model, @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam("email") String email,
                      @RequestParam("code") String code,
                      @RequestParam("next") String next,
                      @RequestParam(value="rememberme", defaultValue = "false") boolean rememberme,
                      HttpServletResponse response) {
        if(jedisAdapter.isKeyExists(email)){
            if(jedisAdapter.getValue(email).equals(code)){
                try {
                    Map<String, Object> map = userService.register(username, password);
                    if (map.containsKey("ticket")) {
                        Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                        cookie.setPath("/");
                        if (rememberme) {
                            cookie.setMaxAge(3600*24*5);
                        }
                        response.addCookie(cookie);
                        if (StringUtils.isNotBlank(next)) {
                            return "redirect:" + next;
                        }
                        return "redirect:/";
                    } else {
                        model.addAttribute("msg", map.get("msg"));
                        return "login";
                    }
                } catch (Exception e) {
                    logger.error("注册异常" + e.getMessage());
                    model.addAttribute("msg", "服务器错误");
                    return "login";
                }
            }else {
                model.addAttribute("msg", "验证码错误");
                return "login";
            }
        }else {
            model.addAttribute("msg", "验证码失效");
            return "login";
        }

    }

    @RequestMapping(path = {"/sendCode"})
    @ResponseBody
    public int sendCode(HttpServletRequest request) {
        String code = String.valueOf((int)((Math.random()*9+1)*1000));
        String email = request.getParameter("email");

        jedisAdapter.setEx(email,300,code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code",code );
        mailSender.sendWithHTMLTemplate(email, "注册验证码", "mails/login_codeemial.html", map);
        return 1;
    }


    @RequestMapping(path = {"/login/"}, method = {RequestMethod.POST})
    public String login(Model model, @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value="next", required = false) String next,
                        @RequestParam(value="rememberme", defaultValue = "false") boolean rememberme,
                        HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.login(username, password);
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                if (rememberme) {
                    cookie.setMaxAge(3600*24*5);
                }
                response.addCookie(cookie);

                //eventProducer.fireEvent(new EventModel(EventType.LOGIN)
                //        .setExt("username", username).setExt("email", "13548627468@163.com")
                //        .setActorId((int)map.get("userId")));

                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/";
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }

        } catch (Exception e) {
            logger.error("登陆异常 " + e.getMessage());
            e.printStackTrace();
            return "login";
        }
    }

    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }


    @RequestMapping(value = "/reglogin",method = RequestMethod.GET)
    public String reg(Model model,@RequestParam(value = "next",required = false) String next){
        model.addAttribute("next",next);
        return "login";
    }

}
