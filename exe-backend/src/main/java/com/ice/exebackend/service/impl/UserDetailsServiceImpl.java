package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.mapper.SysPermissionMapper;
import com.ice.exebackend.mapper.SysRoleMapper; // 确保导入 SysRoleMapper
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream; // 导入 Stream

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService sysUserService;

    @Autowired
    private SysRoleMapper sysRoleMapper; // 注入 SysRoleMapper

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

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

        // 查询权限标识
        List<String> permissionCodes = sysPermissionMapper.selectPermissionCodesByUserId(sysUser.getId());
        List<GrantedAuthority> permissionAuthorities = permissionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 新增：查询角色标识
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(sysUser.getId());
        List<GrantedAuthority> roleAuthorities = roleCodes.stream()
                .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                .collect(Collectors.toList());

        // 新增：合并权限和角色
        List<GrantedAuthority> authorities = Stream.concat(permissionAuthorities.stream(), roleAuthorities.stream())
                .collect(Collectors.toList());

        boolean isDisabled = !Objects.equals(sysUser.getIsEnabled(), 1);

        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(authorities) // 使用合并后的权限列表
                .disabled(isDisabled)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .build();
    }
}