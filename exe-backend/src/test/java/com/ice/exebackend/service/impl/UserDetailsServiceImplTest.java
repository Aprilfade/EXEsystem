package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.mapper.SysPermissionMapper;
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.service.PermissionCacheService;
import com.ice.exebackend.service.SysUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserDetailsServiceImpl 测试类
 * 测试用户认证服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户认证服务测试")
class UserDetailsServiceImplTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysPermissionMapper sysPermissionMapper;

    @Mock
    private PermissionCacheService permissionCacheService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("应该成功加载存在的用户")
    void shouldLoadExistingUser() {
        // Given
        String username = "admin";
        SysUser mockUser = createMockUser(1L, username, "password", 1);

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(null);
        when(sysPermissionMapper.selectPermissionCodesByUserId(1L))
                .thenReturn(Arrays.asList("sys:user:list", "sys:user:add"));
        when(sysRoleMapper.selectRoleCodesByUserId(1L))
                .thenReturn(Arrays.asList("admin"));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertFalse(userDetails.isAccountExpired());
        assertFalse(userDetails.isCredentialsExpired());
        assertFalse(userDetails.isAccountLocked());

        // 验证权限包含权限码和角色
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertTrue(authorities.contains("sys:user:list"));
        assertTrue(authorities.contains("sys:user:add"));
        assertTrue(authorities.contains("ROLE_admin"));
    }

    @Test
    @DisplayName("应该抛出异常当用户不存在时")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        String username = "nonexistent";
        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(null);

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    @DisplayName("应该从缓存加载用户权限")
    void shouldLoadPermissionsFromCache() {
        // Given
        String username = "admin";
        SysUser mockUser = createMockUser(1L, username, "password", 1);
        List<String> cachedPermissions = Arrays.asList("sys:user:list", "sys:role:list");

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(cachedPermissions);
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        verify(permissionCacheService).getUserPermissions(1L);
        verify(sysPermissionMapper, never()).selectPermissionCodesByUserId(any());
        verify(permissionCacheService, never()).cacheUserPermissions(any(), any());

        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertTrue(authorities.contains("sys:user:list"));
        assertTrue(authorities.contains("sys:role:list"));
    }

    @Test
    @DisplayName("应该从数据库加载权限并缓存当缓存未命中时")
    void shouldLoadPermissionsFromDatabaseAndCacheWhenCacheMiss() {
        // Given
        String username = "admin";
        SysUser mockUser = createMockUser(1L, username, "password", 1);
        List<String> dbPermissions = Arrays.asList("sys:user:list");

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(null);
        when(sysPermissionMapper.selectPermissionCodesByUserId(1L)).thenReturn(dbPermissions);
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        verify(sysPermissionMapper).selectPermissionCodesByUserId(1L);
        verify(permissionCacheService).cacheUserPermissions(eq(1L), eq(dbPermissions));

        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertTrue(authorities.contains("sys:user:list"));
    }

    @Test
    @DisplayName("应该正确处理禁用的用户")
    void shouldHandleDisabledUser() {
        // Given
        String username = "disabled_user";
        SysUser mockUser = createMockUser(1L, username, "password", 0); // isEnabled = 0

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(Collections.emptyList());
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled()); // 用户应该被禁用
    }

    @Test
    @DisplayName("应该正确加载没有权限的用户")
    void shouldLoadUserWithNoPermissions() {
        // Given
        String username = "user_no_perms";
        SysUser mockUser = createMockUser(1L, username, "password", 1);

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(null);
        when(sysPermissionMapper.selectPermissionCodesByUserId(1L)).thenReturn(Collections.emptyList());
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("应该正确合并权限和角色")
    void shouldMergePermissionsAndRoles() {
        // Given
        String username = "admin";
        SysUser mockUser = createMockUser(1L, username, "password", 1);

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(null);
        when(sysPermissionMapper.selectPermissionCodesByUserId(1L))
                .thenReturn(Arrays.asList("sys:user:list", "sys:user:add"));
        when(sysRoleMapper.selectRoleCodesByUserId(1L))
                .thenReturn(Arrays.asList("admin", "teacher"));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 验证权限
        assertTrue(authorities.contains("sys:user:list"));
        assertTrue(authorities.contains("sys:user:add"));

        // 验证角色（应该添加ROLE_前缀）
        assertTrue(authorities.contains("ROLE_admin"));
        assertTrue(authorities.contains("ROLE_teacher"));

        // 验证总数
        assertEquals(4, authorities.size());
    }

    @Test
    @DisplayName("应该在缓存返回空列表时从数据库加载")
    void shouldLoadFromDatabaseWhenCacheReturnsEmptyList() {
        // Given
        String username = "admin";
        SysUser mockUser = createMockUser(1L, username, "password", 1);
        List<String> dbPermissions = Arrays.asList("sys:user:list");

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(Collections.emptyList());
        when(sysPermissionMapper.selectPermissionCodesByUserId(1L)).thenReturn(dbPermissions);
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        verify(sysPermissionMapper).selectPermissionCodesByUserId(1L);
        verify(permissionCacheService).cacheUserPermissions(eq(1L), eq(dbPermissions));
    }

    @Test
    @DisplayName("应该正确处理没有角色的用户")
    void shouldHandleUserWithNoRoles() {
        // Given
        String username = "user";
        SysUser mockUser = createMockUser(1L, username, "password", 1);

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(null);
        when(sysPermissionMapper.selectPermissionCodesByUserId(1L))
                .thenReturn(Arrays.asList("sys:user:view"));
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains("sys:user:view"));
    }

    @Test
    @DisplayName("应该正确设置用户账户状态")
    void shouldSetUserAccountStatus() {
        // Given
        String username = "testuser";
        SysUser mockUser = createMockUser(1L, username, "password", 1);

        when(sysUserService.getOne(any(QueryWrapper.class))).thenReturn(mockUser);
        when(permissionCacheService.getUserPermissions(1L)).thenReturn(Collections.emptyList());
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertTrue(userDetails.isEnabled());
        assertFalse(userDetails.isAccountExpired());
        assertFalse(userDetails.isCredentialsExpired());
        assertFalse(userDetails.isAccountLocked());
    }

    /**
     * 创建模拟用户对象
     */
    private SysUser createMockUser(Long id, String username, String password, Integer isEnabled) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setIsEnabled(isEnabled);
        return user;
    }
}
