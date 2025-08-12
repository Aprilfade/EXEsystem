package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
// import lombok.Data; // 移除导入

// @Data // 移除 @Data 注解
@TableName("sys_role_permission")
public class SysRolePermission {
    private Long roleId;
    private Long permissionId;

    // 【新增】手动添加 Getter 和 Setter 方法
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }
}