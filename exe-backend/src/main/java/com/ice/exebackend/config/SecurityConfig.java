package com.ice.exebackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService adminUserDetailsService;

    @Autowired
    @Qualifier("studentDetailsServiceImpl")
    private UserDetailsService studentUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 【核心修改】
     * 我们将手动创建一个 AuthenticationManager (类型为 ProviderManager)，
     * 并将我们为管理员和学生创建的两个 AuthenticationProvider 明确地注入进去。
     * 这将替换掉Spring Boot的自动配置，从而解决 "Global Authentication Manager will not be configured" 的警告。
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Arrays.asList(adminAuthenticationProvider(), studentAuthenticationProvider()));
    }

    @Bean
    @SuppressWarnings("deprecation") // 【修改点】压制过时警告
    public AuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @SuppressWarnings("deprecation") // 【修改点】压制过时警告
    public AuthenticationProvider studentAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(studentUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 【关键修复】必须放在第一位！允许所有 ASYNC 和 ERROR 类型的 dispatch，避免 async dispatch 时重新检查权限
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC).permitAll()
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()

                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/student/auth/login",
                                "/api/v1/files/**",
                                "/api/v1/portal/**", // 【新增】Portal导航页API，允许所有人访问
                                "/ws/**",
                                "/error", // 【修复】放行错误页面
                                // Swagger API 文档白名单
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                // Spring Boot Actuator 健康检查和监控端点
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/metrics",
                                "/actuator/metrics/**"
                        ).permitAll()
                        // 【核心修改1】将 /api/v1/files/** 的规则拆分
                        // GET请求（查看图片）对所有人开放
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
                        // POST请求（上传文件）只需要登录即可，不限角色
                        .requestMatchers(HttpMethod.POST, "/api/v1/files/upload").authenticated()

                        // 2. 学生端API路径：需要认证，后续由 @PreAuthorize 进行角色检查
                        .requestMatchers("/api/v1/student/**").authenticated()

                        // 3. 管理后台API路径：需要认证，后续由 @PreAuthorize 进行角色检查
                        .requestMatchers("/api/v1/**").authenticated()

                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}