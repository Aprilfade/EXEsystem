package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.UserInfoDTO;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.mapper.SysPermissionMapper;
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.service.SysLoginLogService;
import com.ice.exebackend.service.SysUserService;
import com.ice.exebackend.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理员认证", description = "管理员登录、注册、登出、获取用户信息等接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    // --- 以下为登录日志功能所需的新增注入 ---
    @Autowired
    private SysLoginLogService loginLogService;

    @Autowired
    private HttpServletRequest request;

    private String getIpAddress() {
        final String UNKNOWN = "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 【关键】处理多级代理的情况，获取第一个真实的IP
        if (ip != null && ip.length() > 0 && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }

        // 【可选】对于本地开发，将IPv6的localhost转为IPv4格式，更易读
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    private String getUserAgent() {
        return request.getHeader("User-Agent");
    }


    /**
     * 登录接口优化
     * 保留 try-catch 仅为了记录失败日志，最后 re-throw 异常
     */
    @Operation(summary = "管理员登录", description = "管理员用户名密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.get("password"))
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            logger.info("用户 '{}' 登录成功!", userDetails.getUsername());
            // 记录成功日志
            loginLogService.recordLoginLog(username, "LOGIN_SUCCESS", getIpAddress(), getUserAgent());

            return Result.suc(Map.of("token", token));

        } catch (AuthenticationException e) {
            logger.error("用户 '{}' 认证失败: {}", username, e.getMessage());
            // 1. 记录失败日志 (这是我们保留 try-catch 的唯一原因)
            loginLogService.recordLoginLog(username, "LOGIN_FAILURE", getIpAddress(), getUserAgent());

            // 2. 【关键】重新抛出异常，交给 GlobalExceptionHandler 统一处理返回 Result.fail(...)
            throw e;
        }
    }

    @Operation(summary = "管理员登出", description = "管理员退出登录，记录登出日志")
    @PostMapping("/logout")
    public Result logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        loginLogService.recordLoginLog(username, "LOGOUT", getIpAddress(), getUserAgent());
        return Result.suc();
    }

    /**
     * 注册接口优化
     * 移除了 try-catch，代码更清爽
     */
    @Operation(summary = "管理员注册", description = "新用户注册管理员账号")
    @PostMapping("/register")
    public Result register(@RequestBody SysUser user) {
        // 如果 createUser 抛出 "用户名已存在" 等异常，会被 GlobalExceptionHandler 捕获并返回给前端
        sysUserService.createUser(user);
        return Result.suc("注册成功，请登录！");
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息，包括角色和权限")
    @GetMapping("/me")
    public Result getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();

        if (user == null) {
            return Result.fail("用户不存在");
        }

        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getId());
        List<String> permissions = sysPermissionMapper.selectPermissionCodesByUserId(user.getId());

        // ... 组装数据 ...
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);
        userInfoDTO.setRoles(sysRoleMapper.selectRolesByUserId(user.getId()));
        return Result.suc(Map.of("user", userInfoDTO, "permissions", sysPermissionMapper.selectPermissionCodesByUserId(user.getId())));
    }
}