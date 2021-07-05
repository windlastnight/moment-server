package cn.rongcloud.moment.server.jobs;

import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.model.CacheExpireProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author renchaoyang
 */
@Slf4j
@Component
public class ClearFeedCacheJob implements InitializingBean {

    @Resource
    CacheExpireProperties cacheExpireProperties;

    @Resource(name = "redisTemplate")
    ZSetOperations zSetOperations;

    @Resource
    RedisOptService redisOptService;

    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void fillOrgInfoUserStatistic() {
        log.info("ClearFeedCacheJob execute time:{}", LocalDateTime.now());
        handleJob();
    }

    private void handleJob() {
        double min = 0d;
        double max = Double.valueOf(String.valueOf(DateTimeUtils.currentDt().getTime() - cacheExpireProperties.getIntervalS()));
        Set keys = zSetOperations.rangeByScore(RedisKey.getFeedSetKey(), min, max);
        zSetOperations.removeRangeByScore(RedisKey.getFeedSetKey(), min, max);
        if (!CollectionUtils.isEmpty(keys)) {
            redisOptService.hashDelete(RedisKey.getFeedKey(), keys);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
