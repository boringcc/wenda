package com.cc.wenda.async.handler;

import com.cc.wenda.async.EventHandler;
import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventType;
import com.cc.wenda.model.Action;
import com.cc.wenda.model.EntityType;
import com.cc.wenda.model.Message;
import com.cc.wenda.model.User;
import com.cc.wenda.service.ActionService;
import com.cc.wenda.service.MessageService;
import com.cc.wenda.service.UserService;
import com.cc.wenda.util.ActionUtil;
import com.cc.wenda.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class FollowHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;


    @Autowired
    ActionService actionService;

    @Override
    public void doHandle(EventModel model) {
        //站内信部分
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());

        if (model.getEntityType() == EntityType.ENTITY_QUESTION) {
            message.setContent("用户" + user.getName()
                    + "关注了你的问题,http://127.0.0.1:8080/question/" + model.getEntityId());
        } else if (model.getEntityType() == EntityType.ENTITY_USER) {
            message.setContent("用户" + user.getName()
                    + "关注了你,http://127.0.0.1:8080/user/" + model.getActorId());
        }
        messageService.addMessage(message);
        //个人动态部分
        actionService.addAction(ActionUtil.getAction(model));
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.FOLLOW);
    }


}
