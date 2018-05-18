package com.cc.wenda.async;


import com.alibaba.fastjson.JSON;
import com.cc.wenda.util.JedisAdapter;
import com.cc.wenda.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理所有队列中的event
 */
@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware{

    //通过eventType得到该类型要用的handler
    private Map<EventType,List<EventHandler>> config = new HashMap<EventType,List<EventHandler>>();

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private ApplicationContext applicationContext;

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        //初始化，得到所有EventHandler的实现类
        Map<String,EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if(beans != null){
            //将本handler所关心的事件类型与其关联起来
            for(Map.Entry<String,EventHandler> entry : beans.entrySet()){
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
                for(EventType type : eventTypes){
                    if(!config.containsKey(type)){
                        config.put(type,new ArrayList<EventHandler>());
                    }

                    config.get(type).add(entry.getValue());
                }
            }
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String key = RedisKeyUtil.getEventQueueKey();
                    //从队列中移除并获取最后一个元素
                    List<String> events = jedisAdapter.brpop(0,key);
                    for(String message : events){
                        if(message.equals(key)){
                            continue;
                        }

                        //反序列化
                        EventModel eventModel = JSON.parseObject(message,EventModel.class);
                        if(!config.containsKey(eventModel.getType())){
                            logger.error("不能识别的事件类型");
                            continue;
                        }
                        for (EventHandler handler : config.get(eventModel.getType())){
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //将接口存储起来
        this.applicationContext = applicationContext;
    }
}
