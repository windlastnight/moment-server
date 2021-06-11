package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.im.IMHelper;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.IdentifierUtils;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.mapper.CommentMapper;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqCreateComment;
import cn.rongcloud.moment.server.pojos.RespComment;
import cn.rongcloud.moment.server.pojos.RespCreateComment;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    IMHelper imHelper;

    @Override
    public RestResult comment(ReqCreateComment reqComment) throws RestException {
//        1.检查feedid是否有效
        Feed feed = this.feedService.checkFeedExists(reqComment.getFeedId());
//        2.检查replyto是否有效TApplicationTypeMapper.xml

        String replyTo = reqComment.getReplyTo();
        if (Objects.nonNull(replyTo)) {
            checkCommentUserExists(reqComment.getFeedId(), replyTo);
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(reqComment, comment);
        comment.setCreateDt(new Date());
        comment.setUserId(UserHolder.getUid());
        comment.setCommentId(IdentifierUtils.uuid24());
        this.commentMapper.insertSelective(comment);

        List<String> receivers = this.getCommentNtfRecivers(feed);
        receivers.add(feed.getUserId());
        this.imHelper.publishCommentNtf(receivers);

        RespCreateComment respCreateComment = new RespCreateComment();
        BeanUtils.copyProperties(comment, respCreateComment);
        return RestResult.success(respCreateComment);
    }

    private List<String> getCommentNtfRecivers(Feed feed) {
        List<String> receivers = this.commentMapper.getAllCommentAndLikeUserIds(feed.getFeedId());
        String feedOwner = feed.getUserId();
        if (!receivers.contains(feedOwner)) {
            receivers.add(feedOwner);
        }
        return receivers;
    }

    @Override
    public void delComment(String feedId, String commentId) throws RestException {
        this.feedService.checkFeedExists(feedId);
        Comment comment = this.commentMapper.selectByPrimaryKey(commentId);
        if (Objects.isNull(comment) || !Objects.equals(comment.getFeedId(), feedId)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        } else if (!Objects.equals(comment.getUserId(), UserHolder.getUid())) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        }
        this.commentMapper.deleteByPrimaryKey(commentId);
    }

    @Override
    public RestResult getPagedComments(String feedId, Paged page) throws RestException {
        this.feedService.checkFeedExists(feedId);
        if (StringUtils.isNotBlank(page.getFromId())) {
            Comment comment = this.commentMapper.selectByPrimaryKey(page.getFromId());
            if (Objects.isNull(comment)) {
                throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
            }
        }

        List<Comment> comments = this.commentMapper.selectPagedComment(page);
        List<RespComment> res = comments.stream().map(cm -> {
            RespComment respComment = new RespComment();
            BeanUtils.copyProperties(cm, respComment);
            return respComment;
        }).collect(Collectors.toList());
        return RestResult.success(res);
    }

    private void checkCommentUserExists(String feedId, String userId) throws RestException {
        Comment comment = this.commentMapper.selectLastUserCommet(feedId, userId);
        if (Objects.isNull(comment)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
        }
    }

}
