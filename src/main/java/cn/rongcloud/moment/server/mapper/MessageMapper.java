package cn.rongcloud.moment.server.mapper;

import cn.rongcloud.moment.server.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface MessageMapper {

    int insertMessage(Message message);

    Message getMessage(@Param("messageId") String messageId, @Param("userId") String userId);

    List<Message> getMessageByPage(@Param("userId") String userId, @Param("fromMessageAutoIncId") Long fromMessageAutoIncId, @Param("size") Integer size);

    void batchDelete(@Param("userId") String userId, @Param("messageIds") List<String> messageIds);

    void deleteAll(@Param("userId") String userId);

    void batchInsertMessage(@Param("messages") List<Message> messages);

    void updateStatus(@Param("status") Integer status, @Param("messageId") String messageId);

    List<String> getLikeAlreadyNotifyUser(@Param("messageId") String messageId, @Param("userId") String userId);

    void delMsgBeforeDate(Date date);
}
