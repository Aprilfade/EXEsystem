package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role_permission")
public class SysRolePermission {
    private Long roleId;
    private Long permissionId;
}