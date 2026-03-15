# Latke 2.5.8 -> 3.4.30 升级计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 org.b3log:latke-core 从 2.5.8 升级到 3.4.30，同时更新相关的 latke-repository-mysql 和 latke-repository-h2 依赖。

**Architecture:** 
- Latke 3.x 相比 2.x 有重大架构变更：从 Servlet/Jetty 切换到 Netty 异步框架
- 需要验证 API 兼容性，特别是 RequestContext、Repository、Service 等核心接口
- 升级后需确保所有 53 个 @RequestProcessor 和相关处理器正常工作

**Tech Stack:** 
- Java 21 (已兼容)
- Latke 3.4.30 (Netty-based)
- MySQL 8.0.33 / H2 2.2.224
- TestNG 7.10.2

---

## 重要提示：重大变更

Latke 3.x 主要变更：
1. **Web 容器**: Jetty -> Netty (异步非阻塞)
2. **日志框架**: SLF4J -> Log4j2
3. **依赖注入**: API 可能有调整
4. **RequestContext**: 接口可能变化

---

## Task 1: 环境准备与备份

**Files:**
- Create: `docs/upgrade/latke-backup-info.md`
- Modify: N/A

**Step 1: 记录当前状态**
```bash
git status
git log -1 --oneline
```
预期输出: 显示当前分支和最新提交信息

**Step 2: 创建备份分支**
```bash
git checkout -b backup/latke-2.5.8-$(date +%Y%m%d)
git checkout master
```
预期输出: 切换到新分支后切回 master

**Step 3: 记录当前依赖版本**
```bash
mvn dependency:list | grep org.b3log > docs/upgrade/latke-backup-info.md
```

**Step 4: 提交备份信息**
```bash
git add docs/upgrade/latke-backup-info.md
git commit -m "chore: record latke 2.5.8 dependency state before upgrade"
```

---

## Task 2: 更新 pom.xml 版本号

**Files:**
- Modify: `pom.xml:98`

**Step 1: 修改版本属性**
将 pom.xml 第 98 行：
```xml
<org.b3log.latke.version>2.5.8</org.b3log.latke.version>
```
修改为：
```xml
<org.b3log.latke.version>3.4.30</org.b3log.latke.version>
```

**Step 2: 验证修改**
```bash
grep "org.b3log.latke.version" pom.xml
```
预期输出: `<org.b3log.latke.version>3.4.30</org.b3log.latke.version>`

**Step 3: 提交变更**
```bash
git add pom.xml
git commit -m "feat: upgrade latke from 2.5.8 to 3.4.30"
```

---

## Task 3: 尝试编译并识别编译错误

**Files:**
- 无文件修改，仅用于识别问题

**Step 1: 清理并编译**
```bash
mvn clean compile -DskipTests 2>&1 | tee build-errors.log
```
预期输出: 编译错误列表

**Step 2: 分析错误类型**
```bash
grep -E "cannot find symbol|package.*does not exist|method.*cannot be applied" build-errors.log | sort -u
```

**Step 3: 记录错误文件列表**
```bash
grep -E "error:" build-errors.log | awk -F: '{print $1}' | sort -u > affected-files.txt
```

---

## Task 4: 处理日志框架变更 (SLF4J -> Log4j2)

**Files:**
- Modify: `pom.xml` (添加 Log4j2 依赖，调整 SLF4J)

**Step 1: 检查当前日志配置**
```bash
grep -r "slf4j" pom.xml
grep -r "log4j" pom.xml
```

**Step 2: 添加 Log4j2 依赖（如果 Latke 3.x 需要）**
在 pom.xml 的 `<dependencies>` 中添加：
```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.20.0</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>2.20.0</version>
</dependency>
```

**Step 3: 移除或调整旧的 SLF4J 依赖**
检查是否有冲突的日志依赖需要排除。

**Step 4: 验证日志依赖**
```bash
mvn dependency:tree | grep -E "slf4j|log4j"
```

---

## Task 5: 修复 RequestContext API 变更

**Files:**
- Modify: 所有使用 `RequestContext` 的处理器文件

**Step 1: 检查 RequestContext 使用模式**
```bash
grep -rn "context\.getRequest()\|context\.getResponse()\|context\.attr" src/main/java/ > requestcontext-usage.txt
```

**Step 2: 逐文件检查并修复**
针对每个使用 RequestContext 的文件，检查：
- `context.getRequest()` -> 可能需要改为新的 API
- `context.getResponse()` -> 可能需要改为新的 API  
- `context.sendRedirect()` -> 确认参数签名
- `context.attr()` -> 确认属性操作 API

**Step 3: 批量修复常见模式（如果 API 变更确定）**
根据实际编译错误进行修复。

**Step 4: 编译验证**
```bash
mvn compile -DskipTests 2>&1 | head -100
```

---

## Task 6: 修复 Repository API 变更

**Files:**
- Modify: 所有 Repository 相关文件

**Step 1: 检查 Repository 使用**
```bash
grep -rn "extends.*Repository\|implements.*Repository" src/main/java/
```

