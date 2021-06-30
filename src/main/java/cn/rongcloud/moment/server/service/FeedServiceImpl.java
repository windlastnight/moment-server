package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.common.utils.IdentifierUtils;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.enums.FeedStatus;
import cn.rongcloud.moment.server.mapper.FeedMapper;
import cn.rongcloud.moment.server.mapper.TimelineMapper;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Like;
import cn.rongcloud.moment.server.model.Timeline;
import cn.rongcloud.moment.server.pojos.*;
import cn.rongcloud.moment.server.common.rce.RceHelper;
import cn.rongcloud.moment.server.common.rce.RceRespResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Value("${moment.system_manager_id}")
    private String systemManagerId;


    @Override
    public RestResult publish(String userId, ReqFeedPublish data) {
        //从 RCE 获取部门下所有员工 Id
        RceRespResult result = rceHelper.queryStaffOrgIds(userId);
        List<String> orgIds = result.getResult();
        if (orgIds == null || orgIds.isEmpty()) {
            return RestResult.generic(RestResultCode.ERR_FEED_PUBLISH_ORG_ID_ERROR);
        }

        for (String inputOrgId: data.getOrgIds()) {
            if (!orgIds.contains(inputOrgId)) {
                return RestResult.generic(RestResultCode.ERR_FEED_PUBLISH_ORG_ID_ERROR);
            }
        }

        //动态存储
        Feed feed = new Feed();
        saveFeed(userId, data, feed);

        //将 feed 存储至队列,定时通知
        redisOptService.setAdd(RedisKey.getMomentPublishNotifyUsersKey(), data.getOrgIds());

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
    public RestResult getFeedInfo(String feedId, boolean withComments, int commentSize, boolean withLikes, int likeSize) {
        //检查feed是否存在
        Feed feed = feedMapper.getFeedById(feedId);
        if (feed == null) {
            return RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED);
        }
        //TODO 校验用户是否有权限查看 Feed
        RespFeedInfo resp = build(feed, withComments, commentSize, withLikes, likeSize);
        return RestResult.success(resp);
    }

    @Override
    public RestResult batchGetFeedInfo(List<String> feedIds, boolean withComments, int commentSize, boolean withLikes, int likeSize) {

        List<RespFeedInfo> respFeedInfoList = new ArrayList<>();
        if (feedIds == null || feedIds.isEmpty()) {
            return RestResult.success(respFeedInfoList);
        }

        List<Feed> feeds = feedMapper.getFeedsByIds(feedIds);
        if (feeds != null && !feeds.isEmpty()) {
            for (Feed feed: feeds) {
                RespFeedInfo resp = build(feed, withComments, commentSize, withLikes, likeSize);
                respFeedInfoList.add(resp);
            }
        }

        List<RespFeedInfo> result = new ArrayList<>();
        Map<String, RespFeedInfo> feedInfoMap = respFeedInfoList.stream().collect(Collectors.toMap(RespFeedInfo::getFeedId, Function.identity()));
        for (String feedId: feedIds) {
            if (feedInfoMap.containsKey(feedId)) {
                result.add(feedInfoMap.get(feedId));
            } else {
                RespFeedInfo feedInfo = new RespFeedInfo();
                feedInfo.setFeedId(feedId);
                feedInfo.setFeedStatus(1);
                result.add(feedInfo);
            }
        }
        //TODO 校验用户是否有权限查看 Feed
        return RestResult.success(result);
    }

    @Override
    public RestResult getNewFeed(String latestFeedId) {
        RespGetNewFeed resp = new RespGetNewFeed();
        resp.setHasNew(false);

        Long fromTimelineId = null;
        if (!StringUtils.isEmpty(latestFeedId)) {
            fromTimelineId = timelineMapper.getMaxTimelineIdByFeedId(latestFeedId);
        }
        RceRespResult rceQueryResult = rceHelper.queryStaffOrgIds(UserHolder.getUid());
        if (!rceQueryResult.isSuccess()){
            return RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED);
        }
        List<String> orgIds = rceQueryResult.getResult();
        if (orgIds == null || orgIds.isEmpty()) {
            return RestResult.success(resp);
        }

        String feedId = timelineMapper.getNewFeed(orgIds, fromTimelineId);
        if (feedId != null) {
            resp.setHasNew(true);
        }
        return RestResult.success(resp);
    }

    @Override
    public Feed checkFeedExists(String feedId) throws RestException {
        Feed feed = this.feedMapper.getFeedById(feedId);
        if (Objects.isNull(feed) || Objects.equals(feed.getFeedStatus(), FeedStatus.DELETED.getValue())) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        }
        return feed;
    }

    private RespFeedInfo build(Feed feed, boolean withComments, int commentSize, boolean withLikes, int likeSize){
        RespFeedInfo resp = new RespFeedInfo();
        resp.setFeedId(feed.getFeedId());
        resp.setUserId(feed.getUserId());
        resp.setFeedType(feed.getFeedType());
        resp.setFeedContent(feed.getFeedContent());
        resp.setFeedStatus(feed.getFeedStatus());
        resp.setCreateDt(feed.getCreateDt());
        resp.setUpdateDt(feed.getUpdateDt());

        if (withComments) {
            List<RespCommentInfo> respCommentInfoList = new ArrayList<>();
            List<Comment> comments = commentService.getComments(feed.getFeedId(), null, commentSize);
            if (comments != null && !comments.isEmpty()){
                for (Comment comment: comments) {
                    RespCommentInfo respCommentInfo = new RespCommentInfo();
                    respCommentInfo.setCommentId(comment.getCommentId());
                    respCommentInfo.setContent(comment.getCommentContent());
                    respCommentInfo.setUserId(comment.getUserId());
                    respCommentInfo.setCreateDt(comment.getCreateDt());
                    respCommentInfo.setReplyTo(comment.getReplyTo());
                    respCommentInfoList.add(respCommentInfo);
                }
            }
            resp.setComments(respCommentInfoList);
        }

        if (withLikes) {
            List<RespLikeInfo> respLikeInfoList = new ArrayList<>();
            List<Like> likes = likeService.getLikes(feed.getFeedId(), null, likeSize);
            if (likes != null && !likes.isEmpty()) {
                for (Like like: likes) {
                    RespLikeInfo respLikeInfo = new RespLikeInfo();
                    respLikeInfo.setLikeId(like.getLikeId());
                    respLikeInfo.setUserId(like.getUserId());
                    respLikeInfo.setCreateDt(like.getCreateDt());
                    respLikeInfoList.add(respLikeInfo);
                }
            }
            resp.setLikes(respLikeInfoList);
        }

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
