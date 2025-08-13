package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.UserInfoDTO;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.entity.SysUserRole;
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.mapper.SysUserMapper;
import com.ice.exebackend.mapper.SysUserRoleMapper;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper; // 确保 SysRoleMapper 也被注入

    @Autowired
    public SysUserServiceImpl(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public boolean createUser(SysUser user) {
        SysUser existingUser = this.getOne(new QueryWrapper<SysUser>().eq("username", user.getUsername()));
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setIsEnabled(1);
        user.setIsDeleted(0);

        boolean result = this.save(user);


        // 【修改开始】
        // 如果是注册场景 (roleIds为空)，则赋予默认角色
        if (result && CollectionUtils.isEmpty(user.getRoleIds())) {
            // 假设 "普通用户" 角色的code是 'USER'
            SysRole defaultRole = sysRoleMapper.selectOne(new QueryWrapper<SysRole>().eq("code", "USER"));
            if (defaultRole != null) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(defaultRole.getId());
                sysUserRoleMapper.insert(userRole);
            }
        }
        // 如果是管理员创建用户指定了角色，则走原来的逻辑
        else if (result && !CollectionUtils.isEmpty(user.getRoleIds())) {
            updateUserRoles(user.getId(), user.getRoleIds());
        }
        // 【修改结束】
        return result;
    }

    @Override
    @Transactional
    public boolean updateUserAndRoles(SysUser user) {
        if(user.getPassword() != null && !user.getPassword().isEmpty()){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }

        boolean result = this.updateById(user);

        if (result) {
            updateUserRoles(user.getId(), user.getRoleIds());
        }
        return result;
    }

    private void updateUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().eq("user_id", userId));

        if (!CollectionUtils.isEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
    }


    @Override
    public boolean deleteUserById(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            return false;
        }
        user.setIsDeleted(1);
        return this.updateById(user);
    }

    @Override
    public IPage<UserInfoDTO> getUserPage(Page<SysUser> page) {
        IPage<SysUser> userPage = this.baseMapper.selectPage(page, new QueryWrapper<SysUser>().eq("is_deleted", 0));

        IPage<UserInfoDTO> dtoPage = userPage.convert(sysUser -> {
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            BeanUtils.copyProperties(sysUser, userInfoDTO);

            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(sysUser.getId());
            userInfoDTO.setRoles(roles);

            return userInfoDTO;
        });

        return dtoPage;
    }

    public boolean updateUserProfile(SysUser user) {
        // 创建一个新对象，只设置允许用户自己修改的字段
        SysUser userToUpdate = new SysUser();
        userToUpdate.setId(user.getId()); // ID是必须的，用于定位记录
        userToUpdate.setNickName(user.getNickName());
        userToUpdate.setAvatar(user.getAvatar()); // 【新增此行】

        // 仅当用户输入了新密码时，才进行加密和更新
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            userToUpdate.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return this.updateById(userToUpdate);
    }
}