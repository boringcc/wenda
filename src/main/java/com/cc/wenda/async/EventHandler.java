package com.cc.wenda.async;


import java.util.List;

public interface EventHandler {

    //处理
    void doHandle(EventModel model);

    //注册
    List<EventType> getSupportEventTypes();
}
