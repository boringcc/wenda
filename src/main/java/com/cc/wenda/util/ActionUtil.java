package com.cc.wenda.util;

import com.cc.wenda.async.EventModel;
import com.cc.wenda.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ActionUtil {
    private static final Logger logger = LoggerFactory.getLogger(ActionUtil.class);

    public static Action getAction(EventModel model){
        Action action = new Action();
        action.setUserId(model.getActorId());
        action.setEntityId(model.getEntityId());
        action.setEntityType(model.getEntityType());
        action.setCreatedDate(new Date());
        action.setEventType(model.getType().getValue());
        return action;
    }
}
