package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.common.utils.IdentifierUtils;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.enums.LikeStatus;
import cn.rongcloud.moment.server.mapper.LikeMapper;
import cn.rongcloud.moment.server.model.CacheExpireProperties;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Like;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqLikeIt;
import cn.rongcloud.moment.server.pojos.RespLike;
import cn.rongcloud.moment.server.pojos.RespLikeIt;
import cn.rongcloud.moment.server.service.asyncTask.PublishCommentTask;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Slf4j
@Service
public class LikeServiceImpl implements LikeService {

    @Resource
    FeedService feedService;

    @Resource
    LikeMapper likeMapper;

    @Resource
    CommentService commentService;

    @Resource
    MessageService messageService;

    @Resource
    RedisOptService redisOptService;

    @Resource
    CacheExpireProperties expireProperties;

    @Resource(name = "redisTemplate")
    ZSetOperations zSetOperations;

    @Resource
    PublishCommentTask publishCommentTask;

    @Override
    public RestResult likeIt(ReqLikeIt reqLike) throws RestException {
        String feedId = reqLike.getFeedId();
        Feed feed = this.feedService.checkFeedExists(feedId);
        checkUserLikeFeed(feedId);
        Like like = checkUserLikeFeed(feedId);
        if (like == null) {
            like = this.saveLike(feedId);
        } else {
            updateLikeStatus(LikeStatus.NORMAL.getValue(), like.getId());
            like.setLikeStatus(LikeStatus.NORMAL.getValue());
        }
        this.handleCommentCache(feedId);
        CacheService.cacheOne(RedisKey.getLikeSetKey(feedId), RedisKey.getLikeKey(feedId), like.getLikeId(),
                like, CacheService.date2Score(like.getCreateDt()), expireProperties.getComment());


        RespLikeIt respLikeIt = new RespLikeIt();
        BeanUtils.copyProperties(like, respLikeIt);
        List<String> receivers = this.commentService.getCommentNtfReceivers(feed);
        if (like.getLikeStatus() == LikeStatus.DELETED.getValue()) {
            List<String> alreadyNotifyUserIds = messageService.getLikeAlreadyNotifyUser(like.getLikeId(), UserHolder.getUid());
            if (receivers != null && alreadyNotifyUserIds != null) {
                receivers.removeAll(alreadyNotifyUserIds);
            }
        }
        publishCommentTask.likeTask(like, receivers);
        return RestResult.success(respLikeIt);
    }


    private Like checkUserLikeFeed(String feedId) throws RestException {
        Like like = getLikeByUser(feedId);
        if (Objects.nonNull(like) && like.getLikeStatus() == LikeStatus.NORMAL.getValue()) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_ALEADY_LIKED));
        }
        return like;
    }

    private Like getLikeByUser(String feedId) {
        log.info("get like by user, feedId:{}, userId:{}", feedId, UserHolder.getUid());
        return this.likeMapper.selectByFeedIdAndUserId(feedId, UserHolder.getUid());
    }

    @Override
    public void unLikeIt(String feedId) throws RestException {
        this.feedService.checkFeedExists(feedId);
        Like like = this.getLikeByUser(feedId);
        if (Objects.isNull(like)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_NO_LIKE));
        }
        like.setLikeStatus(LikeStatus.DELETED.getValue());
        this.likeMapper.updateByPrimaryKeySelective(like);
        CacheService.uncacheOne(RedisKey.getLikeSetKey(feedId), RedisKey.getLikeKey(feedId), like.getLikeId());
    }

    @Override
    public RestResult getPagedLikes(String fid, Paged page) throws RestException {

        this.feedService.checkFeedExists(fid);

        List<Like> likes = getLikes(fid, page.getFromUId(), page.getSize());

        List<RespLike> res = likes.stream().map(cm -> {
            RespLike respLike = new RespLike();
            BeanUtils.copyProperties(cm, respLike);
            return respLike;
        }).collect(Collectors.toList());
        return RestResult.success(res);
    }

    @Override
    public List<Like> getLikes(String feedId, String fromLikeId, int size) {
        String zsetKey = RedisKey.getLikeSetKey(feedId);
        this.handleCommentCache(feedId);
        Long index;
        if (StringUtils.isNotBlank(fromLikeId)) {
            index = zSetOperations.rank(zsetKey, fromLikeId);
        } else {
            index = 1L;
        }
        List<Like> likes = Lists.newArrayList();
        if (index == null) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_NO_LIKE));
        } else {
            Set keys = zSetOperations.range(zsetKey, index, size);
            if (!CollectionUtils.isEmpty(keys)) {
                likes = (List<Like>) Optional.ofNullable(redisOptService.hmget(RedisKey.getLikeKey(feedId), Lists.newArrayList(keys.iterator()))).map(Map::values).map(Lists::newArrayList).orElse(Lists.newArrayList());
            }
        }
        Collections.sort(likes, Comparator.comparing(Like::getCreateDt));
        return likes;
    }

    @Override
    public List<Like> batchGetLikes(List<String> likeIds) {
        List<Like> likes;
        if (likeIds == null || likeIds.isEmpty()) {
            return new ArrayList<>();
        }

        likes = likeMapper.batchGetLikes(likeIds);
        if (likes == null) {
            return new ArrayList<>();
        }
        return likes;
    }

    private Like saveLike(String feedId) {
        Like savedLike = new Like();
        savedLike.setCreateDt(DateTimeUtils.currentDt());
        savedLike.setFeedId(feedId);
        savedLike.setUserId(UserHolder.getUid());
        savedLike.setLikeId(IdentifierUtils.uuid32());
        savedLike.setLikeStatus(LikeStatus.NORMAL.getValue());
        this.likeMapper.insertSelective(savedLike);
        return savedLike;
    }

    public void handleCommentCache(String feedId) {
        CacheService.cacheHandle(RedisKey.getLikeSetKey(feedId), CacheService.getLikes, feedId, expireProperties.getComment());
    }

    private Like updateLikeStatus(Integer status, Long likeId) {
        Like like = new Like();
        like.setId(likeId);
        like.setLikeStatus(status);
        this.likeMapper.updateByPrimaryKeySelective(like);
        return like;
    }

}
