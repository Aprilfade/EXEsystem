# 📚 文档快速访问指南

## 最新功能文档（优先查看）

### 🔥 管理端课程管理优化 (v3.07) - 刚完成
📄 **完成报告**: `docs/admin/管理端课程管理优化完成报告-v3.07.md`
📄 **实施指南**: `docs/admin/管理端课程管理优化实施指南-v3.07.md`

**核心功能**:
- ✅ 章节管理（树形结构、拖拽排序）
- ✅ 学习数据分析（学生进度仪表板）
- ✅ 标签页集成（基本信息/章节/资源/数据）

---

### ⭐ 在线练习功能 (v3.10)
📄 **文档**: `docs/practice/在线练习功能优化实施总结-v3.10.md`

**核心功能**:
- ✅ 智能题目生成
- ✅ AI自动批改
- ✅ 错因深度分析

---

### 🎓 课程学习中心 (v3.06)
📄 **文档**: `docs/course/课程学习中心全面优化完成报告-v3.06-最终版.md`

**核心功能**:
- ✅ DPlayer视频播放器
- ✅ 学习进度自动追踪
- ✅ 章节化学习路径
- ✅ 访客模式支持

---

## 📂 按需查找

### 需要部署系统？
1. **数据库**: `sql/exam_system.sql` - 完整数据库脚本
2. **编译问题**: `docs/deployment/编译问题解决方案.md`
3. **SQL说明**: `sql/README.md`

### 遇到bug需要修复？
1. **紧急修复**: `docs/fixes/紧急修复指南-更新版.md`
2. **权限问题**: `docs/fixes/403权限问题修复说明.md`

### 想了解AI功能？
1. **AI推荐**: `docs/ai/AI智能推荐系统-优秀毕业设计完整文档.md`
2. **AI助手**: `docs/ai/学生端AI助手优化总结-优秀毕业设计版.md`
3. **流式响应**: `docs/ai/流式响应功能说明.md`

### 优化学生端？
1. **Portal导航**: `docs/portal/导航系统优化实施总结-v3.03.md`
2. **学生功能**: `docs/student/学生端全功能优化方案.md`

---

## 🗂️ 完整目录结构

```
EXEsystem/
├── README.md                    # 项目主README
├── document/                    # 📁 文档中心（所有文档集中存放）
│   ├── DOCS_GUIDE.md           # 📖 本文件 - 快速访问指南
│   ├── docs/                   # 功能文档分类
│   │   ├── README.md          # 文档索引（60+份文档分类导航）
│   │   ├── ai/                # 🤖 AI功能 (16个文档)
│   │   ├── course/            # 📚 课程学习 (4个文档)
│   │   ├── portal/            # 🏠 导航页 (12个文档)
│   │   ├── practice/          # ✏️ 在线练习 (6个文档)
│   │   ├── admin/             # 👨‍💼 管理端 (7个文档)
│   │   ├── student/           # 👨‍🎓 学生端 (3个文档)
│   │   ├── grading/           # 📝 批阅 (1个文档)
│   │   ├── fixes/             # 🔧 修复 (3个文档)
│   │   └── deployment/        # 🚀 部署 (6个文档)
│   └── sql/                    # 🗄️ SQL脚本
│       ├── README.md          # SQL说明文档
│       ├── exam_system.sql    # 完整系统数据库
│       ├── course-learning-optimization.sql
│       ├── grading-history-table.sql
│       ├── grading-optimization-migration.sql
│       └── notification-table.sql
├── exe-backend/                 # 后端代码
├── exe-frontend/                # 前端代码
└── ...
```

---

## 🎯 常见任务导航

| 任务 | 文档路径 |
|------|---------|
| 新增课程章节 | `docs/admin/管理端课程管理优化完成报告-v3.07.md` |
| 查看学生学习数据 | `docs/admin/管理端课程管理优化完成报告-v3.07.md` |
| 配置AI推荐 | `docs/ai/AI智能推荐系统-优秀毕业设计完整文档.md` |
| 部署新环境 | `sql/README.md` + `docs/deployment/编译问题解决方案.md` |
| 优化Portal页面 | `docs/portal/导航系统优化实施总结-v3.03.md` |
| 修复权限问题 | `docs/fixes/403权限问题修复说明.md` |
| 配置在线练习 | `docs/practice/在线练习功能优化实施总结-v3.10.md` |

---

## 💡 提示

- 📖 每个子目录都有详细的README索引：`docs/README.md`, `sql/README.md`
- 🔍 使用Ctrl+F在索引文件中搜索关键词
- 📅 文档版本号表示迭代顺序，数字越大越新
- ⭐ 标记"最终版"的文档是该功能的最新完整版本

---

**最后更新**: 2026-01-11
**文档总数**: 60+份
**SQL脚本**: 5个
