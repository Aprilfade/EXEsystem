package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.service.SysRoleService; // 导入我们自己的 SysRoleService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService; // 修改前: IService<SysRole>

    /**
     * 获取所有角色列表
     */
    @GetMapping

    public Result getAllRoles() {
        List<SysRole> roles = sysRoleService.list();
        return Result.suc(roles);
    }
}