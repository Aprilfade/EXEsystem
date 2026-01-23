package com.ice.exebackend.controller;

import com.ice.exebackend.annotation.AuditLog;  // 【新增】审计日志注解
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.UserInfoDTO;
import com.ice.exebackend.entity.SysRole;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.mapper.SysPermissionMapper;
import com.ice.exebackend.mapper.SysRoleMapper;
import com.ice.exebackend.service.SysLoginLogService;
import com.ice.exebackend.service.SysUserService;
import com.ice.exebackend.utils.JwtUtil;
import com.ice.exebackend.utils.LoginRateLimiter;  // 新增
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

    // 【新增 - 安全加固】登录限流器
    @Autowired
    private LoginRateLimiter loginRateLimiter;

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
     * 登录接口 - 增强版（含限流防护）
     * 功能：
     * 1. 检查账户是否被锁定
     * 2. 执行认证
     * 3. 记录登录成功/失败
     * 4. 失败次数超限时锁定账户
     */
    @Operation(summary = "管理员登录", description = "管理员用户名密码登录，返回JWT Token（含暴力破解防护）")
    @PostMapping("/login")
    @AuditLog(module = "系统认证", operationType = AuditLog.OperationType.LOGIN, description = "管理员登录")  // 【新增】审计日志
    public Result login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");

        // 【安全检查1】检查账户是否被锁定
        if (loginRateLimiter.isLocked(username)) {
            long remainingSeconds = loginRateLimiter.getLockRemainingSeconds(username);
            long remainingMinutes = (remainingSeconds + 59) / 60; // 向上取整

            logger.warn("账户已被锁定，尝试登录失败: username={}, remainingSeconds={}", username, remainingSeconds);
            loginLogService.recordLoginLog(username, "LOGIN_LOCKED", getIpAddress(), getUserAgent());

            return Result.fail(String.format("账户已被锁定，请 %d 分钟后重试", remainingMinutes));
        }

        try {
            // 【步骤2】执行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.get("password"))
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // 【步骤3】认证成功，清除失败记录
            loginRateLimiter.clearFailures(username);

            logger.info("用户 '{}' 登录成功!", userDetails.getUsername());
            loginLogService.recordLoginLog(username, "LOGIN_SUCCESS", getIpAddress(), getUserAgent());

            return Result.suc(Map.of("token", token));

        } catch (AuthenticationException e) {
            // 【步骤4】认证失败，记录失败次数
            loginRateLimiter.recordFailure(username);

            int remaining = loginRateLimiter.getRemainingAttempts(username);

            logger.error("用户 '{}' 认证失败: {}, 剩余尝试次数: {}", username, e.getMessage(), remaining);
            loginLogService.recordLoginLog(username, "LOGIN_FAILURE", getIpAddress(), getUserAgent());

            // 构造失败提示信息
            if (remaining > 0) {
                return Result.fail(String.format("用户名或密码错误，还剩 %d 次尝试机会", remaining));
            } else {
                return Result.fail("登录失败次数过多，账户已被锁定30分钟");
            }
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