package com.cc.wenda.service;

import com.cc.wenda.dao.QuestionDAO;
import com.cc.wenda.model.Question;
import com.cc.wenda.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by nowcoder on 2016/7/15.
 */
@Service
public class QuestionService {

    @Autowired
    UserService userService;

    @Autowired
    QuestionDAO questionDAO;

    @Autowired
    SensitiveService sensitiveService;


    public Question getById(int id) {
        return questionDAO.getById(id);
    }

    public int addQuestion(Question question){
        //转义html代码
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        //敏感词过滤
        question.setContent(sensitiveService.filter(question.getContent()));
        question.setTitle(sensitiveService.filter(question.getTitle()));


        return questionDAO.addQuestion(question) > 0 ?question.getId() : 0;
    }

    public List<Question> getLatestQuestions(int userId, int offset, int limit) {
        return questionDAO.selectLatestQuestions(userId, offset, limit);
    }

    public List<Question> selectScoreQuestions(int userId, int offset, int limit) {
        return questionDAO.selectScoreQuestions(userId, offset, limit);
    }

    public int updateCommentCount(int id, int count) {
        return questionDAO.updateCommentCount(id, count);
    }


    public int updateScoreById(int id, double score) {
        return questionDAO.updateScoreById(id, score);
    }

}
