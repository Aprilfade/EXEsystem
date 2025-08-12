package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.mapper.SysPermissionMapper;
import com.ice.exebackend.mapper.SysRoleMapper; // 1. 导入 SysRoleMapper
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 2. 导入 SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors; // 3. 导入 stream Collectors

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // 【修改】将字段注入改为 final 字段
    private final SysUserService sysUserService;

    @Autowired
    private SysPermissionMapper sysPermissionMapper; // 注入

    @Autowired
    public UserDetailsServiceImpl(@Lazy SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getOne(new QueryWrapper<SysUser>().eq("username", username));

        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 根据用户ID，查询该用户的权限标识列表
        List<String> permissionCodes = sysPermissionMapper.selectPermissionCodesByUserId(sysUser.getId());

        // 将权限标识转换为Spring Security的权限对象
        List<GrantedAuthority> authorities = permissionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        boolean isDisabled = !Objects.equals(sysUser.getIsEnabled(), 1);

        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(authorities) // 使用真实的权限列表
                .disabled(isDisabled)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .build();
    }
}