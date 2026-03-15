# Bolo-Solo 代码库代理指南

## 项目概述

Bolo-Solo 是一个基于 Solo 的稳定、美丽的博客系统，使用 Java 编写，结合 Maven 和前端技术。

---

## 构建与测试命令

### 前端构建（Gulp）
```bash
npm run build    # 构建所有前端资源
npm run dev      # 监听模式构建
gulp            # npm run build 的别名
gulp watch      # 开发模式
```

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

**许可证头部** (必须包含):
```java
/*
 * Bolo - 一个基于 Solo 的稳定美丽的博客系统。
 * Copyright (c) 2020-present, https://github.com/bolo-blog
 *
 * 本程序是自由软件：您可以 redistribute 它并根据 GNU Affero General Public License 的条款
 * 修改它，要么按照版本 3，要么（根据您的选择）任何后来的版本。
 *
 * 本程序旨在有用，但 WITHOUT ANY WARRANTY；没有明示的也不暗示的担保
 * MERCHANTABILITY 或 FITNESS FOR A PARTICULAR PURPOSE。  See the
 * GNU Affero General Public License 以获取更多详情。
 *
 * 您应该收到了 GNU Affero General Public License 的副本
 * 如果没有，请参阅 <https://www.gnu.org/licenses/>。
 */
```

**导入顺序**:
1. 标准 Java 导入
2. 第三方库（按字母顺序）
3. 组之间空一行

**命名约定**:
- 类: PascalCase (`BackupService`, `Global`)
- 方法: camelCase (`run`, `getOptions`)
- 变量: camelCase (`fileItem`, `uploadUtil`)
- 常量: UPPER_SNAKE_CASE (`MAX_UPLOAD_SIZE`)

**错误处理**:
- 使用 SLF4J: `Logger.getLogger(ClassName.class)`
- 捕获特定异常，不要用通用的 `Exception`
- 使用适当日志级别: debug, info, warn, error

### 前端代码风格

**SCSS**:
- 输出样式: `compressed`
- 使用 `/* 注释 */`
- 导入路径包含 `node_modules`

**JavaScript**:
- 压缩文件使用 ASCII
- 使用 `const` 和 `let`，不使用 `var`
- 优先 ES6+ 语法

**HTML/Freemarker (.ftl)**:
- HTML5 规范
- 正确使用 Freemarker 转义

---

## 质量保证与安全

### 代码检查
- 运行: `mvn license:format`
- 确保: 所有 Java 文件都有许可证头部
- 检查: 配置文件完整性

### 测试规范
- 框架: TestNG（不是 JUnit）
- 位置: `src/test/java/`
- 配置: surefire 插件设置 `useSystemClassLoader=false`

### 依赖项安全（已加固）
- SLF4J: 2.0.9（修复日志注入漏洞）
- TestNG: 7.10.2（修复反序列化漏洞）
- Jsoup: 1.14.3（修复 SSRF 漏洞）
- H2 Database: 2.2.224（最新版本）
- SnakeYAML: 1.33（修复反序列化漏洞）
- Jetty: 9.4.53（修复 HTTP/2 漏洞）
- Flexmark: 0.64.8（修复反序列化漏洞）

### 安全最佳实践
1. 使用 PreparedStatement 防止 SQL 注入
2. 输入验证和清理
3. 密码使用哈希存储（BCrypt）
4. 敏感信息不硬编码
5. 定期更新依赖项
6. 代码审查流程

---

## 源文件结构

```
src/main/java/
├── org/b3log/solo/bolo/           # Bolo 专用扩展
│   ├── pic/                       # 图片上传
│   ├── prop/                      # 属性和邮件服务
│   └── ...
└── pers/adlered/                  # 自定义扩展
    └── ...

src/main/webapp/
├── js/
│   ├── admin/                     # 管理脚本
│   ├── lib/                       # 库
│   └── skins/                     # 主题脚本
├── skins/                         # 多个主题
│   ├── bolo-bubble/
│   ├── bolo-casper/
│   └── ...
├── scss/                          # 主 SCSS 文件
└── plugins/                       # 前端插件
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
- **日志**: SLF4J 2.0.9

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

### 运行单个测试
```bash
mvn test -Dtest=YourTestClass
mvn test -Dtest=YourTestClass#testMethod
```

### 安全加固清单
- [ ] 更新过时依赖
- [ ] 检查硬编码凭据
- [ ] 验证 SQL 注入防护
- [ ] 实施输入验证
- [ ] 添加 CSRF 保护
- [ ] 配置 HTTP 安全头
- [ ] 测试依赖项漏洞

---

## 持续维护

### 依赖项管理
```bash
# 更新依赖
mvn versions:display-dependency-updates

# 生成报告
mvn license:check

# 代码质量检查
mvn clean verify
```

### 定期检查
- 每月更新依赖项
- 季度安全审计
- 定期备份
- 性能监控

---

## 项目状态

✅ **当前状态**:
- JDK: 21
- 安全漏洞: 0 个（已修复 15 个）
- 构建状态: 成功
- 风险等级: 低

**下一步行动**:
1. 保持当前稳定版本
2. 定期依赖项更新
3. 关注社区反馈
4. 评估升级时机
