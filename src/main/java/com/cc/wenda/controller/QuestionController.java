package com.cc.wenda.controller;


import com.cc.wenda.async.EventModel;
import com.cc.wenda.async.EventProducer;
import com.cc.wenda.async.EventType;
import com.cc.wenda.model.*;
import com.cc.wenda.service.*;
import com.cc.wenda.util.JedisAdapter;
import com.cc.wenda.util.WendaUtil;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 增加问题
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(value = "/question/add",method = RequestMethod.POST)
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,@RequestParam("content") String content){
        try {
            Question question = new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCommentCount(0);
            question.setScore(getScore(2,0,0,0,0,0));
            question.setCreatedDate(new Date());
            if(hostHolder.getUser() == null){
                //question.setUserId(WendaUtil.ANONYMOUS_USERID);
                return WendaUtil.getJSONString(999);
            }else {
                question.setUserId(hostHolder.getUser().getId());
            }
            //增加题目
            if(questionService.addQuestion(question) > 0 ){
                //增加题目的异步处理
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION).setEntityType(EntityType.ENTITY_QUESTION)
                        .setActorId(question.getUserId()).setEntityId(question.getId()).setType(EventType.ADD_QUESTION)
                        .setExt("title", question.getTitle()).setExt("content", question.getContent()));
                return WendaUtil.getJSONString(0);
            }
        }catch (Exception e){
            logger.error("增加题目失败" + e.getMessage());
        }

        return WendaUtil.getJSONString(1,"失败");
    }

    /**
     * 进入问题详细页面
     * @param modle
     * @param qid
     * @return
     */
    @RequestMapping(value = "/question/{qid}")
    public String questionDetail(Model modle, @PathVariable("qid") int qid){
        Question question = questionService.getById(qid);
        modle.addAttribute("question",question);
        long likeCountSum = 0;
        //获取评论信息
        List<Comment> commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
        List<ViewObject> comments = new ArrayList<ViewObject>();
        for(Comment comment : commentList){
            ViewObject vo = new ViewObject();
            vo.set("comment",comment);
            if(hostHolder.getUser() ==null){
                vo.set("liked",0);
            }else {
                vo.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,comment.getId()));
            }
            long likeCount = likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId());
            likeCountSum+=likeCount;
            vo.set("likeCount",likeCount);
            vo.set("user",userService.getUser(comment.getUserId()));
            comments.add(vo);
        }
        //重写Compareable方法，按照点赞的人数来重新排序
        Collections.sort(comments, new Comparator<ViewObject>(){
            @Override
            public int compare(ViewObject c1, ViewObject c2) {
                if((Long)c1.get("likeCount") > (Long) c2.get("likeCount")){
                    return -1;
                }else if ((Long)c1.get("likeCount") < (Long) c2.get("likeCount")){
                    return 1;
                }else {
                    return 1;
                }
            }
        });
        modle.addAttribute("comments",comments);

        //获取用户关注信息
        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION,qid,20);
        for(Integer userId : users){
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if( u == null){
                continue;
            }
            vo.set("name",u.getName());
            vo.set("headUrl",u.getHeadUrl());
            vo.set("id",u.getId());
            followUsers.add(vo);
        }
        modle.addAttribute("followUsers",followUsers);
        if(hostHolder.getUser() != null){
            modle.addAttribute("followed",followService.isFollower(hostHolder.getUser().getId(),
                    EntityType.ENTITY_QUESTION,qid));
        }else {
            modle.addAttribute("followed",false);
        }



        //排序分值
        //QV是指Question的Views，即浏览量，用redis来进行存储
        String QV = "Q-" + qid;
        if(!jedisAdapter.isKeyExists(QV)) {
            jedisAdapter.set(QV, "0");
        }
        long Qviews = jedisAdapter.incr(QV);;     //问题浏览数
        int Qanswers = 0;    //问题回答数
        long Qscore = 0;     //问题关注数
        long Ascore = likeCountSum;     //回答踩赞数
        long QageInHours = 0;//题目发布时间差
        long Qupdated = 0;   //最新的回答时间
        Qanswers = commentList.size();
        Date nowDate = new Date();
        long now = nowDate.getTime();
        QageInHours = (now - question.getCreatedDate().getTime())/(1000*60*60);
        Qupdated = commentList.size()==0 ? 0 : ((now - commentList.get(0).getCreatedDate().getTime())/(1000*60*60)) ;
        Qscore = followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId());
        double score = getScore( Qviews, Qanswers, Qscore, Ascore, QageInHours, Qupdated);
        //将排序分值存储在数据库中
        questionService.updateScoreById(question.getId(),score);
        //System.out.println( "Qanswers " + Qanswers +" QageInHours "  +QageInHours +" Qupdated " +Qupdated
        //        + " Qviews " + Qviews + " Qscore " + Qscore + " Ascore " +  Ascore +" score " +score + " up "
        //        + up +" down " + down);
        //System.out.println((QageInHours +1) + "  "  + ((QageInHours - Qupdated)/2));
        return "detail";
    }

    public double getScore(long Qviews,int Qanswers,long Qscore,long Ascore,long QageInHours,long Qupdated){
        double up = ((Math.log(Qviews)) + ((Qanswers * Qscore)*2) + Ascore*2 )*5;
        double down = (QageInHours +5) - ((QageInHours - Qupdated)/2);
        double score = up/down;
        return score;
    }


}
