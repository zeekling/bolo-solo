# Solo Latke 3.x 迁移模式分析

## 概述

本文档分析了 Solo 官方仓库（88250/solo）的 Latke 3.x 迁移方案，为 Bolo-Solo 的迁移提供参考。

**关键发现**: Bolo-Solo 已使用 Latke 3.4.30，但采用 Servlet 容器部署模式（WAR），而 Solo 已迁移到独立 Server 模式（JAR）。

## 1. Server.java 结构概览

### Solo 的 Server.java 关键结构

```java
public final class Server extends BaseServer {
    
    // 版本常量
    public static final String VERSION = "4.4.0";
    
    // 内存日志写入器
    public static final TailStringWriter TAIL_LOGGER_WRITER = new TailStringWriter();
    
    public static void main(final String[] args) {
        // 1. TLS 配置
        System.setProperty("https.protocols", "TLSv1.2");
        
        // 2. 初始化内存日志
        initInMemoryLogger();
        
        // 3. Latke 初始化
        Latkes.setScanPath("org.b3log.solo");
        Latkes.init();
        
        // 4. 路由注册
        routeProcessors();
        
        // 5. 服务初始化
        final InitService initService = beanManager.getReference(InitService.class);
        initService.initTables();
        
        // 6. 启动服务器
        final Server server = new Server();
        server.start(port);  // 或 server.start(unixDomainSocketPath);
    }
    
    public static void routeProcessors() {
        // 全局 Handler 链
        Dispatcher.startRequestHandler = new BeforeRequestHandler();
        Dispatcher.HANDLERS.add(1, new SkinHandler());
        Dispatcher.HANDLERS.add(2, new InitCheckHandler());
        Dispatcher.HANDLERS.add(3, new PermalinkHandler());
        Dispatcher.endRequestHandler = new AfterRequestHandler();
        
        routeConsoleProcessors();
        routeIndexProcessors();
        Dispatcher.mapping();
    }
}
```

### Bolo-Solo 当前架构（对比）

Bolo-Solo 使用 `SoloServletListener` + `Starter`（Jetty 嵌入）：

```java
// SoloServletListener.java - Servlet 容器模式
public final class SoloServletListener extends AbstractServletListener {
    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        Latkes.setScanPath("org.b3log.solo");
        super.contextInitialized(servletContextEvent);
        Dispatcher.HANDLERS.add(0, new InitCheckHandler());
        Dispatcher.HANDLERS.add(1, new PermalinkHandler());
        
        routeConsoleProcessors();
        // ... 服务初始化
    }
}
```

## 2. 函数式路由模式

### 基本路由定义

Solo 使用流畅的函数式 API 定义路由：

```java
// 简单 GET 路由
Dispatcher.get("/admin-index.do", adminConsole::showAdminIndex);

// 多路径路由
Dispatcher.route().get(new String[] {
    "/admin-article.do",
    "/admin-article-list.do",
    "/admin-comment-list.do"
}, adminConsole::showAdminFunctions);

// 路径变量
Dispatcher.get("/console/article/{id}", articleConsole::getArticle);

// 复杂路径变量
Dispatcher.get("/console/articles/status/{status}/{page}/{pageSize}/{windowSize}",
    articleConsole::getArticles);
```

### RouterGroup 模式（带中间件）

```java
// 带 Middleware 的路由组
final ArticleProcessor articleProcessor = beanManager.getReference(ArticleProcessor.class);
final Dispatcher.RouterGroup articleGroup = Dispatcher.group();
articleGroup.post("/console/markdown/2html", articleProcessor::markdown2HTML)
    .get("/console/article-pwd", articleProcessor::showArticlePwdForm)
    .post("/console/article-pwd", articleProcessor::onArticlePwdForm)
    .get("/articles/random.json", articleProcessor::getRandomArticles);

// 带 Middleware 的路由组
final CategoryProcessor categoryProcessor = beanManager.getReference(CategoryProcessor.class);
final Dispatcher.RouterGroup categoryGroup = Dispatcher.group();
categoryGroup.middlewares(staticMidware::handle);  // 添加中间件
categoryGroup.get("/articles/category/{categoryURI}", categoryProcessor::getCategoryArticlesByPage)
    .get("/category/{categoryURI}", categoryProcessor::showCategoryArticles);

// Router 模式（多 HTTP 方法）
final FeedProcessor feedProcessor = beanManager.getReference(FeedProcessor.class);
final Dispatcher.RouterGroup feedGroup = Dispatcher.group();
feedGroup.middlewares(staticMidware::handle);
feedGroup.router()
    .get().head().uri("/atom.xml").handler(feedProcessor::blogArticlesAtom)
    .get().head().uri("/rss.xml").handler(feedProcessor::blogArticlesRSS);

// 多路径 + 方法路由
indexGroup.router().get(new String[]{"", "/", "/index.html"}, indexProcessor::showIndex);
```

