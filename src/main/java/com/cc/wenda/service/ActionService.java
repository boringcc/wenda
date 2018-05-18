package com.cc.wenda.service;

import com.cc.wenda.controller.HomeController;
import com.cc.wenda.dao.ActionDAO;
import com.cc.wenda.model.Action;
import com.cc.wenda.model.Comment;
import com.cc.wenda.util.ActionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class ActionService {

    private static final Logger logger = LoggerFactory.getLogger(ActionService.class);

    @Autowired
    ActionDAO actionDAO;


    public int addAction(Action action) {
        return actionDAO.addAction(action);
    }

    public List<Action> selectActionByUID(int userId){
        return actionDAO.selectActionByUID(userId);
    }

    public List<Action> selectActionsByUID(int userId,int offset,int limit){
        return actionDAO.selectActionsByUID(userId,offset,limit);
    }

}
