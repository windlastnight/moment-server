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
    public RestResult getMineTimeLine(String fromFeedId, Integer size) {

        List<String> timelines = getTimeLine(fromFeedId, size, UserHolder.getUid());
        return RestResult.success(timelines);
    }

    @Override
    public RestResult getUserTimeLine(String fromFeedId, Integer size, String userId) {
        //TODO 校验用户是否有权限查看这个人的朋友圈
        List<String> timelines = getTimeLine(fromFeedId, size, userId);
        return RestResult.success(timelines);
    }

    private List<String> getTimeLine(String fromFeedId, Integer size, String userId){

        List<String> timelines = null;

        Date fromDate = DateTimeUtils.strToDate("2020-01-01");
        if (!StringUtils.isEmpty(fromFeedId)) {
            Timeline timeline = timelineMapper.getTimelineByFeedId(fromFeedId);
            if (timeline == null) {
                throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
            }
            fromDate = timeline.getCreateDt();
        }

        RceRespResult rceQueryResult = rceHelper.queryStaffOrgIds(UserHolder.getUid());
        if (!rceQueryResult.isSuccess()){
            throw new RestException(RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED));
        }

        List<String> orgIds = rceQueryResult.getResult();
        if (orgIds == null || orgIds.isEmpty()) {
            timelines = new ArrayList<>();
            return timelines;
        }

        timelines = timelineMapper.getTimeline(orgIds, fromDate, size);

        return timelines;
    }
}
