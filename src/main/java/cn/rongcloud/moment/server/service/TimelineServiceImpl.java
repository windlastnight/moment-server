package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.mapper.FeedMapper;
import cn.rongcloud.moment.server.mapper.TimelineMapper;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Timeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class TimelineServiceImpl implements TimelineService {

    @Autowired
    private TimelineMapper timelineMapper;

    @Autowired
    private FeedMapper feedMapper;

    @Override
    public RestResult getTimeLine(String fromFeedId, Integer size) {
        Timeline timeline = timelineMapper.getTimelineByFeedId(fromFeedId);
        if (timeline == null) {
            return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
        }

        //TODO 向 RCE 获取用户的部门
        List<String> orgIds = new ArrayList<>();
        orgIds.add(timeline.getOrgId());
        List<String> timelines = timelineMapper.getTimeline(orgIds, timeline.getCreateDt(), size);

        return RestResult.success(timelines);
    }
}
