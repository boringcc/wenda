package com.cc.wenda.controller;

import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventProducer;
import com.cc.wenda.async.EventType;
import com.cc.wenda.model.Comment;
import com.cc.wenda.model.EntityType;
import com.cc.wenda.model.HostHolder;
import com.cc.wenda.service.ActionService;
import com.cc.wenda.service.CommentService;
import com.cc.wenda.service.LikeService;
import com.cc.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LikeController {
    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;

    /**
     * 赞和踩的功能
     */
    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if(hostHolder.getUser() ==null){
            return WendaUtil.getJSONString(999);
        }
        Comment comment = commentService.getCommentById(commentId);

        //发送站内信和增加个人动态
        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setActorId(hostHolder.getUser().getId()).setEntityId(commentId).setType(EventType.LIKE)
                .setEntityType(EntityType.ENTITY_COMMENT).setEntityOwnerId(comment.getUserId())
                .setExt("questionId", String.valueOf(comment.getEntityId())));

        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT,commentId);
        return WendaUtil.getJSONString(0,String.valueOf(likeCount));

    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        long likeCount = likeService.disLike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));
    }








}
