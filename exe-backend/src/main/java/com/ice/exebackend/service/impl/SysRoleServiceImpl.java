// src/main/java/com/ice/exebackend/service/impl/SysRoleServiceImpl.java
package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service // <-- 关键注解，将它声明为一个 Spring Bean
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    // 目前不需要额外的方法，继承 ServiceImpl 即可
}