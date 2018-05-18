package com.cc.wenda.controller;

import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventProducer;
import com.cc.wenda.async.EventType;
import com.cc.wenda.model.Comment;
import com.cc.wenda.model.EntityType;
import com.cc.wenda.model.HostHolder;
import com.cc.wenda.service.CommentService;
import com.cc.wenda.service.QuestionService;
import com.cc.wenda.service.SensitiveService;
import com.cc.wenda.service.UserService;
import com.cc.wenda.util.WendaUtil;
import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;


/**
 * 评论
 */
@Controller
public class CommentController {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    EventProducer eventProducer;

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);


    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                //comment.setUserId(WendaUtil.ANONYMOUS_USERID);
                //没有登录则先跳往登录页面
                 return "redirect:/reglogin";
            }
            //设置评论的属性
            comment.setCreatedDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.addComment(comment);

            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);

            //异步处理评论事件，添加feed，在feedHandler中
            eventProducer.fireEvent(new EventModel(EventType.COMMENT)
                    .setActorId(comment.getUserId()).setEntityType(EntityType.ENTITY_COMMENT)
                    .setEntityId(questionId).setType(EventType.COMMENT));


        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId;
    }

}
