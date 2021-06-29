package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.im.IMHelper;
import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.common.utils.IdentifierUtils;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.enums.MessageStatus;
import cn.rongcloud.moment.server.enums.MomentsCommentType;
import cn.rongcloud.moment.server.mapper.LikeMapper;
import cn.rongcloud.moment.server.model.*;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqLikeIt;
import cn.rongcloud.moment.server.pojos.RespLike;
import cn.rongcloud.moment.server.pojos.RespLikeIt;
import com.google.common.collect.Lists;
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
@Service
public class LikeServiceImpl implements LikeService {

    @Resource
    FeedService feedService;

    @Resource
    LikeMapper likeMapper;

    @Resource
    IMHelper imHelper;

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

    @Override
    public RestResult likeIt(ReqLikeIt reqLike) throws RestException {
        String feedId = reqLike.getFeedId();
        Feed feed = this.feedService.checkFeedExists(feedId);
        checkUserLikeFeed(feedId);
        Like like = this.saveLike(feedId);
        this.handleCommentCache(feedId);
        CacheService.cacheOne(RedisKey.getLikeSetKey(feedId), RedisKey.getLikeKey(feedId), like.getLikeId(),
                like, CacheService.date2Score(like.getCreateDt()), expireProperties.getComment());

        RespLikeIt respLikeIt = new RespLikeIt();
        BeanUtils.copyProperties(like, respLikeIt);
        List<String> receivers = this.commentService.getCommentNtfReceivers(feed);
        List<String> alreadyNotifyUserIds = messageService.getLikeAlreadyNotifyUser(feedId, UserHolder.getUid());
        if (receivers != null && alreadyNotifyUserIds != null) {
            receivers.removeAll(alreadyNotifyUserIds);
        }

        LikeNotifyData likeNotifyData = new LikeNotifyData();
        BeanUtils.copyProperties(like, likeNotifyData);
        likeNotifyData.setCreateDt(like.getCreateDt().getTime());
        this.imHelper.publishCommentNtf(receivers, likeNotifyData, MomentsCommentType.LIKE);

        if (receivers != null && !receivers.isEmpty()) {

            List<Message> messages = new ArrayList<>();
            for (String receiverId: receivers) {
                if (receiverId.equals(UserHolder.getUid())) {
                    continue;
                }
                Message message = new Message();
                message.setFeedId(feedId);
                message.setMessageId(like.getLikeId());
                message.setUserId(receiverId);
                message.setPublishUserId(UserHolder.getUid());
                message.setCreateDt(like.getCreateDt());
                message.setMessageType(MomentsCommentType.LIKE.getType());
                message.setStatus(MessageStatus.NORMAL.getValue());
                messages.add(message);
                redisOptService.zsAdd(RedisKey.getUserUnreadMessageKey(receiverId), message, like.getCreateDt().getTime());
            }
            messageService.saveMessage(messages);
        }

        return RestResult.success(respLikeIt);
    }


    private void checkUserLikeFeed(String feedId) throws RestException {
        Like like = getLikeByUser(feedId);
        if (Objects.nonNull(like)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_ALEADY_LIKED));
        }
    }

    private Like getLikeByUser(String feedId) {
        return this.likeMapper.selectByFeedIdAndUserId(feedId, UserHolder.getUid());
    }

    @Override
    public void unLikeIt(String feedId) throws RestException {
        this.feedService.checkFeedExists(feedId);
        Like like = this.getLikeByUser(feedId);
        if (Objects.isNull(like)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_NO_LIKE));
        }
        this.likeMapper.deleteByPrimaryKey(like.getId());
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
        if (fromLikeId != null) {
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
        return likes;
    }

    private Like saveLike(String feedId) {
        Like savedLike = new Like();
        savedLike.setCreateDt(DateTimeUtils.currentDt());
        savedLike.setFeedId(feedId);
        savedLike.setUserId(UserHolder.getUid());
        savedLike.setLikeId(IdentifierUtils.uuid24());
        this.likeMapper.insertSelective(savedLike);
        return savedLike;
    }

    public void handleCommentCache(String feedId) {
        CacheService.cacheHandle(RedisKey.getLikeSetKey(feedId), CacheService.getLikes, feedId, expireProperties.getComment());
    }

}