**Step 2: 检查 Query API 变更**
```bash
grep -rn "new Query\|Query()\|FilterOperator\|PropertyFilter" src/main/java/ > query-usage.txt
```

**Step 3: 修复 Repository 接口变更**
根据编译错误修复：
- `Repository.add()` 签名
- `Repository.get()` 返回类型
- `Query` 构造方式

---

## Task 7: 修复 Service 和 IOC 注解变更

**Files:**
- Modify: 所有 Service 类文件

**Step 1: 检查 Service 注解**
```bash
grep -rn "@Service\|@Inject" src/main/java/ | head -50
```

**Step 2: 验证 BeanManager 使用**
```bash
grep -rn "BeanManager" src/main/java/
```

**Step 3: 修复注入问题**
根据编译错误修复依赖注入相关代码。

---

## Task 8: 修复 Servlet API 兼容性

**Files:**
- Modify: 所有使用 Servlet API 的文件

**Step 1: 检查 Servlet API 使用**
```bash
grep -rn "import javax.servlet\|import jakarta.servlet" src/main/java/ > servlet-imports.txt
```

**Step 2: 确认 Servlet API 版本**
Latke 3.x 可能使用 Jakarta EE (jakarta.servlet.*) 而非 Java EE (javax.servlet.*)

**Step 3: 批量更新导入（如需要）**
如果需要切换到 Jakarta：
```bash
find src/main/java -name "*.java" -exec sed -i 's/import javax.servlet/import jakarta.servlet/g' {} \;
```

---

## Task 9: 更新配置文件

**Files:**
- Modify: `src/main/resources/*.properties`
- Modify: `src/main/webapp/WEB-INF/web.xml` (如果存在)

**Step 1: 检查 latke 配置**
```bash
find src/main/resources -name "*.properties" -exec grep -l "latke\|runtimeMode\|jdbc" {} \;
```

**Step 2: 更新配置属性**
根据 Latke 3.x 文档更新必要的配置项。

**Step 3: 检查 web.xml**
```bash
ls -la src/main/webapp/WEB-INF/web.xml 2>/dev/null || echo "No web.xml"
```

---

## Task 10: 修复测试代码

**Files:**
- Modify: `src/test/java/**/*.java`

**Step 1: 运行测试识别失败**
```bash
mvn test -Dmaven.test.failure.ignore=true 2>&1 | tee test-errors.log
```

**Step 2: 分析测试失败原因**
```bash
grep -E "FAILED|Error|Exception" test-errors.log | head -30
```

**Step 3: 修复测试代码**
根据测试失败原因进行针对性修复。

---

## Task 11: 完整构建验证

**Files:**
- 无

**Step 1: 清理并完整构建**
```bash
mvn clean package -DskipTests
```
预期输出: BUILD SUCCESS

**Step 2: 运行所有测试**
```bash
mvn test
```
预期输出: 所有测试通过

**Step 3: 检查 WAR 包**
```bash
ls -la target/bolo.war
```
预期输出: WAR 文件存在且大小合理

---

## Task 12: 许可证检查

**Files:**
- 无

**Step 1: 运行许可证检查**
```bash
mvn license:check
```
预期输出: 无错误

**Step 2: 格式化许可证（如有问题）**
```bash
mvn license:format
```

**Step 3: 提交许可证修复**
```bash
git add -A
git commit -m "style: fix license headers after latke upgrade"
```

---

## Task 13: 最终提交与文档更新

**Files:**
- Modify: `CHANGELOG.md` 或版本说明

**Step 1: 查看所有变更**
```bash
git status
git diff --stat
```

**Step 2: 最终提交**
```bash
git add -A
git commit -m "feat: complete latke 3.4.30 upgrade

- Upgrade org.b3log:latke-core from 2.5.8 to 3.4.30
- Update latke-repository-mysql and latke-repository-h2
- Adapt to Latke 3.x API changes
- Fix compatibility issues with RequestContext and Repository
- Update logging framework configuration"
```

**Step 3: 创建升级总结文档**
在 `docs/upgrade/latke-3.4.30-migration.md` 中记录：
- 升级过程中的主要变更
- 遇到的问题和解决方案
- 需要注意的后续事项

---

## 回滚计划

如果升级失败需要回滚：

```bash
git checkout backup/latke-2.5.8-YYYYMMDD
git checkout -b rollback/latke-2.5.8
git push origin rollback/latke-2.5.8
```

---

## 注意事项

1. **Netty 兼容**: Latke 3.x 使用 Netty，需确保与现有 Jetty 部署兼容
2. **日志迁移**: SLF4J -> Log4j2 可能需要更新日志配置文件
3. **数据库连接**: HikariCP 配置可能有变化
4. **Servlet API**: 检查是否需要迁移到 Jakarta EE
5. **测试覆盖**: 确保所有测试通过后再部署

---

## 参考资源

- Latke GitHub: https://github.com/88250/latke
- Maven Central: https://central.sonatype.com/artifact/org.b3log/latke-core
- Solo (参考项目): https://github.com/88250/solo
