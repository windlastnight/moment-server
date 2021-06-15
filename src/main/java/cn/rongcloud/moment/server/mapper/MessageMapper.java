package cn.rongcloud.moment.server.mapper;

import cn.rongcloud.moment.server.model.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper {

    int insertMessage(Message message);

}
