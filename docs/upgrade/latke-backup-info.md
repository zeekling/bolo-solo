# Latke 升级前备份信息

## 备份信息
- **备份日期**: 2025-03-15
- **备份分支**: backup/latke-2.5.8-20250315
- **源分支**: main_new
- **最新提交**: 5d3dc261 更新agents.md

## 当前 Latke 依赖版本 (2.5.8)

### org.b3log 依赖
| Artifact | Version |
|----------|---------|
| latke-core | 2.5.8 |
| latke-repository-mysql | 2.5.8 |
| latke-repository-h2 | 2.5.8 |

### 相关依赖版本
| Component | Version |
|-----------|---------|
| Jetty | 9.4.53.v20231009 |
| Servlet API | 3.1.0 |
| H2 Database | 2.2.224 |
| MySQL Connector | 8.0.33 |
| SLF4J | 1.7.28 |
| Flexmark | 0.64.8 |
| Jsoup | 1.14.3 |
| SnakeYAML | 1.33 |

## 升级目标
- **目标 Latke 版本**: 3.4.30
- **主要变更**: Jetty → Netty (需要 Servlet 迁移)

## 注意事项
1. Latke 3.x 使用 Netty 替代 Jetty
2. 需要迁移 Servlet 相关代码
3. 建议在单独的分支进行升级工作
