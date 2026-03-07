package com.ice.exebackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PermissionCacheService 测试类
 * 测试权限缓存服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("权限缓存服务测试")
class PermissionCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PermissionCacheService permissionCacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("应该成功缓存用户权限")
    void shouldCacheUserPermissions() {
        // Given
        Long userId = 1L;
        List<String> permissions = Arrays.asList("sys:user:list", "sys:user:add", "sys:user:edit");

        // When
        permissionCacheService.cacheUserPermissions(userId, permissions);

        // Then
        verify(valueOperations).set(
                eq("auth:user:1:permissions"),
                eq(permissions),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("应该成功获取缓存的用户权限")
    void shouldGetCachedUserPermissions() {
        // Given
        Long userId = 1L;
        List<String> expectedPermissions = Arrays.asList("sys:user:list", "sys:user:add");
        when(valueOperations.get("auth:user:1:permissions")).thenReturn(expectedPermissions);

        // When
        List<String> actualPermissions = permissionCacheService.getUserPermissions(userId);

        // Then
        assertNotNull(actualPermissions);
        assertEquals(2, actualPermissions.size());
        assertEquals(expectedPermissions, actualPermissions);
        verify(valueOperations).get("auth:user:1:permissions");
    }

    @Test
    @DisplayName("应该在缓存未命中时返回null")
    void shouldReturnNullWhenCacheMiss() {
        // Given
        Long userId = 999L;
        when(valueOperations.get("auth:user:999:permissions")).thenReturn(null);

        // When
        List<String> permissions = permissionCacheService.getUserPermissions(userId);

        // Then
        assertNull(permissions);
        verify(valueOperations).get("auth:user:999:permissions");
    }

    @Test
    @DisplayName("应该成功清除单个用户权限缓存")
    void shouldClearSingleUserPermissions() {
        // Given
        Long userId = 1L;
        when(redisTemplate.delete("auth:user:1:permissions")).thenReturn(true);

        // When
        permissionCacheService.clearUserPermissions(userId);

        // Then
        verify(redisTemplate).delete("auth:user:1:permissions");
    }

    @Test
    @DisplayName("应该成功批量清除多个用户权限缓存")
    void shouldClearMultipleUsersPermissions() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);

        // When
        permissionCacheService.clearUsersPermissions(userIds);

        // Then
        verify(redisTemplate, times(3)).delete(anyString());
        verify(redisTemplate).delete("auth:user:1:permissions");
        verify(redisTemplate).delete("auth:user:2:permissions");
        verify(redisTemplate).delete("auth:user:3:permissions");
    }

    @Test
    @DisplayName("应该处理空用户ID列表的批量清除")
    void shouldHandleEmptyUserListInBatchClear() {
        // Given
        List<Long> emptyList = Arrays.asList();

        // When
        permissionCacheService.clearUsersPermissions(emptyList);

        // Then
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("应该处理null用户ID列表的批量清除")
    void shouldHandleNullUserListInBatchClear() {
        // When
        permissionCacheService.clearUsersPermissions(null);

        // Then
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("应该使用正确的缓存键格式")
    void shouldUseCorrectCacheKeyFormat() {
        // Given
        Long userId = 123L;
        List<String> permissions = Arrays.asList("sys:role:list");

        // When
        permissionCacheService.cacheUserPermissions(userId, permissions);

        // Then
        verify(valueOperations).set(
                eq("auth:user:123:permissions"),
                any(),
                anyLong(),
                any(TimeUnit.class)
        );
    }

    @Test
    @DisplayName("应该使用30分钟的缓存过期时间")
    void shouldUse30MinutesCacheTTL() {
        // Given
        Long userId = 1L;
        List<String> permissions = Arrays.asList("sys:user:list");

        // When
        permissionCacheService.cacheUserPermissions(userId, permissions);

        // Then
        verify(valueOperations).set(
                anyString(),
                any(),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("应该正确处理包含特殊权限代码的列表")
    void shouldHandleSpecialPermissionCodes() {
        // Given
        Long userId = 1L;
        List<String> permissions = Arrays.asList(
                "sys:user:*",
                "sys:role:list:all",
                "admin:super:privilege"
        );

        // When
        permissionCacheService.cacheUserPermissions(userId, permissions);

        // Then
        verify(valueOperations).set(
                eq("auth:user:1:permissions"),
                eq(permissions),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("应该能缓存空权限列表")
    void shouldCacheEmptyPermissionList() {
        // Given
        Long userId = 1L;
        List<String> emptyPermissions = Arrays.asList();

        // When
        permissionCacheService.cacheUserPermissions(userId, emptyPermissions);

        // Then
        verify(valueOperations).set(
                eq("auth:user:1:permissions"),
                eq(emptyPermissions),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("应该能处理大量权限的缓存")
    void shouldHandleLargePermissionList() {
        // Given
        Long userId = 1L;
        List<String> largePermissions = Arrays.asList(
                "perm1", "perm2", "perm3", "perm4", "perm5",
                "perm6", "perm7", "perm8", "perm9", "perm10",
                "perm11", "perm12", "perm13", "perm14", "perm15"
        );

        // When
        permissionCacheService.cacheUserPermissions(userId, largePermissions);

        // Then
        verify(valueOperations).set(
                anyString(),
                eq(largePermissions),
                anyLong(),
                any(TimeUnit.class)
        );
    }

    @Test
    @DisplayName("应该为不同用户使用不同的缓存键")
    void shouldUseDifferentKeysForDifferentUsers() {
        // Given
        List<String> permissions = Arrays.asList("sys:user:list");

        // When
        permissionCacheService.cacheUserPermissions(1L, permissions);
        permissionCacheService.cacheUserPermissions(2L, permissions);

        // Then
        verify(valueOperations).set(
                eq("auth:user:1:permissions"),
                any(),
                anyLong(),
                any(TimeUnit.class)
        );
        verify(valueOperations).set(
                eq("auth:user:2:permissions"),
                any(),
                anyLong(),
                any(TimeUnit.class)
        );
    }
}
