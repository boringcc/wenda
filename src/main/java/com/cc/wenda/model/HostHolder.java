package com.cc.wenda.model;

import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    //每个线程分配一个对象
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();;
    }
}
