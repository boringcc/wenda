package com.cc.wenda.async.handler;

import com.cc.wenda.async.EventHandler;
import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventType;
import com.cc.wenda.controller.CommentController;
import com.cc.wenda.model.Action;
import com.cc.wenda.model.EntityType;
import com.cc.wenda.service.ActionService;
import com.cc.wenda.service.SearchService;
import com.cc.wenda.util.ActionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class AddQuestionHandler implements EventHandler{
    private static final Logger logger = LoggerFactory.getLogger(AddQuestionHandler.class);

    @Autowired
    SearchService searchService;

    @Autowired
    ActionService actionService;

    @Override
    public void doHandle(EventModel model) {
        //增加solr的索引
        try {
            searchService.indexQuestion(model.getEntityId(),model.getExt("title")
                                        ,model.getExt("content"));
        }catch (Exception e){
            logger.error("增加题目索引失败"+ e.getMessage());
        }

        //个人动态增加
        actionService.addAction(ActionUtil.getAction(model));
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.ADD_QUESTION);
    }
}
