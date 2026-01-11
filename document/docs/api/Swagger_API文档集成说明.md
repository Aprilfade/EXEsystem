# Swagger API 文档集成说明

## 📚 集成完成概览

EXEsystem 项目已成功集成 Swagger API 文档（基于 Springdoc OpenAPI 3.0）。

### ✅ 已完成的工作

1. **添加依赖**
   - `springdoc-openapi-starter-webmvc-ui:2.3.0` 已添加到 `pom.xml`

2. **创建配置类**
   - `SwaggerConfig.java` - Swagger 核心配置
   - 配置了 API 基本信息（标题、描述、版本、联系人等）
   - 配置了 JWT 认证方式
   - 创建了 6 个 API 分组：
     - 0. 完整 API（全部）
     - 1. 管理端 API
     - 2. 学生端 API
     - 3. AI 智能服务 API
     - 4. 文件服务 API
     - 5. 公共 API

3. **配置文件修改**
   - `application.yml` - 添加了 Springdoc 配置
   - 启用 API 文档和 Swagger UI
   - 配置了排序、过滤、持久化认证等功能

4. **安全配置**
   - `SecurityConfig.java` - 添加了 Swagger UI 白名单
   - 允许以下路径无需认证访问：
     - `/swagger-ui/**`
     - `/swagger-ui.html`
     - `/v3/api-docs/**`
     - `/swagger-resources/**`
     - `/webjars/**`

5. **Controller 注解示例**
   - `BizStudentController.java` - 完整的 Swagger 注解示例（10+ 个接口）
   - `AuthController.java` - 认证接口 Swagger 注解示例（4 个接口）
   - 使用的注解：
     - `@Tag` - 描述 Controller 分组
     - `@Operation` - 描述接口功能
     - `@Parameter` - 描述参数

---

## 🚀 如何启动和使用

### 1. 重新编译项目

由于修改了 `pom.xml` 添加了新依赖，需要重新编译项目：

```bash
cd exe-backend
mvn clean install
```

或者在 IDEA 中：
- 点击右侧 Maven 工具栏
- 点击 `Reload All Maven Projects` 图标（刷新）
- 等待依赖下载完成

### 2. 启动后端服务

```bash
cd exe-backend
mvn spring-boot:run
```

或者在 IDEA 中：
- 找到 `ExeBackendApplication.java`
- 右键 -> `Run 'ExeBackendApplication'`

### 3. 访问 Swagger UI

启动成功后，在浏览器中访问：

**主界面：**
```
http://localhost:8080/swagger-ui.html
```

**API 文档 JSON：**
```
http://localhost:8080/v3/api-docs
```

---

## 📖 Swagger UI 使用指南

### 1. 界面说明

打开 Swagger UI 后，你会看到：

- **顶部导航栏**：显示 API 标题和版本信息
- **Select a definition**：下拉菜单选择 API 分组
  - 0. 完整 API（全部） - 查看所有接口
  - 1. 管理端 API - 管理后台接口
  - 2. 学生端 API - 学生端接口
  - 3. AI 智能服务 API - AI 相关接口
  - 4. 文件服务 API - 文件上传下载
  - 5. 公共 API - 无需认证的公共接口

- **Authorize 按钮**：配置 JWT Token 认证
- **接口列表**：按 Controller 分组显示所有接口

### 2. 如何测试需要认证的接口

#### 步骤 1：获取 Token

1. 找到 `管理员认证` 分组
2. 展开 `POST /api/v1/auth/login` 接口
3. 点击 `Try it out` 按钮
4. 输入登录信息（JSON 格式）：
   ```json
   {
     "username": "admin",
     "password": "your_password"
   }
   ```
5. 点击 `Execute` 按钮
6. 在响应结果中复制 `token` 值（例如：`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`）

#### 步骤 2：配置认证

