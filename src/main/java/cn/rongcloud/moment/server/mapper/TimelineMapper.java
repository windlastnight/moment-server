package cn.rongcloud.moment.server.mapper;

import cn.rongcloud.moment.server.model.Timeline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface TimelineMapper {

    void batchInsertTimeline(@Param("timelines") List<Timeline> timelines);

    void deleteTimelineByFeedId(@Param("feedId") String feedId);

    Timeline getTimelineByFeedId(@Param("feedId") String feedId);

    List<String> getTimeline(@Param("orgIds") List<String> orgIds, @Param("createDt") Date date, @Param("size") Integer size);
}
