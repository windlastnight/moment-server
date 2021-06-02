package cn.rongcloud.moment.server.common.redis;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisOptService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "redisTemplate")
    private ValueOperations valueOperations;

    @Resource(name = "redisTemplate")
    private HashOperations hashOperations;

    @Resource(name = "redisTemplate")
    private ListOperations listOperations;

    @Resource(name = "redisTemplate")
    private SetOperations setOperations;

    @Resource(name = "redisTemplate")
    private ZSetOperations zSetOperations;

    public boolean expire(String key, long milliseconds) {
        if (milliseconds > 0) {
            return redisTemplate.expire(key, milliseconds, TimeUnit.MILLISECONDS);
        }
        return false;
    }

    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean deleteKey(String... key) {
        if (key != null) {
            int length = key.length;
            if (length > 0) {
                if (length == 1) {
                    return redisTemplate.delete(key[0]);
                } else {
                    return redisTemplate.delete(CollectionUtils.arrayToList(key)) == length;
                }
            }
        }
        return false;
    }

    //============================Value=============================

    public Object get(String key) {
        return StringUtils.isBlank(key) ? null : valueOperations.get(key);
    }

    public String getStringValue(String key) {
        return StringUtils.isBlank(key) ? null : (String) valueOperations.get(key);
    }

    public void set(String key, Object value) {
        valueOperations.set(key, value);
    }

    public void set(String key, Object value, long milliseconds) {
        valueOperations.set(key, value, milliseconds, TimeUnit.MILLISECONDS);
    }

    public long incr(String key) {
        return valueOperations.increment(key);
    }

    public long decr(String key) {
        return valueOperations.decrement(key);
    }

    //================================hash=================================

    public Object hashGet(String key, String item) {
        return hashOperations.get(key, item);
    }

    public Map<Object, Object> hashGetAll(String key) {
        return hashOperations.entries(key);
    }

    public void hmSet(String key, Map<String, Object> map) {
        hashOperations.putAll(key, map);
    }

    public boolean hmSet(String key, Map<String, Object> map, long milliseconds) {
        hmSet(key, map);
        if (milliseconds > 0) {
            return expire(key, milliseconds);
        }
        return false;

    }

    public void hashSet(String key, String item, Object value) {
        hashOperations.put(key, item, value);
    }

    public void hashDelete(String key, Object... item) {
        hashOperations.delete(key, item);
    }

    public boolean hashHasKey(String key, String item) {
        return hashOperations.hasKey(key, item);
    }

    public Map<String, Object> hmget(String key, List<String> fields) {
        List<Object> result = hashOperations.multiGet(key, fields);
        Map<String, Object> ans = new HashMap<>(fields.size());
        int index = 0;
        for (String field : fields) {
            if (result.get(index) == null) {
                continue;
            }
            ans.put(field, result.get(index));
            index ++;
        }
        return ans;
    }

    //============================set=============================

    public Set<Object> setMembers(String key) {
        return setOperations.members(key);
    }

    public boolean setIsMember(String key, Object value) {
        return setOperations.isMember(key, value);
    }

    public long setAdd(String key, Object... values) {
        return setOperations.add(key, values);
    }

    public long setAdd(String key, long milliseconds, Object... values) {
        Long count = setOperations.add(key, values);
        if (milliseconds > 0) {
            expire(key, milliseconds);
        }
        return count;
    }

    public long setSize(String key) {
        return setOperations.size(key);
    }

    public long setRemove(String key, Object... values) {
        return setOperations.remove(key, values);
    }

    //===============================list=================================

    public List<Object> lGet(String key, long start, long end) {
        return listOperations.range(key, start, end);
    }

    public long listSize(String key) {
        return listOperations.size(key);
    }

    public Object listIndex(String key, long index) {
        return listOperations.index(key, index);
    }

    public void listRightPush(String key, Object value) {
        listOperations.rightPush(key, value);
    }

    public boolean listRightPush(String key, Object value, long milliseconds) {
        listOperations.rightPush(key, value);
        if (milliseconds > 0) {
            return expire(key, milliseconds);
        }
        return false;
    }

    public long listRightPushAll(String key, List<Object> value) {
        return listOperations.rightPushAll(key, value);
    }

    public boolean listRightPushAll(String key, List<Object> value, long milliseconds) {
        listOperations.rightPushAll(key, value);
        if (milliseconds > 0) {
            return expire(key, milliseconds);
        }
        return false;
    }

    public void listSet(String key, long index, Object value) {
        listOperations.set(key, index, value);
    }

    public long listRemove(String key, long count, Object value) {
        return listOperations.remove(key, count, value);
    }

    //===============================zset=================================

    public boolean zsAdd(String key, Object value, double score) {
        return zSetOperations.add(key, value, score);
    }

    public Object zReverseRangeByScore(String key, double start, double end) {
        return zSetOperations.reverseRangeByScore(key, start, end);
    }

    public long batchDel(List<String> keys){
        return redisTemplate.delete(keys);
    }

}
