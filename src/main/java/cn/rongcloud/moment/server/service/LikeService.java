package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.model.Like;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqLikeIt;

import java.util.List;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
public interface LikeService {

    RestResult likeIt(ReqLikeIt reqLike) throws RestException;

    void unLikeIt(String fid) throws RestException;

    RestResult getPagedLikes(String fid, Paged page) throws RestException;

    List<Like> getLikes(String feedId, String fromLikeId, int size);

    List<Like> batchGetLikes(List<String> likeIds);
}
