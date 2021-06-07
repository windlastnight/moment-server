package cn.rongcloud.moment.server.mapper;

import cn.rongcloud.moment.server.model.Feed;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedMapper {

    int insertFeed(Feed feed);
}
