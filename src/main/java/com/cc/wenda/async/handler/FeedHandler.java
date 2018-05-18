package com.cc.wenda.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.cc.wenda.async.EventHandler;
import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventType;
import com.cc.wenda.model.*;
import com.cc.wenda.service.*;
import com.cc.wenda.util.ActionUtil;
import com.cc.wenda.util.JedisAdapter;
import com.cc.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FeedHandler implements EventHandler{

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    FeedService feedService;

    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    ActionService actionService;

    private String buildFeedData(EventModel model){
        Map<String,String > map = new HashMap<String, String>();

        User actor = userService.getUser(model.getActorId());
        if ( actor == null){
            return null;
        }
        map.put("userId",String.valueOf(actor.getId()));
        map.put("userHead",actor.getHeadUrl());
        map.put("userName",actor.getName());
        if(model.getType() == EventType.COMMENT
                || (model.getType() == EventType.FOLLOW && model.getEntityType() == EntityType.ENTITY_QUESTION)){
            Question question = questionService.getById(model.getEntityId());
            if(question == null){
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));
            map.put("questionTitle",question.getTitle());
            return JSONObject.toJSONString(map);
        }

        if(model.getType() == EventType.LIKE){
            Comment comment = commentService.getCommentById(model.getEntityId());
            if(comment == null){
                return null;
            }
            map.put("commetnUserId",String.valueOf(comment.getUserId()));
            map.put("commentUserName",String.valueOf(userService.getUser(comment.getUserId()).getName()));
            map.put("content",comment.getContent().substring(0,5)+"。。。。。。");
            return JSONObject.toJSONString(map);
        }

        return null;
    }


    @Override
    public void doHandle(EventModel model) {
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setUserId(model.getActorId());
        feed.setType(model.getType().getValue());
        feed.setData(buildFeedData(model));
        if(feed.getData() == null){
            return;
        }
        feedService.addFeed(feed);
        //给事件的粉丝推
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER,model.getActorId(),Integer.MAX_VALUE);

        followers.add(0);
        for(int follower : followers){
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);
            jedisAdapter.lpush(timelineKey,String.valueOf(feed.getId()));
        }
        //增加回答问题的个人动态
        if(model.getType()== EventType.COMMENT){
            actionService.addAction(ActionUtil.getAction(model));
        }

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[] {EventType.COMMENT,EventType.FOLLOW,EventType.LIKE});
    }
}