### 后台认证路由组

```java
// Console 认证中间件
final ConsoleAuthMidware consoleAuthMidware = beanManager.getReference(ConsoleAuthMidware.class);
final ConsoleAdminAuthMidware consoleAdminAuthMidware = beanManager.getReference(ConsoleAdminAuthMidware.class);

// 普通用户认证
final ArticleConsole articleConsole = beanManager.getReference(ArticleConsole.class);
final Dispatcher.RouterGroup articleConsoleGroup = Dispatcher.group();
articleConsoleGroup.middlewares(consoleAuthMidware::handle);  // 认证中间件
articleConsoleGroup.get("/console/article/{id}", articleConsole::getArticle)
    .delete("/console/article/{id}", articleConsole::removeArticle)
    .put("/console/article/", articleConsole::updateArticle)
    .post("/console/article/", articleConsole::addArticle);

// 管理员认证
final CategoryConsole categoryConsole = beanManager.getReference(CategoryConsole.class);
final Dispatcher.RouterGroup categoryGroup = Dispatcher.group();
categoryGroup.middlewares(consoleAdminAuthMidware::handle);  // 管理员认证
categoryGroup.put("/console/category/order/", categoryConsole::changeOrder)
    .get("/console/category/{id}", categoryConsole::getCategory);
```

### 单路由 + 中间件

```java
// 单个路由带中间件
final FetchUploadProcessor fetchUploadProcessor = beanManager.getReference(FetchUploadProcessor.class);
Dispatcher.post("/upload/fetch", fetchUploadProcessor::fetchUpload, consoleAuthMidware::handle);
```

## 3. 处理器迁移示例

### 处理器类定义（无变化）

```java
@Singleton
public class ArticleProcessor {
    
    @Inject
    private ArticleQueryService articleQueryService;
    
    // 方法签名：RequestContext -> void
    public void showArticle(final RequestContext context) {
        final JSONObject article = (JSONObject) context.attr(Article.ARTICLE);
        if (null == article) {
            context.sendError(404);
            return;
        }
        
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "article.ftl");
        // ... 处理逻辑
    }
    
    public void getArticlesByPage(final RequestContext context) {
        final JSONObject jsonObject = new JSONObject();
        // ... 处理逻辑
        context.renderJSON(jsonObject);  // 快捷 JSON 渲染
    }
}
```

### 关键 RequestContext 方法

```java
// 获取参数
String param = context.param("id");
String pathVar = context.pathVar("id");
JSONObject json = context.requestJSON();

// 响应处理
context.sendError(404);
context.sendRedirect(url);
context.renderJSON(result);  // 快捷方法
context.setRenderer(renderer);

// 请求/响应访问
Request request = context.getRequest();
Response response = context.getResponse();

// 属性传递
Object attr = context.attr(Keys.HttpRequest.REQUEST_URI);
context.attr(Article.ARTICLE, article);

// 中间件控制
context.handle();  // 继续处理链
context.abort();   // 中止处理链
```

## 4. 中间件实现

### 认证中间件示例

```java
@Singleton
public class ConsoleAuthMidware {
    
    public void handle(final RequestContext context) {
        final JSONObject currentUser = Solos.getCurrentUser(context);
        if (null == currentUser) {
            context.sendError(401);
            context.abort();  // 重要：中止处理链
            return;
        }
        
        final String userRole = currentUser.optString(User.USER_ROLE);
        if (Role.VISITOR_ROLE.equals(userRole)) {
            context.sendError(403);
            context.abort();
            return;
        }
        
        context.handle();  // 重要：继续处理链
    }
}
```

### 功能中间件示例（静态化）

```java
@Singleton
public class StaticMidware {
    
    public void handle(final RequestContext context) {
        final String html = Statics.get(context);
        if (null == html) {
            context.handle();  // 无缓存，继续处理
            return;
        }
        
        // 有缓存，直接返回
        context.getResponse().setContentType("text/html; charset=utf-8");
        context.sendString(html);
        context.abort();  // 中止处理链
    }
}
```

### 全局 Handler 示例

```java
public class BeforeRequestHandler implements Handler {
    
    @Override
    public void handle(final RequestContext context) {
        context.attr(Keys.HttpRequest.START_TIME_MILLIS, System.currentTimeMillis());
        
        final String remoteAddr = Requests.getRemoteAddr(context.getRequest());
        if (BLACKLIST_IPS.contains(remoteAddr)) {
            context.setStatus(429);
            context.setHeader("Retry-After", "600");
            context.sendString("Too Many Requests");
            context.abort();
            return;
        }
        
        Stopwatchs.start("Request Initialized [requestURI=" + context.requestURI() + "]");
        // 注意：没有调用 context.handle()，因为是 startRequestHandler
    }
}
```

## 5. 迁移关键模式总结

### 5.1 路由注册模式

