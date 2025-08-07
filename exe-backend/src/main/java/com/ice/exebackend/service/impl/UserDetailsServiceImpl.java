package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.mapper.SysRoleMapper; // 1. 导入 SysRoleMapper
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SysUserService sysUserService;

    // 4. 注入 SysRoleMapper 以便查询角色
    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getOne(new QueryWrapper<SysUser>().eq("username", username));

        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 5. 根据用户ID，从数据库动态查询该用户的角色列表
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(sysUser.getId());

        // 6. 将角色编码（如 "SUPER_ADMIN", "ADMIN"）转换为Spring Security的权限对象
        //    我们约定，角色的权限以 "ROLE_" 为前缀
        List<GrantedAuthority> authorities = roleCodes.stream()
                .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                .collect(Collectors.toList());

        boolean isDisabled = !Objects.equals(sysUser.getIsEnabled(), 1);

        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(authorities) // 7. 将从数据库查出的真实权限赋予用户
                .disabled(isDisabled)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .build();
    }
}