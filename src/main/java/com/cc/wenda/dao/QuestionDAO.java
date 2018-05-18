package com.cc.wenda.dao;

import com.cc.wenda.model.Question;
import com.cc.wenda.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionDAO {
    String TABLE_NAME = " question ";
    String INSERT_FIELDS = " title, content, created_date, user_id, comment_count,score";
    String SELECT_FIELDS = " id, title, content, created_date, user_id, comment_count,score";

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{title},#{content},#{createdDate},#{userId},#{commentCount},#{score})"})
    int addQuestion(Question question);

    List<Question> selectLatestQuestions(@Param("userId") int userId, @Param("offset") int offset,
                                         @Param("limit") int limit);

    List<Question> selectScoreQuestions(@Param("userId") int userId, @Param("offset") int offset,
                                         @Param("limit") int limit);


    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Question getById(int id);

    @Update({"update ", TABLE_NAME, " set comment_count = #{commentCount} where id=#{id}"})
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    @Update({"update ", TABLE_NAME, " set score = #{score} where id=#{id}"})
    int updateScoreById(@Param("id") int id,@Param("score") double score);

}
