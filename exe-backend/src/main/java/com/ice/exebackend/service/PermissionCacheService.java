package com.ice.exebackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 权限缓存服务
 * 用于缓存用户权限信息，减少数据库查询，提升权限验证性能
 */
@Service
public class PermissionCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PERMISSION_CACHE_PREFIX = "auth:user:";
    private static final String PERMISSION_CACHE_SUFFIX = ":permissions";
    private static final long CACHE_TTL = 30; // 30分钟

    /**
     * 获取缓存的权限列表
     * @param userId 用户ID
     * @return 权限代码列表，缓存未命中返回null
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(Long userId) {
        String key = PERMISSION_CACHE_PREFIX + userId + PERMISSION_CACHE_SUFFIX;
        Object cached = redisTemplate.opsForValue().get(key);
        return cached != null ? (List<String>) cached : null;
    }

    /**
     * 缓存用户权限列表
     * @param userId 用户ID
     * @param permissions 权限代码列表
     */
    public void cacheUserPermissions(Long userId, List<String> permissions) {
        String key = PERMISSION_CACHE_PREFIX + userId + PERMISSION_CACHE_SUFFIX;
        redisTemplate.opsForValue().set(key, permissions, CACHE_TTL, TimeUnit.MINUTES);
    }

    /**
     * 清除单个用户的权限缓存
     * 当用户权限发生变化时调用此方法
     * @param userId 用户ID
     */
    public void clearUserPermissions(Long userId) {
        String key = PERMISSION_CACHE_PREFIX + userId + PERMISSION_CACHE_SUFFIX;
        redisTemplate.delete(key);
    }

    /**
     * 批量清除多个用户的权限缓存
     * 当角色权限发生变化时调用此方法，清除该角色下所有用户的缓存
     * @param userIds 用户ID列表
     */
    public void clearUsersPermissions(List<Long> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            userIds.forEach(this::clearUserPermissions);
        }
    }
}
