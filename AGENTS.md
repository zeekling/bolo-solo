# Bolo-Solo 代码库代理指南

## 全局规则

本项目需要读取系统默认的 AGENTS.md 规则作为基础：
- 系统 AGENTS.md 由 OpenCode 平台提供
- 本地 AGENTS.md 规则作为补充和覆盖
- 如有冲突，以本项目 AGENTS.md 为准

## 构建与测试命令

### Java 构建（Maven - JDK 21）
```bash
# 基本构建
mvn clean       # 清理构建产物
mvn package     # 构建 WAR 包
mvn verify      # 运行测试和验证

# 测试执行
mvn test                    # 运行所有测试
mvn test -Dtest=TestClass    # 运行单个测试类
mvn test -Dtest=TestClass#method  # 运行单个测试方法

# 确保测试覆盖率
mvn jacoco:report            # 生成测试覆盖率报告

# 安全检查
mvn license:format            # 检查许可证头部
mvn license:check             # 验证许可证合规性
```

### Docker 部署
```bash
docker build -t bolo-solo:latest .    # 构建 Docker 镜像
docker-compose up -d                   # 使用 docker-compose 启动
```

---

## 代码风格指南

### Java 代码风格

**包命名**:
- 原始 Solo 代码: `org.b3log.solo.*`
- Bolo 扩展: `org.b3log.solo.bolo.*` 和 `pers.adlered.*`

**许可证头部** (必须包含，文件顶部):
```java
/*
 * Bolo - 一个基于 Solo 的稳定美丽的博客系统。
 * Copyright (c) 2020-present, https://github.com/bolo-blog
 *
 * 本程序是自由软件：您可以 redistribute 它并根据 GNU Affero General Public License 的条款
 * 修改它，要么按照版本 3，要么（根据您的选择）任何后来的版本。
 *
 * 本程序旨在有用，但 WITHOUT ANY WARRANTY。
 * 您应该收到了 GNU Affero General Public License 的副本
 * 如果没有，请参阅 <https://www.gnu.org/licenses/>。
 */
```

**错误处理**:
- 使用 SLF4J: `Logger.getLogger(ClassName.class)`
- 捕获特定异常，不要用通用的 `Exception`

### 前端代码风格

- SCSS 输出样式: `compressed`
- JavaScript 使用 `const`/`let`，ES6+ 优先
- 主题模板: `src/main/webapp/skins/` 下的 `.ftl` 文件

---

## 质量保证与安全

### 测试规范
- 框架: TestNG（不是 JUnit）
- 运行: `mvn test`

### 代码检查
- 许可证头部: `mvn license:format`

### 安全最佳实践
- 使用 PreparedStatement 防止 SQL 注入
- 密码使用 BCrypt 哈希存储

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

## Git 工作流程

### 分支策略
- `master`: 生产代码
- `dev/*`: 功能开发分支
- `feature/*`: 功能开发
- `bugfix/*`: Bug 修复

### 提交规范
```
<类型>: <描述>

类型:
- feat: 新功能
- fix: Bug 修复
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试
- chore: 构建/工具

示例:
- feat: 添加邮件通知功能
- fix: 修复评论显示问题
- docs: 更新部署文档
```

---

## 技术栈（当前版本）

### 后端
- **Java**: JDK 21
- **框架**: Latke 2.5.8
- **数据库**: MySQL 8.0.33, H2 2.2.224
- **Web 服务器**: Jetty 9.4.53
- **Markdown**: Flexmark 0.64.8
- **HTML 解析**: Jsoup 1.14.3
- **日志**: SLF4J 1.7.28 (slf4j-log4j12)

### 前端
- **JavaScript**: ES6+
- **CSS**: SASS 1.51.0
- **编辑器**: Vditor 3.8.5
- **jQuery**: 3.1.0

---

## 常见任务

### 添加新功能
1. 在 `org.b3log.solo.bolo` 或 `pers.adlered` 创建包
2. 添加许可证头部
3. 如可测试，添加 TestNG 测试
4. 运行 `mvn clean package` 验证
5. 更新前端资源（如需要）

### 依赖项更新
```bash
mvn versions:display-dependency-updates  # 检查更新
mvn clean verify                          # 验证构建
```

### 代码格式化
每次涉及代码修改的功能结束后，必须格式化代码：
```bash
mvn spotless:apply  # 格式化代码
```
如果涉及 UT（单元测试），则需要检查 UT 是否通过：
```bash
mvn test  # 运行测试验证
```