| 功能 | Solo 模式 | Bolo-Solo 当前模式 |
|------|-----------|-------------------|
| 简单路由 | `Dispatcher.get(uri, handler)` | ✅ 已使用 |
| 路径变量 | `Dispatcher.get("/article/{id}", handler)` | ✅ 已使用 |
| 路由组 | `Dispatcher.group()` + `middlewares()` | ❌ 未使用 |
| 多方法路由 | `router().get().head().uri()` | ❌ 未使用 |
| 多路径路由 | `Dispatcher.route().get(paths, handler)` | ✅ 已使用 |

### 5.2 中间件模式

| 功能 | Solo 模式 | Bolo-Solo 当前模式 |
|------|-----------|-------------------|
| 全局 Handler | `Dispatcher.HANDLERS.add()` | ✅ 已使用 |
| 路由级中间件 | `group.middlewares(midware)` | ❌ 未使用 |
| 单路由中间件 | `Dispatcher.post(uri, handler, midware)` | ❌ 未使用 |
| 认证中间件 | `ConsoleAuthMidware::handle` | ❌ 内联在方法中 |

### 5.3 服务器架构

| 功能 | Solo 模式 | Bolo-Solo 当前模式 |
|------|-----------|-------------------|
| 服务器类型 | `BaseServer` 独立运行 | `AbstractServletListener` + Jetty |
| 入口类 | `Server.main()` | `Starter.main()` |
| 部署方式 | JAR | WAR |
| Handler 链 | 4 个 Handler | 2 个 Handler |

## 6. Bolo-Solo 迁移建议

### 6.1 短期优化（无需架构变更）

1. **添加路由组中间件支持**
   - 将认证逻辑从方法内部移到中间件
   - 使用 `Dispatcher.group()` + `middlewares()` 重构后台路由

2. **统一错误处理**
   - 使用 `context.sendError()` 替代手动错误响应
   - 使用 `context.abort()` 明确中止处理链

### 6.2 中期迁移（架构调整）

1. **创建 Server.java**
   - 继承 `BaseServer`
   - 整合 `SoloServletListener` 和 `Starter` 功能
   - 支持独立运行和 Docker 部署

2. **重构 Handler 链**
   ```java
   Dispatcher.startRequestHandler = new BeforeRequestHandler();
   Dispatcher.HANDLERS.add(1, new SkinHandler());
   Dispatcher.HANDLERS.add(2, new InitCheckHandler());
   Dispatcher.HANDLERS.add(3, new PermalinkHandler());
   Dispatcher.endRequestHandler = new AfterRequestHandler();
   ```

3. **迁移到 JAR 部署**
   - 修改 pom.xml 打包方式
   - 移除 web.xml 依赖
   - 更新 Docker 构建脚本

### 6.3 需要注意的差异

1. **Bolo-Solo 特有功能**
   - `KanBanNiangProcessor` - 看板娘
   - `CommentProcessor` - 评论处理
   - `MailService` - 邮件服务
   - `WAF` - Web 应用防火墙
   - `FollowConsole` - 关注功能

2. **路由数量差异**
   - Solo 后台路由更简洁
   - Bolo-Solo 有更多自定义路由

## 7. 代码示例：认证中间件迁移

### 迁移前（内联认证）

```java
@Singleton
public class ArticleConsole {
    public void getArticle(final RequestContext context) {
        final JSONObject currentUser = Solos.getCurrentUser(context);
        if (null == currentUser) {
            context.sendError(401);
            return;
        }
        // ... 业务逻辑
    }
}
```

### 迁移后（中间件）

```java
// 1. 创建中间件（复用现有代码）
@Singleton
public class ConsoleAuthMidware {
    public void handle(final RequestContext context) {
        final JSONObject currentUser = Solos.getCurrentUser(context);
        if (null == currentUser) {
            context.sendError(401);
            context.abort();
            return;
        }
        context.handle();
    }
}

// 2. 路由注册时添加中间件
final ArticleConsole articleConsole = beanManager.getReference(ArticleConsole.class);
final Dispatcher.RouterGroup articleGroup = Dispatcher.group();
articleGroup.middlewares(consoleAuthMidware::handle);
articleGroup.get("/console/article/{id}", articleConsole::getArticle);

// 3. 处理器简化
public void getArticle(final RequestContext context) {
    // 认证已由中间件处理
    final String articleId = context.pathVar("id");
    // ... 业务逻辑
}
```

## 8. 参考资源

- Solo 仓库：https://github.com/88250/solo
- Solo Server.java：`src/main/java/org/b3log/solo/Server.java`
- Solo 处理器：`src/main/java/org/b3log/solo/processor/`
- Solo 中间件：`src/main/java/org/b3log/solo/processor/console/ConsoleAuthMidware.java`
- Latke 版本：3.4.30（Solo 和 Bolo-Solo 相同）
