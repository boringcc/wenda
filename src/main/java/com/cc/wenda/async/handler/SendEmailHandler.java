package com.cc.wenda.async.handler;

import com.cc.wenda.async.EventHandler;
import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventType;
import com.cc.wenda.util.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendEmailHandler implements EventHandler{
    private static final Logger logger = LoggerFactory.getLogger(SendEmailHandler.class);

    @Autowired
    MailSender mailSender;

    @Override
    public void doHandle(EventModel model) {
        // xxxx判断发现这个用户登陆异常
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", model.getExt("username"));
        mailSender.sendWithHTMLTemplate(model.getExt("email"), "登陆IP异常", "mails/login_exception.html", map);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.SEND_EMAIL);
    }
}