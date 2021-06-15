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
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.CommentNotifyData;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Message;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqCreateComment;
import cn.rongcloud.moment.server.pojos.RespComment;
import cn.rongcloud.moment.server.pojos.RespCreateComment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Slf4j
@Transactional
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

    @Override
    public RestResult comment(ReqCreateComment reqComment) {
//        1.检查feedid是否有效
        Feed feed = this.feedService.checkFeedExists(reqComment.getFeedId());
//        2.检查replyto是否有效TApplicationTypeMapper.xml

        String replyTo = reqComment.getReplyTo();
        if (StringUtils.isNotBlank(replyTo)) {
            checkCommentUserExists(reqComment.getFeedId(), replyTo);
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(reqComment, comment);
        comment.setCreateDt(new Date());
        comment.setUserId(UserHolder.getUid());
        comment.setCommentId(IdentifierUtils.uuid24());
        this.commentMapper.insertSelective(comment);

        List<String> receivers = this.getCommentNtfReceivers(feed);
        CommentNotifyData commentNotifyData = new CommentNotifyData();
        BeanUtils.copyProperties(comment, commentNotifyData);
        commentNotifyData.setCreateDt(comment.getCreateDt().getTime());
        this.imHelper.publishCommentNtf(receivers, commentNotifyData, MomentsCommentType.COMMENT);

        Message message = new Message();
        message.setMessageId(comment.getCommentId());
        message.setUserId(UserHolder.getUid());
        message.setCreateDt(DateTimeUtils.currentDt());
        message.setMessageType(MomentsCommentType.COMMENT.getType());
        message.setStatus(MessageStatus.NORMAL.getValue());
        messageService.saveMessage(message);
        if (receivers != null && !receivers.isEmpty()) {
            for (String receiverId: receivers) {
                if (receiverId.equals(UserHolder.getUid())) {
                    continue;
                }
                redisOptService.zsAdd(RedisKey.getUserUnreadMessageKey(receiverId), message, DateTimeUtils.currentDt().getTime());
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
        Long fromAutoIncCommentId = null;
        if (StringUtils.isNotBlank(fromCommentId)) {
            Comment comment = commentMapper.selectByCommentId(fromCommentId);
            if (Objects.isNull(comment)) {
                throw new RestException(RestResult.generic(RestResultCode.ERR_COMMENT_NOT_EXISTED));
            }
            fromAutoIncCommentId = comment.getId();
        }
        return this.commentMapper.selectPagedComment(feedId, fromAutoIncCommentId, size);
    }

    private void checkCommentUserExists(String feedId, String userId) {
        Comment comment = this.commentMapper.selectLastUserCommet(feedId, userId);
        if (Objects.isNull(comment)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        }
    }

}