1. 点击页面右上角的 `Authorize` 按钮（锁形图标）
2. 在弹出的对话框中，Value 输入框输入：
   ```
   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
   ⚠️ 注意：必须加上 `Bearer ` 前缀（注意空格）
3. 点击 `Authorize` 按钮
4. 点击 `Close` 关闭对话框

#### 步骤 3：测试接口

1. 展开任意需要认证的接口（例如：`GET /api/v1/students`）
2. 点击 `Try it out` 按钮
3. 填写必要的参数（例如：`current=1`, `size=10`）
4. 点击 `Execute` 按钮
5. 查看响应结果

### 3. 接口参数说明

- **Path Parameters**：路径参数（例如：`/api/v1/students/{id}` 中的 `id`）
- **Query Parameters**：查询参数（例如：`?current=1&size=10`）
- **Request Body**：请求体（JSON 格式）
- **Responses**：响应示例和状态码

---

## 🔧 为其他 Controller 添加 Swagger 注解

### 示例：为新的 Controller 添加注解

```java
package com.ice.exebackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "试卷管理", description = "试卷增删改查、智能组卷、试卷导出等接口")
@RestController
@RequestMapping("/api/v1/papers")
public class BizPaperController {

    @Operation(summary = "获取试卷列表", description = "分页查询试卷列表，支持按科目、年级筛选")
    @GetMapping
    public Result getPaperList(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "科目ID（可选）") @RequestParam(required = false) Long subjectId,
            @Parameter(description = "年级（可选）", example = "高一") @RequestParam(required = false) String grade) {
        // ... 业务逻辑
    }

    @Operation(summary = "创建试卷", description = "新增一份试卷")
    @PostMapping
    public Result createPaper(@RequestBody BizPaper paper) {
        // ... 业务逻辑
    }

    @Operation(summary = "更新试卷", description = "根据试卷ID更新试卷信息")
    @PutMapping("/{id}")
    public Result updatePaper(
            @Parameter(description = "试卷ID", required = true) @PathVariable Long id,
            @RequestBody BizPaper paper) {
        // ... 业务逻辑
    }

    @Operation(summary = "删除试卷", description = "根据ID删除试卷")
    @DeleteMapping("/{id}")
    public Result deletePaper(@Parameter(description = "试卷ID", required = true) @PathVariable Long id) {
        // ... 业务逻辑
    }
}
```

### 常用注解说明

| 注解 | 作用位置 | 说明 | 示例 |
|-----|---------|------|------|
| `@Tag` | Controller 类 | 描述整个 Controller 的功能分组 | `@Tag(name = "学生管理", description = "...")` |
| `@Operation` | 方法 | 描述接口的功能和用途 | `@Operation(summary = "创建学生", description = "...")` |
| `@Parameter` | 方法参数 | 描述参数的含义、类型、是否必填等 | `@Parameter(description = "学生ID", required = true)` |
| `@Schema` | DTO/Entity 类字段 | 描述实体类字段 | `@Schema(description = "学生姓名", example = "张三")` |

---

## 📝 推荐优化建议

### 1. 为所有 Controller 添加注解

建议按以下优先级为 Controller 添加 Swagger 注解：

**高优先级（核心业务）：**
- ✅ `BizStudentController` - 学生管理（已完成）
- ✅ `AuthController` - 管理员认证（已完成）
- ⏳ `BizQuestionController` - 题库管理
- ⏳ `BizPaperController` - 试卷管理
- ⏳ `BizSubjectController` - 科目管理
- ⏳ `StudentAuthController` - 学生认证
- ⏳ `StudentExamController` - 学生考试

**中优先级（辅助功能）：**
- ⏳ `BizKnowledgePointController` - 知识点管理
- ⏳ `BizClassController` - 班级管理
- ⏳ `BizCourseController` - 课程管理
- ⏳ `AiController` - AI 服务

**低优先级（管理功能）：**
- ⏳ `SysUserController` - 用户管理
- ⏳ `SysRoleController` - 角色管理
- ⏳ `SysPermissionController` - 权限管理
- ⏳ `SysLoginLogController` - 登录日志
- ⏳ `SysOperLogController` - 操作日志

### 2. 为 DTO/Entity 添加 @Schema 注解

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "学生实体")
@Data
public class BizStudent {

    @Schema(description = "学生ID", example = "1")
    private Long id;

    @Schema(description = "学号", example = "2024001", required = true)
    private String studentNo;

    @Schema(description = "学生姓名", example = "张三", required = true)
    private String name;

    @Schema(description = "年级", example = "高一", required = true)
    private String grade;

    @Schema(description = "班级", example = "1班")
    private String className;

    @Schema(description = "联系方式", example = "13800138000")
    private String contact;

    @Schema(description = "积分", example = "100")
    private Integer points;

    @Schema(description = "密码（不返回给前端）", hidden = true)
    private String password;
}
```

