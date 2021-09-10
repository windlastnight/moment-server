package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.model.Message;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ElasticSearchService {
    void saveMessage(Message message);
    void batchSaveMessage(List<Message> messages);
    List<Message> getMessageByPage(String userId, Integer size, Message fromMessage);
    Message getMessage(String messageId, String userId);
    boolean deleteByMessageIds(Map<String, Date> messageMap);
    long deleteByUserId(String userId);
    List<String> getLikeAlreadyNotifyUser(String messageId, String publishUserId);
}
