package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.mapper.SysUserMapper;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // 1. 导入 @Lazy 注解
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    // 2. 使用构造函数进行注入，并在 passwordEncoder 参数前添加 @Lazy
    @Autowired
    public SysUserServiceImpl(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean createUser(SysUser user) {
        // 检查用户名是否已存在
        SysUser existingUser = this.getOne(new QueryWrapper<SysUser>().eq("username", user.getUsername()));
        if (existingUser != null) {
            // 可以抛出自定义异常，由全局异常处理器捕获
            throw new RuntimeException("用户名已存在");
        }

        // 对密码进行BCrypt加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsEnabled(1); // 默认启用
        user.setIsDeleted(0); // 默认未删除
        return this.save(user);
    }

    @Override
    public boolean deleteUserById(Long userId) {
        // 采用逻辑删除，而非物理删除
        SysUser user = this.getById(userId);
        if (user == null) {
            return false;
        }
        user.setIsDeleted(1); // 设置逻辑删除标志
        return this.updateById(user);
    }

    @Override
    public IPage<SysUser> getUserPage(Page<SysUser> page) {
        // 查询未被逻辑删除的用户
        return this.baseMapper.selectPage(page, new QueryWrapper<SysUser>().eq("is_deleted", 0));
    }
}