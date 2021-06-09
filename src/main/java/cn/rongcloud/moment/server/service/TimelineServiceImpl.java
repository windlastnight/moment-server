package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rce.RceHelper;
import cn.rongcloud.moment.server.common.rce.RceQueryResult;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.mapper.FeedMapper;
import cn.rongcloud.moment.server.mapper.TimelineMapper;
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

    @Autowired
    private RceHelper rceHelper;

    @Override
    public RestResult getTimeLine(String fromFeedId, Integer size) {
        List<String> timelines = null;

        Timeline timeline = timelineMapper.getTimelineByFeedId(fromFeedId);
        if (timeline == null) {
            return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
        }

        RceQueryResult rceQueryResult = rceHelper.queryStaffOrgIds(UserHolder.getUid());
        if (!rceQueryResult.isSuccess()){
            return RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED);
        }

        List<String> orgIds = rceQueryResult.getResult();
        if (orgIds == null || orgIds.isEmpty()) {
            timelines = new ArrayList<>();
            return RestResult.success(timelines);
        }

        timelines = timelineMapper.getTimeline(orgIds, timeline.getCreateDt(), size);

        return RestResult.success(timelines);
    }
}
