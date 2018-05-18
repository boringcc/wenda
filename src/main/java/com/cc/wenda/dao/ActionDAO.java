package com.cc.wenda.dao;

import com.cc.wenda.model.Action;
import com.cc.wenda.model.Comment;
import com.cc.wenda.model.Message;
import com.cc.wenda.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActionDAO {
    String TABLE_NAME = " action ";
    String INSERT_FIELDS = " user_id,entity_type, entity_id ,created_date,event_type";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId},#{entityType},#{entityId},#{createdDate},#{eventType})"})
    int addAction(Action action);


    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where user_id=#{userId}  order by created_date desc"})
    List<Action> selectActionByUID(@Param("userId") int userId);


    List<Action> selectActionsByUID(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

}
