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
import cn.rongcloud.moment.server.mapper.CommentMapper;
import cn.rongcloud.moment.server.model.*;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqCreateComment;
import cn.rongcloud.moment.server.pojos.RespComment;
import cn.rongcloud.moment.server.pojos.RespCreateComment;
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
public class CommentServiceImpl implements CommentService {

    @Resource
    CommentMapper commentMapper;

    @Resource
    FeedService feedService;

    @Resource
    MessageService messageService;

    @Resource
    RedisOptService redisOptService;

    @Resource
    IMHelper imHelper;

    @Resource(name = "redisTemplate")
    ZSetOperations zSetOperations;

    @Resource
    CacheExpireProperties expireProperties;

    @Override
    public RestResult comment(ReqCreateComment reqComment) {
//        1.检查feedid是否有效
        Feed feed = this.feedService.checkFeedExists(reqComment.getFeedId());
//        2.检查replyto是否有效TApplicationTypeMapper.xml

        String replyTo = reqComment.getReplyTo();
        String feedId = feed.getFeedId();
        if (StringUtils.isNotBlank(replyTo)) {
            checkCommentUserExists(reqComment.getFeedId(), replyTo);
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(reqComment, comment);
        comment.setCreateDt(DateTimeUtils.currentDt());
        comment.setUserId(UserHolder.getUid());
        comment.setCommentId(IdentifierUtils.uuid24());
        this.commentMapper.insertSelective(comment);

        handleCommentCache(feedId);
        CacheService.cacheOne(RedisKey.getCommentSetKey(feedId), RedisKey.getCommentKey(feedId), comment.getCommentId(),
                comment, CacheService.date2Score(comment.getCreateDt()), expireProperties.getComment());

        List<String> receivers = this.getCommentNtfReceivers(feed);
        CommentNotifyData commentNotifyData = new CommentNotifyData();
        BeanUtils.copyProperties(comment, commentNotifyData);
        commentNotifyData.setCreateDt(comment.getCreateDt().getTime());
        this.imHelper.publishCommentNtf(receivers, commentNotifyData, MomentsCommentType.COMMENT);

        Message message = new Message();
        message.setFeedId(feed.getFeedId());
        message.setMessageId(comment.getCommentId());
        message.setUserId(UserHolder.getUid());
        message.setCreateDt(comment.getCreateDt());
        message.setMessageType(MomentsCommentType.COMMENT.getType());
        message.setStatus(MessageStatus.NORMAL.getValue());
//        messageService.saveMessage(message);
        if (receivers != null && !receivers.isEmpty()) {
            for (String receiverId : receivers) {
                if (receiverId.equals(UserHolder.getUid())) {
                    continue;
                }
                redisOptService.zsAdd(RedisKey.getUserUnreadMessageKey(receiverId), message, comment.getCreateDt().getTime());
            }
        }

        RespCreateComment respCreateComment = new RespCreateComment();
        BeanUtils.copyProperties(comment, respCreateComment);
        return RestResult.success(respCreateComment);
    }

    @Override
    public List<String> getCommentNtfReceivers(Feed feed) {
        List<String> receivers = this.commentMapper.getAllCommentAndLikeUserIds(feed.getFeedId());
        String feedOwner = feed.getUserId();
        if (!receivers.contains(feedOwner)) {
            receivers.add(feedOwner);
        }
        receivers.remove(UserHolder.getUid());
        return receivers;
    }

    @Override
    public List<Comment> batchGetComment(List<String> commentIds) {
        List<Comment> comments;
        if (commentIds == null || commentIds.isEmpty()) {
            return new ArrayList<>();
        }

        comments = commentMapper.batchGetComment(commentIds);
        if (comments == null) {
            return new ArrayList<>();
        }
        return comments;
    }

    public void handleCommentCache(String feedId) {
        CacheService.cacheHandle(RedisKey.getCommentSetKey(feedId), CacheService.getComments, feedId, expireProperties.getComment());
    }

    @Override
    public void delComment(String feedId, String commentId) {
        this.feedService.checkFeedExists(feedId);
        Comment comment = this.commentMapper.selectByCommentId(commentId);
        if (Objects.isNull(comment) || !Objects.equals(comment.getFeedId(), feedId)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_COMMENT_NOT_EXISTED));
        } else if (!Objects.equals(comment.getUserId(), UserHolder.getUid())) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        }
        this.commentMapper.deleteByPrimaryKey(comment.getId());
        CacheService.uncacheOne(RedisKey.getCommentSetKey(feedId), RedisKey.getCommentKey(feedId), commentId);
    }

    @Override
    public RestResult getPagedComments(String feedId, Paged page) {
        this.feedService.checkFeedExists(feedId);
        List<Comment> comments = getComments(feedId, page.getFromUId(), page.getSize());

        List<RespComment> res = comments.stream().map(cm -> {
            RespComment respComment = new RespComment();
            BeanUtils.copyProperties(cm, respComment);
            return respComment;
        }).collect(Collectors.toList());
        return RestResult.success(res);
    }

    @Override
    public List<Comment> getComments(String feedId, String fromCommentId, int size) {
        handleCommentCache(feedId);
        String zsetKey = RedisKey.getCommentSetKey(feedId);
        Long index;
        if (fromCommentId != null) {
            index = zSetOperations.rank(zsetKey, fromCommentId);
        } else {
            index = 1L;
        }
        List<Comment> comments = Lists.newArrayList();
        if (index == null) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_COMMENT_NOT_EXISTED));
        } else {
            Set keys = zSetOperations.range(zsetKey, index, size);
            if (!CollectionUtils.isEmpty(keys)) {
                comments = (List<Comment>) Optional.ofNullable(redisOptService.hmget(RedisKey.getCommentKey(feedId), Lists.newArrayList(keys.iterator()))).map(Map::values).map(Lists::newArrayList).orElse(Lists.newArrayList());
            }
        }
        return comments;
    }

    private void checkCommentUserExists(String feedId, String userId) {
        Comment comment = this.commentMapper.selectLastUserCommet(feedId, userId);
        if (Objects.isNull(comment)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        }
    }

}
