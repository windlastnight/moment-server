package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.pojos.ReqFeedPublish;

import java.util.List;

/**
 * Created by sunyinglong on 2020/6/3
 */
public interface FeedService {
    RestResult publish(String userId, ReqFeedPublish data);

    RestResult delete(String userId, String feedId);

    RestResult getFeedInfo(String feedId, boolean withComments, int commentSize, boolean withLikes, int likeSize);

    RestResult batchGetFeedInfo(List<String> feedIds, boolean withComments, int commentSize, boolean withLikes, int likeSize);

    RestResult getNewFeed(String latestFeedId);

    Feed checkFeedExists(String feedId) throws RestException;
}
