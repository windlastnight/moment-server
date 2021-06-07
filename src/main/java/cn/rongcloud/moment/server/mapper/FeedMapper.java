package cn.rongcloud.moment.server.mapper;

import cn.rongcloud.moment.server.model.Feed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FeedMapper {

    int insertFeed(Feed feed);

    Feed getFeedById(@Param("feedId") String feedId);

    void deleteFeed(@Param("feedId") String feedId);
}
