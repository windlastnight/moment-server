package cn.rongcloud.moment.server.common.redis;

import cn.rongcloud.moment.server.common.CustomerConstant;

/**
 * Created by sunyinglong on 2020/6/5
 */
public class RedisKey {

    private RedisKey() {}

    public static String getMomentPublishNotifyUsersKey() {
        return CustomerConstant.SERVICE_NAME + "|publish_notify_users";
    }

    public static String getUserUnreadMessageKey(String userId) {
        return CustomerConstant.SERVICE_NAME + "|user_unread_message|" + userId;
    }

    public static final String COMMENT_SET_KEY = "feed:%s:commentids";
    public static final String COMMENT_INFO_KEY = "feed:%s:comment:info";
    public static final String LIKE_SET_KEY = "feed:%s:likeids";
    public static final String LIKE_INFO_KEY = "feed:%s:like:info";
    public static final String FEED_SET_KEY = "feed:ids";
    public static final String FEED_INFO_KEY = "feed:info";

    public static String getFeedSetKey() {
        return RedisKey.FEED_SET_KEY;
    }
    public static String getFeedKey() {
        return RedisKey.FEED_INFO_KEY;
    }
     public static String getCommentSetKey(String feedId) {
        return String.format(RedisKey.COMMENT_SET_KEY, feedId);
    }
    public static String getCommentKey(String feedId) {
        return String.format(RedisKey.COMMENT_INFO_KEY, feedId);
    }
    public static String getLikeKey(String feedId) {
        return String.format(RedisKey.LIKE_INFO_KEY, feedId);
    }

    public static String getLikeSetKey(String feedId) {
        return String.format(RedisKey.LIKE_SET_KEY, feedId);
    }
}
