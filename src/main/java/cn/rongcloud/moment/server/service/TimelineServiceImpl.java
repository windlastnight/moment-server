package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rce.RceHelper;
import cn.rongcloud.moment.server.common.rce.RceRespResult;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.mapper.FeedMapper;
import cn.rongcloud.moment.server.mapper.TimelineMapper;
import cn.rongcloud.moment.server.model.Timeline;
import cn.rongcloud.moment.server.pojos.RespTimeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
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

        Date fromDate = DateTimeUtils.currentDt();
        if (!StringUtils.isEmpty(fromFeedId)) {
            Timeline timeline = timelineMapper.getTimelineByFeedId(fromFeedId);
            if (timeline == null) {
                return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
            }
            fromDate = timeline.getCreateDt();
        }

        RceRespResult rceQueryResult = rceHelper.queryStaffOrgIds(UserHolder.getUid());
        if (!rceQueryResult.isSuccess()){
            return RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED);
        }

        List<String> orgIds = rceQueryResult.getResult();
        if (orgIds == null || orgIds.isEmpty()) {
            timelines = new ArrayList<>();
        } else {
            timelines = timelineMapper.getTimeline(orgIds, fromDate, size);
        }

        RespTimeline respTimeline = new RespTimeline();
        respTimeline.setFeedIds(timelines);
        return RestResult.success(respTimeline);
    }

    @Override
    public RestResult getUserTimeLine(String fromFeedId, Integer size, String userId) {
        //TODO 校验用户是否有权限查看这个人的朋友圈
        Date fromDate = DateTimeUtils.currentDt();
        if (!StringUtils.isEmpty(fromFeedId)) {
            Timeline timeline = timelineMapper.getTimelineByFeedId(fromFeedId);
            if (timeline == null) {
                return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
            }
            fromDate = timeline.getCreateDt();
        }
        List<String> timelines = feedMapper.getFeedIdsByUserId(userId, fromDate, size);
        RespTimeline respTimeline = new RespTimeline();
        respTimeline.setFeedIds(timelines);
        return RestResult.success(respTimeline);
    }

}
