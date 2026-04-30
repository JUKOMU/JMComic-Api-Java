# 安装与依赖

## 环境要求

- **Java 17** 或更高版本
- Maven / Gradle 构建工具

## Maven 依赖

### 标准安装

大多数情况下，你只需要安装 `jmcomic-core`，它会把 `jmcomic-api` 作为传递依赖一起引入：

```xml
<dependency>
    <groupId>io.github.jukomu</groupId>
    <artifactId>jmcomic-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 仅使用接口

如果你只需要数据模型和接口定义（不依赖 OkHttp / Gson / Jsoup 等第三方库），可以只引入 `jmcomic-api`：

```xml
<dependency>
    <groupId>io.github.jukomu</groupId>
    <artifactId>jmcomic-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Android 平台

在 Android 项目中额外添加：

```xml
<dependency>
    <groupId>io.github.jukomu</groupId>
    <artifactId>jmcomic-android-support</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 传递依赖

`jmcomic-core` 会自动引入以下依赖：

| 依赖 | 用途 |
|------|------|
| OkHttp 4.12+ | HTTP 网络请求 |
| Gson 2.13+ | JSON 解析 |
| Jsoup 1.17+ | HTML 解析（HTML 客户端） |
| webp-imageio 0.1+ | WebP 图片支持 |
| Apache Commons Lang3 | 工具类 |
| SLF4J | 日志门面（需自行提供实现） |

## 下一步

- [下载第一个本子](first-download.md)
- [获取数据示例](fetch-data.md)
- [配置指南](../configuration.md)
