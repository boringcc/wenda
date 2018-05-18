package com.cc.wenda.service;

import com.cc.wenda.dao.MessageDAO;
import com.cc.wenda.model.Message;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    MessageDAO messageDAO;

    @Autowired
    SensitiveService sensitiveService;

    public int addMessage(Message message) {
        message.setContent(sensitiveService.filter(message.getContent()));
        return messageDAO.addMessage(message) > 0 ? message.getId() : 0;
    }

    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return  messageDAO.getConversationDetail(conversationId, offset, limit);
    }

    public List<Message> getConversationList(int userId, int offset, int limit) {
        return  messageDAO.getConversationList(userId, offset, limit);
    }

    public int getConversationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConversationUnreadCount(userId, conversationId);
    }
    public int redMessage(String conversationId){
        return messageDAO.redMessage(conversationId);
    }
}
