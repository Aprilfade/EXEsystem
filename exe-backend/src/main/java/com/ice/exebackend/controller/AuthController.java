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


    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginRequest) {
        // 【核心修复】将 username 的定义移动到方法开头
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

            // 记录失败日志 (现在可以安全地访问 username)
            loginLogService.recordLoginLog(username, "LOGIN_FAILURE", getIpAddress(), getUserAgent());

            return Result.fail("用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    public Result logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        loginLogService.recordLoginLog(username, "LOGOUT", getIpAddress(), getUserAgent());
        return Result.suc();
    }

    @PostMapping("/register")
    public Result register(@RequestBody SysUser user) {
        try {
            boolean success = sysUserService.createUser(user);
            return success ? Result.suc("注册成功，请登录！") : Result.fail("注册失败，请稍后再试。");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/me")
    public Result getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();

        if (user == null) {
            return Result.fail("用户不存在");
        }

        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getId());
        List<String> permissions = sysPermissionMapper.selectPermissionCodesByUserId(user.getId());

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);
        userInfoDTO.setRoles(roles);

        return Result.suc(Map.of(
                "user", userInfoDTO,
                "permissions", permissions
        ));
    }
}