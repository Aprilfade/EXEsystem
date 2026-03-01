# 贡献指南 (Contributing Guide)

感谢您对 **EXEsystem** 项目的关注！我们欢迎所有形式的贡献，包括但不限于：

- 🐛 提交 Bug 报告
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码修复
- ✨ 开发新功能

---

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发环境设置](#开发环境设置)
- [提交规范](#提交规范)
- [Pull Request 流程](#pull-request-流程)
- [代码规范](#代码规范)
- [测试要求](#测试要求)
- [文档编写](#文档编写)

---

## 行为准则

参与本项目即表示您同意遵守我们的 [行为准则](CODE_OF_CONDUCT.md)。请确保所有互动都保持尊重和建设性。

---

## 如何贡献

### 🐛 报告 Bug

如果您发现了 Bug，请通过 [GitHub Issues](https://github.com/your-repo/EXEsystem/issues) 提交报告，并包含：

1. **问题描述**：清晰描述问题是什么
2. **复现步骤**：详细的步骤说明
3. **预期行为**：您期望发生什么
4. **实际行为**：实际发生了什么
5. **环境信息**：
   - 操作系统：Windows/Linux/macOS
   - Java 版本：`java -version`
   - Node.js 版本：`node -v`
   - 浏览器版本（如果是前端问题）
6. **截图/日志**：如果有的话，请附上

**Bug 报告模板示例：**

```markdown
### Bug 描述
登录时提示 JWT token 验证错误

### 复现步骤
1. 访问 http://localhost:5173/login
2. 输入用户名 admin 和密码 123456
3. 点击登录按钮

### 预期行为
成功登录并跳转到首页

### 实际行为
控制台显示 MalformedJwtException 错误

### 环境信息
- OS: Windows 11
- Java: 17.0.2
- Node: 20.10.0
- Browser: Chrome 120.0
```

---

### 💡 功能建议

我们欢迎新功能的建议！请通过 [GitHub Issues](https://github.com/your-repo/EXEsystem/issues) 提交，并包含：

1. **功能描述**：详细描述建议的功能
2. **使用场景**：什么场景下需要这个功能
3. **预期收益**：这个功能能带来什么价值
4. **实现思路**（可选）：您对实现方式的想法

---

## 开发环境设置

### 环境要求

- **JDK**: 17+
- **Maven**: 3.9.6+
- **Node.js**: 20+
- **MySQL**: 8.0+
- **Redis**: 7.2+
- **Git**: 2.30+

### 1. Fork 并克隆项目

```bash
# Fork 项目到您的 GitHub 账户，然后克隆
git clone https://github.com/YOUR_USERNAME/EXEsystem.git
cd EXEsystem

# 添加上游仓库
git remote add upstream https://github.com/original-repo/EXEsystem.git
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE exam_system CHARACTER SET utf8mb4;

# 导入初始化脚本
mysql -u root -p exam_system < document/sql/exam_system.sql
```

### 3. 配置后端

复制配置文件并修改数据库连接信息：

```bash
cd exe-backend/src/main/resources
cp application-dev.yml.example application-dev.yml
# 编辑 application-dev.yml，修改数据库密码和其他配置
```

**关键配置项：**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system?useUnicode=true&characterEncoding=utf8mb4
    username: root
    password: YOUR_PASSWORD
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动后端服务

```bash
cd exe-backend
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动。

### 5. 配置并启动前端

```bash
cd exe-frontend
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5173` 启动。

### 6. 验证环境

访问：
- 前端：http://localhost:5173
- 后端API文档：http://localhost:8080/swagger-ui.html
- 默认账号：admin / 123456

---

## 提交规范

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

### 提交格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(ai): 添加 DeepSeek API 集成` |
| `fix` | Bug 修复 | `fix(auth): 修复 JWT token 验证错误` |
| `docs` | 文档更新 | `docs(readme): 更新安装说明` |
| `style` | 代码格式（不影响功能） | `style: 格式化代码` |
| `refactor` | 重构（不是新功能也不是修复） | `refactor(service): 优化查询逻辑` |
| `perf` | 性能优化 | `perf(database): 添加索引优化查询` |
| `test` | 添加测试 | `test(course): 添加课程模块单元测试` |
| `chore` | 构建/工具链变更 | `chore(deps): 升级 Spring Boot 版本` |

### Scope 范围（可选）

常用的 scope：
- `ai` - AI 相关功能
- `auth` - 认证授权
- `course` - 课程模块
- `exam` - 考试模块
- `practice` - 练习模块
- `admin` - 管理端
- `student` - 学生端
- `database` - 数据库相关
- `docs` - 文档

### 示例

```bash
# 好的提交示例
git commit -m "feat(ai): 添加填空题多答案智能匹配功能"
git commit -m "fix(auth): 修复 JWT token 过期后的异常处理"
git commit -m "docs(contributing): 完善贡献指南文档"

# 不好的提交示例
git commit -m "fix bug"
git commit -m "update"
git commit -m "修改了一些东西"
```

---

## Pull Request 流程

### 1. 创建功能分支

```bash
# 确保主分支是最新的
git checkout master
git pull upstream master

# 创建新分支
git checkout -b feature/amazing-feature
# 或
git checkout -b fix/bug-description
```

### 2. 开发并提交

```bash
# 进行开发...
# 添加变更
git add .

# 提交（遵循提交规范）
git commit -m "feat(scope): 添加新功能描述"
```

### 3. 保持分支更新

```bash
# 定期同步上游更新
git fetch upstream
git rebase upstream/master
```

### 4. 推送到您的 Fork

```bash
git push origin feature/amazing-feature
```

### 5. 创建 Pull Request

1. 访问您 Fork 的 GitHub 页面
2. 点击 "Compare & pull request"
3. 填写 PR 标题和描述：

**PR 标题格式：**
```
[Type] Brief description
```

**PR 描述模板：**
```markdown
## 变更说明
简要描述此 PR 做了什么改动

## 变更类型
- [ ] Bug 修复
- [ ] 新功能
- [ ] 文档更新
- [ ] 代码重构
- [ ] 性能优化
- [ ] 测试相关

## 相关 Issue
Closes #123

## 测试情况
- [ ] 已通过本地测试
- [ ] 已添加/更新单元测试
- [ ] 已手动测试功能

## 截图（如适用）
[添加截图]

## 检查清单
- [ ] 代码符合项目规范
- [ ] 已更新相关文档
- [ ] 提交信息符合规范
- [ ] 无合并冲突
```

### 6. 代码审查

- 维护者会审查您的 PR
- 根据反馈进行修改
- 保持讨论的建设性和尊重

### 7. 合并

审查通过后，维护者会合并您的 PR。恭喜您成为贡献者！🎉

---

## 代码规范

### 后端规范（Java）

遵循 [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)

**关键规范：**

1. **命名规范**
   - 类名：大驼峰 `UserService`
   - 方法名：小驼峰 `getUserById`
   - 常量：全大写下划线 `MAX_COUNT`
   - 包名：全小写 `com.exe.system`

2. **注释规范**
   ```java
   /**
    * 用户服务类
    *
    * @author Your Name
    * @date 2026-01-11
    */
   @Service
   public class UserService {

       /**
        * 根据ID获取用户
        *
        * @param userId 用户ID
        * @return 用户信息
        */
       public User getUserById(Long userId) {
           // 实现逻辑
       }
   }
   ```

3. **异常处理**
   - 使用统一的异常处理机制
   - 记录必要的日志
   - 不要吞掉异常

### 前端规范（Vue 3 + TypeScript）

遵循 [Vue 3 风格指南](https://vuejs.org/style-guide/)

**关键规范：**

1. **组件命名**
   - 文件名：PascalCase `UserProfile.vue`
   - 组件名：多单词 `UserProfile` 而非 `User`

2. **TypeScript 类型**
   ```typescript
   // 定义接口
   interface User {
     id: number
     username: string
     email: string
   }

   // 使用类型
   const user: User = {
     id: 1,
     username: 'admin',
     email: 'admin@example.com'
   }
   ```

3. **Composition API 风格**
   ```vue
   <script setup lang="ts">
   import { ref, computed, onMounted } from 'vue'

   const count = ref(0)
   const doubled = computed(() => count.value * 2)

   onMounted(() => {
     console.log('Component mounted')
   })
   </script>
   ```

---

## 测试要求

### 后端测试

使用 JUnit 5 + MockMvc

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testGetUserById() {
        User user = userService.getUserById(1L);
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }
}
```

### 前端测试

使用 Vitest（计划中）

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserProfile from '@/components/UserProfile.vue'

describe('UserProfile', () => {
  it('renders user name', () => {
    const wrapper = mount(UserProfile, {
      props: { username: 'admin' }
    })
    expect(wrapper.text()).toContain('admin')
  })
})
```

### 测试覆盖率

- 目标：**80%+** 代码覆盖率
- 重点：核心业务逻辑必须有测试

---

## 文档编写

### 文档类型

1. **功能文档**：位于 `document/docs/` 下，按模块分类
2. **API 文档**：使用 Swagger 注解自动生成
3. **部署文档**：位于 `document/docs/deployment/`

### 文档规范

**Markdown 格式：**

```markdown
# 功能名称

## 概述
简要描述功能

## 功能特性
- 特性1
- 特性2

## 使用说明
详细的使用步骤

## 配置说明
配置参数说明

## 常见问题
FAQ

## 参考资料
相关链接
```

**文档命名：**
- 完成报告：`功能名称-完成报告-v版本号.md`
- 实施指南：`功能名称-实施指南-v版本号.md`
- 优化方案：`功能名称-优化方案.md`

---

## 社区支持

### 获取帮助

- 📖 查看 [完整文档](document/DOCS_GUIDE.md)
- 💬 提交 [GitHub Issue](https://github.com/your-repo/EXEsystem/issues)
- 📧 联系维护者

### 贡献者认可

所有贡献者将被列入 [贡献者列表](https://github.com/your-repo/EXEsystem/graphs/contributors)。

---

## 开发资源

### 相关技术文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 3 官方文档](https://vuejs.org/)
- [TypeScript 官方文档](https://www.typescriptlang.org/)
- [MyBatis Plus 文档](https://baomidou.com/)
- [Element Plus 文档](https://element-plus.org/)

### 项目文档

- [快速开始指南](README.md#快速开始)
- [Docker 部署指南](document/docs/deployment/DOCKER_QUICKSTART.md)
- [AI 功能文档](document/docs/ai/)
- [课程模块文档](document/docs/course/)

---

## 许可证

通过贡献代码，您同意您的贡献将在 [MIT License](LICENSE) 下授权。

---

<div align="center">

**感谢您的贡献！🎉**

让我们一起打造更好的在线教育平台！

</div>
