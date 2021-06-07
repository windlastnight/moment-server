package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.pojos.ReqFeedPublish;

import java.util.List;

/**
 * Created by sunyinglong on 2020/6/3
 */
public interface FeedService {
    RestResult publish(String userId, ReqFeedPublish data);

    RestResult delete(String userId, String feedId);

    RestResult getFeedInfo(String userId, String feedId);

    RestResult batchGetFeedInfo(String userId, List<String> feedIds);
}
