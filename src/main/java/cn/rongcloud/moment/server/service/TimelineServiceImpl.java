package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rce.RceHelper;
import cn.rongcloud.moment.server.common.rce.RceRespResult;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.mapper.FeedMapper;
import cn.rongcloud.moment.server.mapper.TimelineMapper;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Timeline;
import cn.rongcloud.moment.server.pojos.RespTimeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
        List<String> timelines = new ArrayList<>();

        Long fromTimelineAutoIncId = null;
        if (!StringUtils.isEmpty(fromFeedId)) {
            fromTimelineAutoIncId = timelineMapper.getMinTimelineIdByFeedId(fromFeedId);
            if (fromTimelineAutoIncId == null) {
                return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
            }
        }

        RceRespResult rceQueryResult = rceHelper.queryStaffOrgIds(UserHolder.getUid());
        if (!rceQueryResult.isSuccess()){
            return RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED);
        }

        List<String> orgIds = rceQueryResult.getResult();
        if (orgIds != null && !orgIds.isEmpty()) {
            List<Timeline> timelineList = timelineMapper.getTimeline(orgIds, fromTimelineAutoIncId, size);
            timelines = timelineList.stream().map(Timeline::getFeedId).collect(Collectors.toList());
        }

        RespTimeline respTimeline = new RespTimeline();
        respTimeline.setFeedIds(timelines);
        return RestResult.success(respTimeline);
    }

    @Override
    public RestResult getUserTimeLine(String fromFeedId, Integer size, String userId) {
        //TODO 校验用户是否有权限查看这个人的朋友圈
        Long fromFeedAutoIncId = null;
        if (!StringUtils.isEmpty(fromFeedId)) {
            Feed feed = feedMapper.getFeedById(fromFeedId);
            if (feed == null) {
                return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
            }
            fromFeedAutoIncId = feed.getId();
        }
        List<String> timelines = feedMapper.getFeedIdsByUserId(userId, fromFeedAutoIncId, size);
        RespTimeline respTimeline = new RespTimeline();
        respTimeline.setFeedIds(timelines);
        return RestResult.success(respTimeline);
    }

}
