package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.common.utils.IdentifierUtils;
import cn.rongcloud.moment.server.enums.FeedStatus;
import cn.rongcloud.moment.server.mapper.FeedMapper;
import cn.rongcloud.moment.server.mapper.TimelineMapper;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Timeline;
import cn.rongcloud.moment.server.pojos.ReqFeedPublish;
import cn.rongcloud.moment.server.common.rce.RceHelper;
import cn.rongcloud.moment.server.common.rce.RceQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class FeedServiceImpl implements FeedService {

    @Autowired
    RceHelper rceHelper;

    @Autowired
    FeedMapper feedMapper;

    @Autowired
    TimelineMapper timelineMapper;

    @Autowired
    RedisOptService redisOptService;

    @Value("${moment.system_manager_id}")
    private String systemManagerId;


    @Override
    public RestResult publish(String userId, ReqFeedPublish data) {
        //从 RCE 获取部门下所有员工 Id
        RceQueryResult result = rceHelper.queryAllStaffId(data.getOrgIds(), userId);
        if (!result.isSuccess()) {
            return RestResult.generic(RestResultCode.ERR_FEED_PUBLISH_ORG_ID_ERROR);
        }

        //动态存储
        saveFeed(userId, data);

        //将发布者排除，动态发布不通知发布者
        List<String> staffIds = result.getResult();
        staffIds.remove(userId);

        //将 feed 存储至队列,定时通知
        redisOptService.setAdd(RedisKey.getMomentPublishNotifyUsersKey(), staffIds);

        return RestResult.success();
    }

    @Override
    public RestResult delete(String userId, String feedId) {
        //检查用户是否有权限删除
        if (!userId.equals(systemManagerId)) {
            Feed feed = feedMapper.getFeedById(feedId);
            if (feed == null) {
                return RestResult.success();
            }
            if (!feed.getUserId().equals(userId)) {
                return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
            }
        }
        //操作数据库删除
        feedMapper.deleteFeed(feedId);
        timelineMapper.deleteTimelineByFeedId(feedId);
        return RestResult.success();
    }


    private void saveFeed(String userId, ReqFeedPublish data) {

        Feed feed = new Feed();
        feed.setFeedId(IdentifierUtils.uuid24());
        feed.setUserId(userId);
        feed.setFeedType(data.getType());
        feed.setFeedContent(data.getContent());
        Date date = DateTimeUtils.currentDt();
        feed.setCreateDt(date);
        feed.setUpdateDt(date);
        feed.setFeedStatus(FeedStatus.NORMAL.getValue());
        feedMapper.insertFeed(feed);

        List<Timeline> timelines = new ArrayList<>();
        for (String orgId: data.getOrgIds()) {
            Timeline timeline = new Timeline();
            timeline.setFeedId(feed.getFeedId());
            timeline.setOrgId(orgId);
            timeline.setCreateDt(date);
            timelines.add(timeline);
        }
        timelineMapper.batchInsertTimeline(timelines);
    }

}
