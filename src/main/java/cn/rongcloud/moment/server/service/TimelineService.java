package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;

/**
 * Created by sunyinglong on 2020/6/3
 */
public interface TimelineService {
    RestResult getMineTimeLine(String fromFeedId, Integer size);
    RestResult getUserTimeLine(String fromFeedId, Integer size, String userId);
}
