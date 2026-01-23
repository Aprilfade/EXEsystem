package com.ice.exebackend.config;

import com.ice.exebackend.interceptor.ApiPerformanceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置 + Web拦截器配置 - 安全加固版
 *
 * 修复内容：
 * 1. 移除不安全的 allowedOriginPatterns("*") 通配符配置
 * 2. 使用明确的域名白名单（从配置文件读取）
 * 3. 防止 CORS 配置被滥用进行跨域攻击
 * 4. 注册API性能监控拦截器
 *
 * @author Ice
 * @date 2026-01-12
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 从配置文件读取允许的源（支持多个，逗号分隔）
     * 默认值：开发环境常用端口
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:80}")
    private String[] allowedOrigins;

    @Autowired
    private ApiPerformanceInterceptor apiPerformanceInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // 仅对 API 路径生效
                .allowedOrigins(allowedOrigins)  // 使用明确的白名单，不再使用通配符
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带凭证（Cookie、Authorization）
                .exposedHeaders("Content-Disposition")  // 暴露文件下载响应头
                .maxAge(3600);  // 预检请求缓存时间（1小时）
    }

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册API性能监控拦截器
        registry.addInterceptor(apiPerformanceInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns(
                        "/api/v1/health",  // 排除健康检查端点（避免日志过多）
                        "/api/v1/health/**"
                );
    }
}