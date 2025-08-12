package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysPermission;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.entity.SysRolePermission;
import com.ice.exebackend.mapper.SysPermissionMapper;
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.mapper.SysRolePermissionMapper;
import com.ice.exebackend.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 【重要】确保导入此注解

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Override
    @Transactional(readOnly = true) // 【核心修复】添加只读事务注解
    public Map<String, Object> getRolePermissions(Long roleId) {
        // 1. 查询所有可用的权限
        List<SysPermission> allPermissions = permissionMapper.selectList(null);

        // 2. 查询当前角色已拥有的权限ID列表
        QueryWrapper<SysRolePermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        List<Long> checkedIds = rolePermissionMapper.selectList(queryWrapper)
                .stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 3. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("allPermissions", allPermissions);
        result.put("checkedIds", checkedIds);
        return result;
    }

    @Override
    @Transactional
    public boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // 1. 先删除该角色所有的旧权限
        rolePermissionMapper.delete(new QueryWrapper<SysRolePermission>().eq("role_id", roleId));

        // 2. 如果传入了新的权限ID列表，则批量插入
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }
        return true;
    }
}