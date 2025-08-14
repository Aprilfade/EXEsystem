package com.ice.exebackend.config;

import com.ice.exebackend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    /**
     * 【修改第1处】
     * 注入两个 UserDetailsService 实例，
     * 并使用 @Qualifier 注解，通过Bean的名称精确指定要注入哪一个。
     */
    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService adminUserDetailsService;

    @Autowired
    @Qualifier("studentDetailsServiceImpl")
    private UserDetailsService studentUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String bearer = "Bearer ";

        if (authHeader != null && authHeader.startsWith(bearer)) {
            String authToken = authHeader.substring(bearer.length());
            // "username" 此时既可能是管理员用户名，也可能是学生学号
            String username = jwtUtil.getUsernameFromToken(authToken);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails;

                /**
                 * 【修改第2处】
                 * 核心判断逻辑：根据请求的URL路径，决定调用哪个认证服务。
                 */
                if (request.getRequestURI().startsWith("/api/v1/student/")) {
                    // 如果是学生端的API请求，则使用学生认证服务
                    userDetails = this.studentUserDetailsService.loadUserByUsername(username);
                } else {
                    // 否则，默认使用管理后台的认证服务
                    userDetails = this.adminUserDetailsService.loadUserByUsername(username);
                }

                // 后续的Token验证和上下文设置逻辑保持不变
                if (jwtUtil.validateToken(authToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}