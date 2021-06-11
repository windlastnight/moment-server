package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestException;
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
import cn.rongcloud.moment.server.common.rce.RceRespResult;
import cn.rongcloud.moment.server.pojos.RespFeedInfo;
import cn.rongcloud.moment.server.pojos.RespFeedPublish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
        RceRespResult result = rceHelper.queryAllStaffId(data.getOrgIds(), userId);
        if (!result.isSuccess()) {
            return RestResult.generic(RestResultCode.ERR_FEED_PUBLISH_ORG_ID_ERROR);
        }

        //动态存储
        Feed feed = new Feed();
        saveFeed(userId, data, feed);

        //将发布者排除，动态发布不通知发布者
        List<String> staffIds = result.getResult();
        staffIds.remove(userId);

        //将 feed 存储至队列,定时通知
        redisOptService.setAdd(RedisKey.getMomentPublishNotifyUsersKey(), staffIds);

        //返回数据封装
        RespFeedPublish resp = new RespFeedPublish();
        resp.setFeedId(feed.getFeedId());
        resp.setFeedStatus(feed.getFeedStatus());
        resp.setUpdateDt(feed.getUpdateDt());
        resp.setCreateDt(feed.getCreateDt());

        return RestResult.success(resp);
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

    @Override
    public RestResult getFeedInfo(String userId, String feedId) {
        //检查feed是否存在
        Feed feed = feedMapper.getFeedById(feedId);
        if (feed == null) {
            return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
        }
        //TODO 校验用户是否有权限查看 Feed
        //TODO 获取前 50 条点赞
        //TODO 获取前 50 条评论
        RespFeedInfo resp = bulid(feed);
        return RestResult.success(resp);
    }

    @Override
    public RestResult batchGetFeedInfo(String userId, List<String> feedIds) {

        List<RespFeedInfo> result = new ArrayList<>();

        List<Feed> feeds = feedMapper.getFeedsByIds(feedIds);
        if (feeds != null && !feeds.isEmpty()) {
            for (Feed feed: feeds) {
                RespFeedInfo resp = bulid(feed);
                result.add(resp);
            }
        }

        //TODO 校验用户是否有权限查看 Feed
        //TODO 获取前 50 条点赞
        //TODO 获取前 50 条评论

        return RestResult.success(result);
    }

    @Override
    public Feed checkFeedExists(String feedId) throws RestException {
        Feed feed = this.feedMapper.getFeedById(feedId);
        if (Objects.isNull(feed) || Objects.equals(feed.getFeedStatus(), FeedStatus.DELETED.getValue())) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        }
        return feed;
    }

    private RespFeedInfo bulid(Feed feed){
        RespFeedInfo resp = new RespFeedInfo();
        resp.setFeedId(feed.getFeedId());
        resp.setUserId(feed.getUserId());
        resp.setFeedType(feed.getFeedType());
        resp.setFeedContent(feed.getFeedContent());
        resp.setFeedStatus(feed.getFeedStatus());
        resp.setCreateDt(feed.getCreateDt());
        resp.setUpdateDt(feed.getUpdateDt());
        return resp;
    }


    private void saveFeed(String userId, ReqFeedPublish data, Feed feed) {

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
