package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map; // 确保导入了 Map

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    // 【新增】定义一个新的方法，用于一次性查询所有权限及角色的选中状态
    List<Map<String, Object>> selectPermissionsWithCheckedStatusByRoleId(@Param("roleId") Long roleId);
}