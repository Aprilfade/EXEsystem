package com.ice.exebackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API 文档配置类
 *
 * 访问地址：
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - API Docs (JSON): http://localhost:8080/v3/api-docs
 *
 * @author ICE
 * @version 1.0
 * @since 2026-01-06
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title("EXEsystem 在线考试与学习管理系统 API")
                        .description("基于 Spring Boot 3 + Vue 3 的智能教育管理平台\n\n" +
                                "核心功能：\n" +
                                "- 题库管理（5种题型）\n" +
                                "- 智能组卷（遗传算法）\n" +
                                "- AI 智能批改（DeepSeek/通义千问）\n" +
                                "- 在线考试（防切屏检测）\n" +
                                "- 知识竞技场（WebSocket 实时对战）\n" +
                                "- 艾宾浩斯智能复习\n" +
                                "- 游戏化学习（积分、成就、段位）\n" +
                                "- 班级作业管理\n" +
                                "- 权限管理（RBAC）")
                        .version("v2.51")
                        .contact(new Contact()
                                .name("ICE")
                                .email("your-email@example.com")
                                .url("https://github.com/your-repo/EXEsystem"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // 外部文档链接
                .externalDocs(new ExternalDocumentation()
                        .description("EXEsystem 项目文档")
                        .url("https://github.com/your-repo/EXEsystem/blob/master/README.md"))

                // 配置 JWT 认证
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入 JWT Token，格式：Bearer {token}")));
    }

    /**
     * 管理端 API 分组
     * 包含所有管理后台相关接口（用户管理、题库管理、试卷管理等）
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("1. 管理端 API")
                .pathsToMatch(
                        "/api/v1/auth/**",        // 管理员认证
                        "/api/v1/users/**",       // 用户管理
                        "/api/v1/roles/**",       // 角色管理
                        "/api/v1/permissions/**", // 权限管理
                        "/api/v1/subjects/**",    // 科目管理
                        "/api/v1/knowledge-points/**", // 知识点管理
                        "/api/v1/questions/**",   // 题库管理
                        "/api/v1/papers/**",      // 试卷管理
                        "/api/v1/biz-students/**", // 学生管理
                        "/api/v1/classes/**",     // 班级管理
                        "/api/v1/courses/**",     // 课程管理
                        "/api/v1/notifications/**", // 通知管理
                        "/api/v1/statistics/**",  // 统计分析
                        "/api/v1/logs/**"         // 日志管理
                )
                .build();
    }

    /**
     * 学生端 API 分组
     * 包含所有学生端相关接口（考试、练习、错题本等）
     */
    @Bean
    public GroupedOpenApi studentApi() {
        return GroupedOpenApi.builder()
                .group("2. 学生端 API")
                .pathsToMatch(
                        "/api/v1/student/auth/**",     // 学生认证
                        "/api/v1/student/dashboard/**", // 学生仪表盘
                        "/api/v1/student/exams/**",    // 在线考试
                        "/api/v1/student/practice/**", // 在线练习
                        "/api/v1/student/wrong-records/**", // 错题本
                        "/api/v1/student/smart-review/**", // 智能复习
                        "/api/v1/student/battle/**",   // 知识竞技场
                        "/api/v1/student/favorites/**", // 题目收藏
                        "/api/v1/student/classes/**",  // 我的班级
                        "/api/v1/student/courses/**",  // 课程中心
                        "/api/v1/student/achievements/**", // 成就系统
                        "/api/v1/student/goods/**",    // 积分商城
                        "/api/v1/student/profile/**"   // 个人中心
                )
                .build();
    }

    /**
     * AI 服务 API 分组
     * 包含所有 AI 相关接口（智能批改、智能出题等）
     */
    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("3. AI 智能服务 API")
                .pathsToMatch("/api/v1/ai/**")
                .build();
    }

    /**
     * 文件服务 API 分组
     * 包含文件上传、下载等接口
     */
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("4. 文件服务 API")
                .pathsToMatch("/api/v1/files/**")
                .build();
    }

    /**
     * 公共 API 分组
     * 包含所有不需要认证的公共接口
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("5. 公共 API")
                .pathsToMatch("/api/v1/public/**")
                .build();
    }

    /**
     * 完整 API 分组
     * 包含所有接口（用于查看完整 API 文档）
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0. 完整 API（全部）")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
