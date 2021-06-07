package cn.rongcloud.moment.server.common.redis;

import cn.rongcloud.moment.server.common.CustomerConstant;

/**
 * Created by sunyinglong on 2020/6/5
 */
public class RedisKey {

    private RedisKey() {}

    public static String getMomentPublishNotifyUsersKey() {
        return CustomerConstant.SERVICE_NAME + "|publish_notify_users|";
    }
}
