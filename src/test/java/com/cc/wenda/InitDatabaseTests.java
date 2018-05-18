package com.cc.wenda;

import com.cc.wenda.async.EventType;
import com.cc.wenda.dao.QuestionDAO;
import com.cc.wenda.dao.UserDAO;
import com.cc.wenda.model.*;
import com.cc.wenda.service.ActionService;
import com.cc.wenda.service.CommentService;
import com.cc.wenda.service.QuestionService;
import com.cc.wenda.service.UserService;
import com.cc.wenda.util.JedisAdapter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WendaApplication.class)
public class InitDatabaseTests {
    @Autowired
    UserDAO userDAO;

    @Autowired
    QuestionDAO questionDAO;

    @Autowired
    JedisAdapter jedisAdapter;


    @Autowired
    ActionService actionService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    /**
     * LIKE(0),    //点赞
     COMMENT(1), //评论
     LOGIN(2),   //登录
     MAIL(3),    //邮件
     FOLLOW(4),  //关注
     UNFOLLOW(5),//取消关注
     ADD_QUESTION(6), //增加题目索引
     SEND_EMAIL(7);   //发送邮件

     public static int ENTITY_QUESTION = 1;
     public static int ENTITY_COMMENT = 2;
     public static int ENTITY_USER = 3;
     */


    public double getScore(long Qviews,int Qanswers,long Qscore,long Ascore,long QageInHours,long Qupdated){
        double up = ((Math.log(Qviews)) + ((Qanswers * Qscore)*2) + Ascore*2 )*5;
        double down = (QageInHours +5) - ((QageInHours - Qupdated)/2);
        double score = up/down;
        return score;
    }

    @Test
    public void testScore(){
        Date nowDate = new Date();
        long now = nowDate.getTime();
        System.out.println(getScore(2,0,0,0,0,0));
    }

    @Test
    public void testQusetionService(){
        Question question = new Question();
        question.setUserId(1);
        question.setTitle("测试增加题目");
        question.setContent("测试增加题目");
        question.setScore(0);
        question.setCreatedDate(new Date());
        question.setCommentCount(0);
        questionService.addQuestion(question);

        //List<Question> questionList =  actionService.selectActionByUID(1);
        //for (Action action : actionList){
        //    System.out.println(action.toString());
        //}

    }

    @Test
    public void testActionService(){
        //Action action = new Action(1,1,18,new Date(),4);
        //actionService.addAction(action);
        List<Action> actionList =  actionService.selectActionsByUID(1,0,2);
        for (Action action : actionList){
            System.out.println(action.toString());
        }

    }


    @Test
    public void test(){
       jedisAdapter.setEx("wlxfcy",60,"wscc");
        System.out.println("val " + jedisAdapter.getValue("wlxfcy"));
    }

    @Test
    public void contextLoads() {
        Random random = new Random();
        for (int i = 0; i < 11; ++i) {
            User user = new User();
            user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", random.nextInt(1000)));
            user.setName(String.format("USER%d", i));
            user.setPassword("");
            user.setSalt("");
            userDAO.addUser(user);
            user.setPassword("newpassword");
            userDAO.updatePassword(user);

            //Question question = new Question();
            //question.setCommentCount(i);
            //Date date = new Date();
            //date.setTime(date.getTime() + 1000 * 3600 * 5 * i);
            //question.setCreatedDate(date);
            //question.setUserId(i + 1);
            //question.setTitle(String.format("TITLE{%d}", i));
            //question.setContent(String.format("Balaababalalalal Content %d", i));
            //questionDAO.addQuestion(question);
        }
        //Assert.assertEquals("newpassword", userDAO.selectById(1).getPassword());
        //userDAO.deleteById(1);
        //Assert.assertNull(userDAO.selectById(1));
    }

    @Test
    public void getActions1(){
        List<ViewObject> viewObjects = getActions(1,10,5);
        for (ViewObject vo : viewObjects){
            System.out.println("content : " + vo.get("content"));
            System.out.println("entityUrl : " + vo.get("entityUrl"));
        }
    }

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
                    content = "对问题：" +qTitle + "发表了：" + content +".....";
                    entityUrl = "http://localhost:8081/question/" + comment.getEntityId();
                }
            }
            vo.set("entityUrl",entityUrl);
            vo.set("content",content);

            vos.add(vo);
        }
        return vos;
    }
}
