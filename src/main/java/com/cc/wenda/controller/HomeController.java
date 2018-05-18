package com.cc.wenda.controller;

import com.cc.wenda.async.EventType;
import com.cc.wenda.model.*;
import com.cc.wenda.service.*;
import com.cc.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    ActionService actionService;

    private List<ViewObject> getQuestions(int userId, int offset, int limit,int type) {
        List<Question> questionList = new ArrayList<>();
        if(type == WendaUtil.SEARCH_QUESTION_SCORE) {
            //采用分数来获取问题列表达到排序的效果
            questionList = questionService.selectScoreQuestions(userId, offset, limit);
        }else {
            //按时间排序，最近的牌前面
            questionList = questionService.getLatestQuestions(userId, offset, limit);
        }

        List<ViewObject> vos = new ArrayList<>();
        for (Question question : questionList) {
            ViewObject vo = new ViewObject();
            vo.set("question", question);
            vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            vo.set("user", userService.getUser(question.getUserId()));
            vos.add(vo);
        }
        return vos;
    }

    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        model.addAttribute("vos", getQuestions(0, 0, 10,WendaUtil.SEARCH_QUESTION_SCORE));
        return "index";
    }

    @RequestMapping(path = "/loadMoreQuestion",method = RequestMethod.POST)
    @ResponseBody
    public String loadMoreQuestion(Model model,HttpServletRequest request){
        int offset =  Integer.parseInt(request.getParameter("offset"));
        int sortType =  Integer.parseInt(request.getParameter("sortType"));
        int userId =  Integer.parseInt(request.getParameter("userId") != null?request.getParameter("userId") : "0" );

        Map<String,Object> questions = new HashMap<String, Object>();
        List<Question> questionList = new ArrayList<>();
        if(sortType == WendaUtil.SEARCH_QUESTION_SCORE) {
            questionList = questionService.selectScoreQuestions(userId, offset, 5);
        }else {
            questionList = questionService.getLatestQuestions(userId, offset, 5);
        }
        List<Long>  followCount = new ArrayList<>();
        List<User> users =  new ArrayList<User>();
        int count = 0;
        for (Question question : questionList) {
            followCount.add(followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            users.add(userService.getUser(question.getUserId()));
            count ++ ;
        }
        questions.put("question", questionList);
        questions.put("followCount",followCount);
        questions.put("users",users);
        questions.put("count",count);
        return WendaUtil.getJSONString(1,questions);
    }





    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model, @PathVariable("userId") int userId) {

        List<ViewObject> viewObjects = getActions(userId,0,10);
        model.addAttribute("vos",viewObjects);

        User user = userService.getUser(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        if (hostHolder.getUser() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        model.addAttribute("type",1); //1代表个人动态页面

        return "profile";
    }


    @RequestMapping(path = "/loadMoreActions",method = RequestMethod.POST)
    @ResponseBody
    public String loadMoreSelfInfo(Model model,HttpServletRequest request){
        Map<String,Object> Actions = new HashMap<String, Object>();
        int offset =  Integer.parseInt(request.getParameter("offset"));  //偏移量分页查询用的
        int userId =  Integer.parseInt(request.getParameter("userId"));

        List<ViewObject> moreActions = getActions(userId,offset,5);
        List<String> contents = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        List<Date> createdDates = new ArrayList<>();
        for(ViewObject vo : moreActions){
            contents.add(String.valueOf(vo.get("content")));
            urls.add(String.valueOf(vo.get("entityUrl")));
            createdDates.add((Date)vo.get("createdDate"));
        }
        Actions.put("contents",contents);
        Actions.put("urls",urls);
        Actions.put("createdDates",createdDates);
        Actions.put("mycount",urls.size());
        return WendaUtil.getJSONString(1,Actions);
    }

    @RequestMapping(path = {"/{userId}/problems"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String problems(Model model, @PathVariable("userId") int userId) {

        model.addAttribute("vos", getQuestions(userId, 0, 10,WendaUtil.SEARCH_QUESTION_TIME));
        User user = userService.getUser(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        if (hostHolder.getUser() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        model.addAttribute("type",2); //2代表个人问题页面
        return "profile";
    }





    /**
     * 得到个人动态数据，按照时间排序
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    private List<ViewObject> getActions(int userId, int offset, int limit) {
        List<Action> actionList = actionService.selectActionsByUID(userId,offset,limit);

        List<ViewObject> vos = new ArrayList<>();
        for (Action action : actionList) {
            ViewObject vo = new ViewObject();
            //如果是实体为人的话，就是关注了某人
            String content = null;
            String entityUrl = null;
            //测试
            int type = action.getEventType();
            int type2 = EventType.LIKE.getValue();
            if(action.getEntityType() == EntityType.ENTITY_USER){
                if(action.getEventType() == EventType.FOLLOW.getValue()){
                    content = "关注了用户：" + userService.getUser(action.getEntityId()).getName();
                    entityUrl =  "http://localhost:8081/user/" + action.getEntityId();

                }
            }
            //如果实体对象为问题
            if(action.getEntityType() == EntityType.ENTITY_QUESTION){
                if(action.getEventType() == EventType.FOLLOW.getValue()){
                    content = "关注了问题：" + questionService.getById(action.getEntityId()).getTitle();
                    entityUrl = "http://localhost:8081/question/" + action.getEntityId();
                }
                if(action.getEventType() == EventType.ADD_QUESTION.getValue()){
                    content = "发表了问题：" + questionService.getById(action.getEntityId()).getTitle();
                    entityUrl = "http://localhost:8081/question/" + action.getEntityId();
                }
            }
            //如果实体对象为评论
            if(action.getEntityType() == EntityType.ENTITY_COMMENT){
                Comment comment = commentService.getCommentByEntityId(action.getEntityId());
                if (comment!=null){
                    content = comment.getContent();
                }
                //限制字数
                if (content.length() > 10 ){
                    content =  content.substring(0,9);
                }
                //问题的标题
                String qTitle = questionService.getById(comment.getEntityId()).getTitle();
                if(action.getEventType() == EventType.LIKE.getValue()){

                    questionService.getById(comment.getEntityId()).getTitle();
                    content = "赞了问题：" +  qTitle + " 的评论：" + content +".....";
                    entityUrl = "http://localhost:8081/question/" + comment.getEntityId();
                }
                if(action.getEventType() == EventType.COMMENT.getValue()){
                    content = "对问题：《" +qTitle + "》  发表了评论：" + content +".....";
                    entityUrl = "http://localhost:8081/question/" + comment.getEntityId();
                }
            }
            vo.set("createdDate",action.getCreatedDate());
            vo.set("entityUrl",entityUrl);
            vo.set("content",content);
            vos.add(vo);
        }
        return vos;
    }

}
