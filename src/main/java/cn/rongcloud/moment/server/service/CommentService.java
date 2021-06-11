package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqCreateComment;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
public interface CommentService {

    RestResult comment(ReqCreateComment comment) throws RestException;

    void delComment(String feedId, String commentId) throws RestException;

    RestResult getPagedComments(String feedId, Paged page) throws RestException;
}
