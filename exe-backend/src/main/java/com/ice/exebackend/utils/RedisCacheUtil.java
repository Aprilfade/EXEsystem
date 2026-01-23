package com.ice.exebackend.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis缓存工具类（增强版）
 *
 * 功能：
 * 1. 统一的缓存操作接口
 * 2. 防止缓存穿透（空值缓存）
 * 3. 防止缓存雪崩（随机过期时间）
 * 4. 统一的key命名规范
 * 5. 自动序列化/反序列化
 *
 * @author Ice
 * @date 2026-01-12
 */
@Component
public class RedisCacheUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheUtil.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 默认过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 3600; // 1小时

    /**
     * 空值缓存过期时间（秒）- 防止缓存穿透
     */
    private static final long NULL_VALUE_EXPIRE_SECONDS = 300; // 5分钟

    /**
     * 缓存key前缀
     */
    public static final String KEY_PREFIX_USER = "user:";
    public static final String KEY_PREFIX_STUDENT = "student:";
    public static final String KEY_PREFIX_QUESTION = "question:";
    public static final String KEY_PREFIX_PAPER = "paper:";
    public static final String KEY_PREFIX_EXAM = "exam:";
    public static final String KEY_PREFIX_SUBJECT = "subject:";
    public static final String KEY_PREFIX_KNOWLEDGE = "knowledge:";

    // ==================== 基础操作 ====================

    /**
     * 设置缓存（使用默认过期时间）
     */
    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存（指定过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            logger.error("Redis设置缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 设置缓存（永不过期）
     */
    public void setPermanent(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            logger.error("Redis设置永久缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis获取缓存失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取缓存（带类型转换）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }

            // 如果已经是目标类型，直接返回
            if (clazz.isInstance(value)) {
                return (T) value;
            }

            // 使用Jackson进行类型转换
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            logger.error("Redis获取缓存失败: key={}, class={}, error={}", key, clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取缓存列表（带类型转换）
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }

            return objectMapper.convertValue(value, new TypeReference<List<T>>() {});
        } catch (Exception e) {
            logger.error("Redis获取列表缓存失败: key={}, class={}, error={}", key, clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            logger.error("Redis删除缓存失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除缓存
     */
    public long delete(Collection<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Redis批量删除缓存失败: keys={}, error={}", keys, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 删除指定前缀的所有缓存
     */
    public long deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                Long count = redisTemplate.delete(keys);
                return count != null ? count : 0;
            }
            return 0;
        } catch (Exception e) {
            logger.error("Redis按前缀删除缓存失败: prefix={}, error={}", prefix, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Redis检查key存在性失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            logger.error("Redis设置过期时间失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取过期时间（秒）
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2; // -2表示key不存在，-1表示永不过期
        } catch (Exception e) {
            logger.error("Redis获取过期时间失败: key={}, error={}", key, e.getMessage(), e);
            return -2;
        }
    }

    // ==================== 高级操作（防止缓存穿透和雪崩） ====================

    /**
     * 获取缓存，如果不存在则从数据库加载并缓存
     * 【防止缓存穿透】：如果数据库也没有，会缓存空值
     *
     * @param key          缓存key
     * @param clazz        返回值类型
     * @param dbLoader     数据库加载函数
     * @param expireSeconds 过期时间（秒）
     * @return 缓存值
     */
    public <T> T getOrLoad(String key, Class<T> clazz, Supplier<T> dbLoader, long expireSeconds) {
        try {
            // 1. 先从缓存获取
            T cachedValue = get(key, clazz);
            if (cachedValue != null) {
                return cachedValue;
            }

            // 2. 缓存未命中，从数据库加载
            T dbValue = dbLoader.get();

            // 3. 将结果缓存（包括空值，防止缓存穿透）
            if (dbValue != null) {
                // 【防止缓存雪崩】：添加随机过期时间（±10%）
                long randomExpire = addRandomExpire(expireSeconds);
                set(key, dbValue, randomExpire, TimeUnit.SECONDS);
            } else {
                // 空值缓存，使用较短的过期时间
                set(key, "NULL_PLACEHOLDER", NULL_VALUE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            }

            return dbValue;

        } catch (Exception e) {
            logger.error("Redis getOrLoad失败: key={}, error={}", key, e.getMessage(), e);
            // 缓存失败时直接从数据库加载
            return dbLoader.get();
        }
    }

    /**
     * 获取列表缓存，如果不存在则从数据库加载并缓存
     */
    public <T> List<T> getListOrLoad(String key, Class<T> clazz, Supplier<List<T>> dbLoader, long expireSeconds) {
        try {
            // 1. 先从缓存获取
            List<T> cachedList = getList(key, clazz);
            if (cachedList != null) {
                return cachedList;
            }

            // 2. 缓存未命中，从数据库加载
            List<T> dbList = dbLoader.get();

            // 3. 将结果缓存
            if (dbList != null && !dbList.isEmpty()) {
                long randomExpire = addRandomExpire(expireSeconds);
                set(key, dbList, randomExpire, TimeUnit.SECONDS);
            } else {
                // 空列表也缓存，防止缓存穿透
                set(key, "EMPTY_LIST_PLACEHOLDER", NULL_VALUE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            }

            return dbList;

        } catch (Exception e) {
            logger.error("Redis getListOrLoad失败: key={}, error={}", key, e.getMessage(), e);
            return dbLoader.get();
        }
    }

    /**
     * 添加随机过期时间（±10%），防止缓存雪崩
     */
    private long addRandomExpire(long baseExpire) {
        // 在基础过期时间上增加 ±10% 的随机值
        double randomFactor = 0.9 + Math.random() * 0.2; // 0.9 ~ 1.1
        return (long) (baseExpire * randomFactor);
    }

    // ==================== Hash操作 ====================

    /**
     * Hash设置值
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            logger.error("Redis Hash设置失败: key={}, field={}, error={}", key, field, e.getMessage(), e);
        }
    }

    /**
     * Hash获取值
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            logger.error("Redis Hash获取失败: key={}, field={}, error={}", key, field, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Hash删除字段
     */
    public long hDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            logger.error("Redis Hash删除失败: key={}, error={}", key, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Hash获取所有键值对
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            logger.error("Redis Hash获取所有键值对失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    // ==================== 计数器操作 ====================

    /**
     * 递增
     */
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            logger.error("Redis递增失败: key={}, delta={}, error={}", key, delta, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 递减
     */
    public long decrement(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            logger.error("Redis递减失败: key={}, delta={}, error={}", key, delta, e.getMessage(), e);
            return 0;
        }
    }

    // ==================== Set操作 ====================

    /**
     * Set添加元素
     */
    public long sAdd(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Redis Set添加失败: key={}, error={}", key, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Set获取所有成员
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            logger.error("Redis Set获取成员失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Set判断是否包含元素
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            logger.error("Redis Set判断成员失败: key={}, value={}, error={}", key, value, e.getMessage(), e);
            return false;
        }
    }
}
