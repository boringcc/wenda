package com.cc.wenda.async;

import com.alibaba.fastjson.JSONObject;
import com.cc.wenda.controller.CommentController;
import com.cc.wenda.util.JedisAdapter;
import com.cc.wenda.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingDeque;

/**
 * 事件的发送者
 */
@Service
public class EventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);
    @Autowired
    JedisAdapter jedisAdapter;

    public boolean fireEvent(EventModel eventModel) {
        try {
            String json = JSONObject.toJSONString(eventModel);
            String key = RedisKeyUtil.getEventQueueKey();
            jedisAdapter.lpush(key, json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }





}
