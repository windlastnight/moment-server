package cn.rongcloud.moment.server.jobs;

import cn.rongcloud.moment.server.common.im.IMHelper;
import cn.rongcloud.moment.server.common.rce.RceHelper;
import cn.rongcloud.moment.server.common.rce.RceRespResult;
import cn.rongcloud.moment.server.common.redis.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class MomentPublishNotifyJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private IMHelper imHelper;

    @Autowired
    private RceHelper rceHelper;

    /**
     * Lua脚本 (获取后删除)
     */
    private static final String LUA_SCRIPT_GET_AND_DELETE =
            "local current = redis.call('smembers', KEYS[1]);\n" +
                    "if (current) then\n" +
                    "    redis.call('del', KEYS[1]);\n" +
                    "end\n" +
                    "return current;";

    @Scheduled(cron = "${moment.publish_notify_corn}")
    public void publishNotify() {
        log.info("moment publish notify job start...");
        List<String> keys = new ArrayList<>();
        keys.add(RedisKey.getMomentPublishNotifyUsersKey());
        RedisScript<List> luaScript = new DefaultRedisScript<>(LUA_SCRIPT_GET_AND_DELETE, List.class);
        List result = redisTemplate.execute(luaScript, keys);
        if (result != null && !result.isEmpty()) {
            List<String> orgIds = (List<String>) result.get(0);
            RceRespResult staffIds = rceHelper.queryAllStaffId(orgIds, null);
            imHelper.publishFeedNtf(staffIds.getResult());
        }
        log.info("moment publish notify job end...");
    }
}