### 3. 配置全局异常返回示例

在 `SwaggerConfig.java` 中可以添加全局异常响应示例：

```java
@Bean
public OpenApiCustomiser openApiCustomiser() {
    return openApi -> {
        // 全局添加 401 未授权响应
        openApi.getPaths().values().forEach(pathItem -> {
            pathItem.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();
                responses.addApiResponse("401", new ApiResponse()
                        .description("未授权，Token无效或过期")
                        .content(new Content()
                                .addMediaType("application/json", new MediaType()
                                        .example("{ \"code\": 401, \"msg\": \"未授权\", \"data\": null }"))));
            });
        });
    };
}
```

---

## 🐛 常见问题

### 1. 访问 Swagger UI 显示 403 Forbidden

**原因**：Security 配置未正确添加白名单

**解决方案**：检查 `SecurityConfig.java` 中是否添加了以下白名单：
```java
.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/swagger-resources/**",
    "/webjars/**"
).permitAll()
```

### 2. Swagger UI 显示空白或没有接口

**原因**：
- 配置文件中的 `packages-to-scan` 路径不正确
- Controller 类未加 `@RestController` 或 `@Controller` 注解

**解决方案**：
1. 检查 `application.yml` 中的配置：
   ```yaml
   springdoc:
     packages-to-scan: com.ice.exebackend.controller
   ```
2. 确保 Controller 类有正确的注解

### 3. 接口测试提示 401 Unauthorized

**原因**：未配置 JWT Token 或 Token 格式错误

**解决方案**：
1. 先调用登录接口获取 Token
2. 点击 `Authorize` 按钮
3. 输入 `Bearer {token}`（注意 Bearer 后面有空格）
4. 点击 `Authorize` 和 `Close`

### 4. Maven 依赖下载失败

**原因**：网络问题或 Maven 仓库连接失败

**解决方案**：
1. 配置国内镜像（阿里云）：
   ```xml
   <!-- 在 pom.xml 中添加 -->
   <repositories>
       <repository>
           <id>aliyun</id>
           <url>https://maven.aliyun.com/repository/public</url>
       </repository>
   </repositories>
   ```
2. 或者在 `~/.m2/settings.xml` 中配置全局镜像

### 5. 启动时出现 Bean 冲突

**原因**：Springdoc 和其他库可能有冲突

**解决方案**：
1. 检查是否同时引入了 Springfox（旧版 Swagger）
2. 如果有，移除 Springfox 依赖
3. Springdoc OpenAPI 是 Spring Boot 3.x 官方推荐的 Swagger 实现

---

## 📚 参考资源

- **Springdoc OpenAPI 官方文档**：https://springdoc.org/
- **Swagger UI 官方文档**：https://swagger.io/tools/swagger-ui/
- **OpenAPI 规范**：https://spec.openapis.org/oas/v3.1.0

---

## ✅ 集成完成清单

- [x] 添加 Springdoc OpenAPI 依赖到 `pom.xml`
- [x] 创建 `SwaggerConfig.java` 配置类
- [x] 配置 `application.yml` 启用 Swagger UI
- [x] 修改 `SecurityConfig.java` 添加白名单
- [x] 为 `BizStudentController` 添加完整注解（示例）
- [x] 为 `AuthController` 添加完整注解（示例）
- [x] 创建本使用说明文档

---

## 🎯 下一步建议

1. **测试 Swagger UI**
   - 启动项目，访问 `http://localhost:8080/swagger-ui.html`
   - 测试登录接口获取 Token
   - 测试学生管理相关接口

2. **为其他 Controller 添加注解**
   - 参照 `BizStudentController` 和 `AuthController` 的示例
   - 优先为核心业务 Controller 添加注解

3. **为 DTO/Entity 添加 @Schema 注解**
   - 提升文档的可读性和可维护性

4. **考虑生成前端 API 客户端代码**
   - 使用 `openapi-generator` 根据 API 文档自动生成 TypeScript 客户端
   - 提升前后端协作效率

---

**文档版本：** v1.0
**创建时间：** 2026-01-06
**作者：** Claude Sonnet 4.5
