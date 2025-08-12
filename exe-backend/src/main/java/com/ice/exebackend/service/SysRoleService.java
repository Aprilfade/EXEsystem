// src/main/java/com/ice/exebackend/service/SysRoleService.java
package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.SysRole;
import java.util.List;
import java.util.Map;

public interface SysRoleService extends IService<SysRole> {
    // 【新增】获取角色权限详情的方法
    Map<String, Object> getRolePermissions(Long roleId);

    // 【新增】更新角色权限的方法
    boolean updateRolePermissions(Long roleId, List<Long> permissionIds);
}