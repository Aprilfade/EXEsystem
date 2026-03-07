package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    /**
     * 批量插入角色权限关系
     * @param rolePermissions 角色权限列表
     * @return 插入成功的记录数
     */
    int insertBatch(@Param("list") List<SysRolePermission> rolePermissions);
}