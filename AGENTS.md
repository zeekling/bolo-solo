# Bolo-Solo 代码库代理指南

## 全局规则

本项目需要读取系统默认的 AGENTS.md 规则作为基础：
- 系统 AGENTS.md 由 OpenCode 平台提供（~/.config/opencode/AGENTS.md）
- 本地 AGENTS.md 规则作为补充和覆盖
- 如有冲突，以本项目 AGENTS.md 为准

---

## 项目特有规则

### 依赖管理

**依赖管理原则**:
- 避免重复依赖：同一个库只保留一个版本
- 移除过时依赖：优先使用新版 API，删除旧版依赖
- 统一包名：迁移时注意包路径变化（如 `com.sun.syndication` → `com.rometools.rome`）

**依赖更新流程**:
```bash
# 检查依赖更新
mvn versions:display-dependency-updates

# 清理并验证
mvn clean verify

# 格式化代码
mvn spotless:apply
```

### 代码检查

**重构检查清单**:
- [ ] 依赖是否重复？是否有过时依赖？
- [ ] 新增 import 是否与现有冲突？
- [ ] API 变更是否需要类型调整？（如 `SyndEntryImpl` → `SyndEntry`）
- [ ] 编译是否通过？

---

## 源文件结构

```
src/main/java/
├── org/b3log/solo/bolo/           # Bolo 专用扩展
│   ├── pic/                       # 图片上传
│   └── prop/                      # 属性和邮件服务
└── pers/adlered/                  # 自定义扩展

src/main/webapp/
├── js/admin/                      # 管理脚本
├── skins/                         # 主题 (.ftl)
├── scss/                          # 样式
└── plugins/                      # 前端插件
```

---

## 包命名

- 原始 Solo 代码: `org.b3log.solo.*`
- Bolo 扩展: `org.b3log.solo.bolo.*` 和 `pers.adlered.*`

---

## 测试规范

- 框架: TestNG（不是 JUnit）
- 运行: `mvn test`

---

## Git 分支策略

- `master`: 生产代码
- `dev/*`: 功能开发分支
- `feature/*`: 功能开发
- `bugfix/*`: Bug 修复