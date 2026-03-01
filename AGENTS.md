# Bolo-Solo 代码库代理指南

## 项目概述

Bolo-Solo 是一个基于 Solo 的稳定、美丽的博客系统，使用 Java 编写，结合 Maven 和前端技术。

## 构建、代码检查和测试命令

### 前端构建（Gulp）
```bash
npm run build    # 构建所有前端资源
npm run dev      # 监听模式构建
gulp            # npm run build 的别名
gulp watch      # 开发模式
```

### Java 构建（Maven）
```bash
mvn clean       # 清理构建产物
mvn package     # 构建 WAR 包
mvn test        # 运行所有测试
mvn test -Dtest=TestClassName#methodName  # 运行单个测试
mvn verify      # 运行测试和站点生成
```

### 测试执行
```bash
mvn surefire:test              # 运行所有测试
mvn test -Dtest=PackageTestClass  # 运行单个测试类
mvn test -Dtest=PackageTestClass#testMethod  # 运行单个方法
mvn test -Dtest=*Test          # 运行所有 Test 后缀的测试
```

### Docker 部署
```bash
docker build -t bolo-solo .    # 构建 Docker 镜像
docker-compose up -d           # 使用 docker-compose 启动
```

## 代码风格指南

### Java 代码风格

**包命名：**
- 原始 Solo 代码：`org.b3log.solo.*`
- Bolo 扩展：`org.b3log.solo.bolo.*` 和 `pers.adlered.*`

**许可证头部：**
所有 Java 源文件必须在顶部包含 AGPLv3 许可证头部：

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

**导入顺序：**
- 标准 Java 导入优先
- 第三方库导入（按字母顺序排列）
- 组之间空一行

**命名约定：**
- 类：PascalCase（例如 `BackupService`、`Global`）
- 方法：camelCase（例如 `run`、`getOptions`）
- 变量：camelCase（例如 `fileItem`、`uploadUtil`）
- 常量：UPPER_SNAKE_CASE（例如 `MAX_UPLOAD_SIZE`）

**错误处理：**
- 使用 SLF4J 记录器进行日志记录：`Logger.getLogger(ClassName.class)`
- 捕获特定异常而不是通用的 `Exception`
- 使用适当的日志级别（debug、info、warn、error）

### 前端代码风格

**SCSS：**
- 输出样式：`compressed`
- 使用 SLASHSTAR_STYLE 映射（/* 注释 */）
- 导入路径应包含 `node_modules`

**JavaScript：**
- 压缩文件使用 ASCII 输出
- 使用 `const` 和 `let` 而不是 `var`
- 优先使用 ES6+ 语法

**HTML/Freemarker 模板（.ftl）：**
- 遵循 HTML5 约定
- 正确使用 Freemarker 转义

### 源文件结构

```
src/main/java/
├── org/b3log/solo/bolo/           # Bolo 专用扩展
│   ├── pic/                       # 图片上传功能
│   ├── prop/                      # 属性和邮件服务
│   └── ...
└── pers/adlered/                  # 自定义 bolo 添加
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

## 质量保证

### 代码格式化
- Maven license-maven-plugin 强制执行许可证头部
- 提交前代码必须通过许可证检查

### 测试
- 使用 TestNG 框架（不是 JUnit）
- 测试文件在 `src/test/java/` 目录中
- 在 surefire 插件中启用 `useSystemClassLoader=false`

### 静态分析
- Jacoco 用于代码覆盖率
- Coveralls 用于覆盖率报告
- License Maven Plugin 用于合规性

## Git 工作流程

### 分支策略
- `master`：生产代码
- `dev/*`：solo 迁移的功能分支
- 功能开发通常从 solo 或 bolo-solo main 开始

### 提交消息
- 使用清晰、描述性的消息
- 适当时引用问题
- 格式：`<类型>: <描述>`（例如 "feat: 添加邮件服务"）

## 数据库

- MySQL 连接器（版本 8.0.18）
- H2 数据库用于测试
- 架构迁移由项目结构处理

## 依赖关系

### 主要 Java 库
- Latke 2.5.8（Web 框架）
- Flexmark 0.62.2（Markdown 渲染）
- Jsoup 1.12.1（HTML 解析）
- SLF4J 1.7.28（日志）

### 前端库
- jQuery 3.1.0
- Vditor 3.8.5（Markdown 编辑器）
- SASS 1.51.0

## 常见任务

### 添加新功能
1. 在 `org.b3log.solo.bolo` 或 `pers.adlered` 下创建适当的包结构
2. 在所有新的 Java 文件中包含许可证头部
3. 如果功能可测试，则添加单元测试
4. 运行 `mvn clean package` 验证构建
5. 如果需要 UI 更改，更新前端资源

### 运行单个测试
```bash
mvn test -Dtest=YourTestClass
mvn test -Dtest=YourTestClass#testMethod
```

### 构建 Docker 镜像
```bash
docker build -t bolo-solo:latest .
docker run -p 8080:8080 bolo-solo:latest
```
