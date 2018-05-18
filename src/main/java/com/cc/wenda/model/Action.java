package com.cc.wenda.model;

import java.util.Date;

public class Action {
    private int id;
    private int userId;
    private int entityId;
    private int entityType;
    private Date createdDate;
    private int eventType;

    @Override
    public String toString() {
        return "Action{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityId=" + entityId +
                ", entityType=" + entityType +
                ", createdDate=" + createdDate +
                ", eventType=" + eventType +
                '}';
    }

    public Action( int userId,  int entityType,int entityId, Date createdDate, int eventType) {
        this.userId = userId;
        this.entityId = entityId;
        this.entityType = entityType;
        this.createdDate = createdDate;
        this.eventType = eventType;
    }

    public Action() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
