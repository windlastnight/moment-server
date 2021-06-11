package cn.rongcloud.moment.server.mapper;

import cn.rongcloud.moment.server.model.Timeline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TimelineMapper {

    void batchInsertTimeline(@Param("timelines") List<Timeline> timelines);

    void deleteTimelineByFeedId(@Param("feedId") String feedId);

    Long getMinTimelineIdByFeedId(@Param("feedId") String feedId);
    Long getMaxTimelineIdByFeedId(@Param("feedId") String feedId);

    List<String> getTimeline(@Param("orgIds") List<String> orgIds, @Param("fromTimelineAutoIncId") Long fromTimelineAutoIncId, @Param("size") Integer size);

    String getNewFeed(@Param("orgIds") List<String> orgIds, @Param("fromTimelineAutoIncId") Long fromTimelineAutoIncId);
}
