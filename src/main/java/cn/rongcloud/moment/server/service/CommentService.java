package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqCreateComment;

import java.util.List;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
public interface CommentService {

    RestResult comment(ReqCreateComment comment);

    void delComment(String feedId, String commentId);

    RestResult getPagedComments(String feedId, Paged page);

    List<Comment> getComments(String feedId, String fromCommentId, int size);

    List<String> getCommentNtfReceivers(Feed feed);

    List<Comment> batchGetComment(List<String> commentIds);
}
