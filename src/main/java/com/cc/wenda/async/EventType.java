package com.cc.wenda.async;

public enum EventType {
    LIKE(0),    //点赞
    COMMENT(1), //评论
    LOGIN(2),   //登录
    MAIL(3),    //邮件
    FOLLOW(4),  //关注
    UNFOLLOW(5),//取消关注
    ADD_QUESTION(6), //增加题目
    SEND_EMAIL(7);   //发送邮件
    private int value;
    EventType(int value) { this.value = value; }
    public int getValue() { return value; }
}
