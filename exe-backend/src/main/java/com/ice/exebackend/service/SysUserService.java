package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    /**
     * 创建新用户
     * @param user 用户信息
     * @return 是否成功
     */
    boolean createUser(SysUser user);

    /**
     * 根据ID删除用户（逻辑删除）
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUserById(Long userId);

    /**
     * 分页查询用户列表
     * @param page 分页参数
     * @return 用户分页数据
     */
    IPage<SysUser> getUserPage(Page<SysUser> page);
}