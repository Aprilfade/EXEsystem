package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.dto.UserInfoDTO; // 导入 UserInfoDTO
import com.ice.exebackend.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    boolean createUser(SysUser user);

    boolean updateUserAndRoles(SysUser user);

    boolean deleteUserById(Long userId);

    // 修改前：IPage<SysUser> getUserPage(Page<SysUser> page);
    // 修改后：返回类型变更为 IPage<UserInfoDTO>
    IPage<UserInfoDTO> getUserPage(Page<SysUser> page);
    boolean updateUserProfile(SysUser user); // <-- 新增此行
}