package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysPermission;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.entity.SysRolePermission;
import com.ice.exebackend.mapper.SysPermissionMapper;
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.mapper.SysRolePermissionMapper;
import com.ice.exebackend.mapper.SysUserRoleMapper;
import com.ice.exebackend.service.PermissionCacheService;
import com.ice.exebackend.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Override
    // 【核心修复】替换整个方法，使用新的高效查询
    public Map<String, Object> getRolePermissions(Long roleId) {
        // 1. 一次性从数据库查询出所有权限，并标记好哪些是当前角色已选中的
        List<Map<String, Object>> permissionsWithStatus = permissionMapper.selectPermissionsWithCheckedStatusByRoleId(roleId);

        // 2. 从查询结果中筛选出所有权限对象
        List<SysPermission> allPermissions = permissionsWithStatus.stream()
                .map(p -> {
                    SysPermission perm = new SysPermission();
                    perm.setId(((Number) p.get("id")).longValue());
                    perm.setName((String) p.get("name"));
                    perm.setCode((String) p.get("code"));
                    perm.setType(((Number) p.get("type")).intValue());
                    Object parentIdObj = p.get("parent_id");
                    perm.setParentId(parentIdObj != null ? ((Number) parentIdObj).longValue() : 0L);
                    return perm;
                })
                .collect(Collectors.toList());

        // 3. 从查询结果中筛选出所有已选中的权限ID
        List<Long> checkedIds = permissionsWithStatus.stream()
                .filter(p -> ((Number) p.get("is_checked")).intValue() == 1)
                .map(p -> ((Number) p.get("id")).longValue())
                .collect(Collectors.toList());

        // 4. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("allPermissions", allPermissions);
        result.put("checkedIds", checkedIds);
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_details", allEntries = true) // 清除所有用户的权限缓存
    public boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // 1. 先删除该角色所有的旧权限
        rolePermissionMapper.delete(new QueryWrapper<SysRolePermission>().eq("role_id", roleId));

        // 2. 【优化】如果传入了新的权限ID列表，则批量插入
        if (permissionIds != null && !permissionIds.isEmpty()) {
            // 构建批量插入的数据列表
            List<SysRolePermission> rolePermissions = permissionIds.stream()
                    .map(permissionId -> {
                        SysRolePermission rp = new SysRolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permissionId);
                        return rp;
                    })
                    .collect(Collectors.toList());

            // 使用批量插入方法，大幅提升性能
            rolePermissionMapper.insertBatch(rolePermissions);
        }

        // 3. 【新增】清除该角色下所有用户的权限缓存，确保权限实时生效
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
        permissionCacheService.clearUsersPermissions(userIds);

        return true;
    }
}