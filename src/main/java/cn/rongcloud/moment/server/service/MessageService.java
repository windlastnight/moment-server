package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.model.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by sunyinglong on 2020/6/3
 */
public interface MessageService {
    void saveMessage(List<Message> messages);
    RestResult getUnreadCount();
    RestResult getUnread();
    RestResult getHistory(String fromMessageId, Integer size);

    RestResult batchDelete(List<String> ids);
    RestResult deleteAll();
    List<String> getLikeAlreadyNotifyUser(String messageId, String userId);
}
