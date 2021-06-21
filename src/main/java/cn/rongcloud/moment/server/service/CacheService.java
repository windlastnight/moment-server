package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.utils.ApplicationUtil;
import cn.rongcloud.moment.server.mapper.CommentMapper;
import cn.rongcloud.moment.server.mapper.LikeMapper;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.Like;
import com.google.common.collect.Sets;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author renchaoyang
 * @date 2021/6/15
 */
public class CacheService {

    public static final String NULL_STRING = "null";

    public static final Supplier<DefaultTypedTuple> nullSupplier = () -> new DefaultTypedTuple(NULL_STRING, Double.valueOf("0"));

    public static final BiFunction<String, Long, Set<DefaultTypedTuple>> getComments = (feedId, expire) -> {
        List<Comment> comments = ApplicationUtil.getBean(CommentMapper.class).selectPagedComment(feedId, null, null);
        Set<DefaultTypedTuple> cachedData = Sets.newHashSet();
        if (!CollectionUtils.isEmpty(comments)) {
            cachedData = comments.stream().map(cm -> new DefaultTypedTuple(cm.getCommentId(), Double.valueOf(cm.getCreateDt().getTime()))).collect(Collectors.toSet());
            String commentKey = RedisKey.getCommentKey(feedId);
            RedisOptService redisOptService = getRedisOptService();
            Map<String, Comment> kvs = comments.stream().collect(Collectors.toMap(Comment::getCommentId, Function.identity()));
            redisOptService.hsetAll(commentKey, kvs);
            redisOptService.expire(commentKey, expire);
        }
        cachedData.add(nullSupplier.get());
        return cachedData;
    };

    private static RedisOptService getRedisOptService() {
        return ApplicationUtil.getBean(RedisOptService.class);
    }

    public static final BiFunction<String, Long, Set<DefaultTypedTuple>> getLikes = (feedId, expire) -> {
        List<Like> likes = ApplicationUtil.getBean(LikeMapper.class).selectPagedLike(feedId, null, null);
        Set<DefaultTypedTuple> cachedData = Sets.newHashSet();
        if (!CollectionUtils.isEmpty(likes)) {
            cachedData = likes.stream().map(cm -> new DefaultTypedTuple(cm.getLikeId(), Double.valueOf(cm.getCreateDt().getTime()))).collect(Collectors.toSet());
            String likeKey = RedisKey.getLikeKey(feedId);
            RedisOptService redisOptService = getRedisOptService();
            Map<String, Like> kvs = likes.stream().collect(Collectors.toMap(Like::getLikeId, Function.identity()));
            redisOptService.hsetAll(likeKey, kvs);
            redisOptService.expire(likeKey, expire);
        }
        cachedData.add(nullSupplier.get());
        return cachedData;
    };

    public static void cacheHandle(String cacheKey, BiFunction<String, Long, Set<DefaultTypedTuple>> cacheFun, String feedId, long expire) {
        RedisOptService redisOptService = getRedisOptService();
        boolean hasKey = redisOptService.hasKey(cacheKey);
        if (!hasKey) {
            Set<DefaultTypedTuple> cacheData = cacheFun.apply(feedId, expire);
            redisOptService.add(cacheKey, cacheData);
            redisOptService.expire(cacheKey, expire);
        }
    }

    public static <T> void cacheOne(String zsetKey, String infoKey, String id, T t, double score, Long exipire) {
        RedisOptService redisOptService = getRedisOptService();
        redisOptService.zsAdd(zsetKey, id, score);
        redisOptService.hashSet(infoKey, id, t);
        if (exipire != null && exipire > 0) {
            redisOptService.expire(zsetKey, exipire);
            redisOptService.expire(infoKey, exipire);
        }
    }

    public static void uncacheOne(String zsetKey, String infoKey, String id) {
        RedisOptService redisOptService = getRedisOptService();
        redisOptService.setRemove(zsetKey, id);
        redisOptService.hashDelete(infoKey, id);
    }

    public static Double date2Score(Date date) {
        return Double.valueOf(String.valueOf(date.getTime()));
    }

}
