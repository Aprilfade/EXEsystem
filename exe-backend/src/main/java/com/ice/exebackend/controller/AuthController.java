package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.UserInfoDTO;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.entity.SysUser; // 导入 SysUser
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.service.SysUserService; // 导入 SysUserService
import com.ice.exebackend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; // 导入 SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*; // 导入 GetMapping

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserService sysUserService; // 注入 SysUserService


    @Autowired
    private SysRoleMapper sysRoleMapper; // 5. 注入 SysRoleMapper

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password"))
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            logger.info("用户 '{}' 登录成功!", userDetails.getUsername());
            return Result.suc(Map.of("token", token));
        } catch (Exception e) {
            logger.error("认证失败: {}", e.getMessage());
            return Result.fail();
        }
    }


    /**
     * 修改后：获取当前登录用户信息及其角色
     */
    @GetMapping("/me")
    public Result getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();

        if (user == null) {
            return Result.fail();
        }

        // 6. 查询该用户的角色列表
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getId());

        // 7. 构建 DTO 并返回
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO); // 复制基础属性
        userInfoDTO.setRoles(roles); // 设置角色列表

        return Result.suc(userInfoDTO);
    }
}