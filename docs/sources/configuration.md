# 配置指南

`JmConfiguration` 使用 Builder 模式创建，所有配置项都有合理的默认值，不配置也可以直接使用。

## 简单配置

```java
JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.API)
        .build();
```

## 完整配置项

```java
JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.HTML)           // 客户端类型: API 或 HTML
        .apiDomains(List.of("https://..."))    // 自定义 API 域名列表
        .htmlDomains(List.of("https://..."))   // 自定义 HTML 域名列表
        .proxy("127.0.0.1", 7890)              // HTTP 代理
        .header("User-Agent", "custom-ua")     // 自定义请求头
        .timeout(Duration.ofSeconds(60))        // 请求超时（默认 30s）
        .imageTimeout(Duration.ofSeconds(120))  // 图片下载超时（默认 60s）
        .retryTimes(10)                         // 重试次数（默认 5）
        .downloadThreadPoolSize(12)             // 下载线程池大小（默认 CPU 核心数）
        .cacheSize(100 * 1024 * 1024)           // 缓存大小（默认 100MB）
        .closeTimeoutMs(30_000)                 // 关闭超时（默认 60s）
        .domainProbeIntervalMs(600_000)         // 域名探活间隔（默认 10min）
        .domainProbeTimeoutMs(3000)             // 域名探活超时（默认 3s）
        .build();
```

## 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `clientType` | `ClientType` | `API` | 客户端类型，推荐 `API` |
| `apiDomains` | `List<String>` | 自动获取 | API 客户端使用的域名 |
| `htmlDomains` | `List<String>` | 自动获取 | HTML 客户端使用的域名 |
| `proxy` | `Proxy` | 无 | 通过 `proxy(host, port)` 设置 |
| `headers` | `Map` | 空 | 通过 `header(key, value)` 逐个添加 |
| `timeout` | `Duration` | 30s | 普通请求超时 |
| `imageTimeout` | `Duration` | 60s | 图片下载超时 |
| `retryTimes` | `int` | 5 | 请求失败重试次数 |
| `downloadThreadPoolSize` | `int` | CPU 核心数 | `-1` 表示使用默认值 |
| `cacheSize` | `int` | 100MB | 响应缓存大小（Byte） |
| `closeTimeoutMs` | `long` | 60000 | 关闭时等待进行中任务完成 |
| `domainProbeIntervalMs` | `long` | 600000 | 后台域名探活间隔 |
| `domainProbeTimeoutMs` | `long` | 3000 | 初始域名探活单域名超时 |

## API vs HTML 客户端

| 特性 | API 客户端（推荐） | HTML 客户端 |
|------|-------------------|-------------|
| 稳定性 | 高（基于官方 API） | 中（易受网页改版影响） |
| 性能 | 高 | 中（需解析大量 HTML） |
| Cloudflare | 几乎不受影响 | 可能被拦截 |
| 数据完整性 | 大部分数据齐全 | 非常完整 |
| 使用场景 | 常规数据获取、下载 | 获取 API 没有的特定信息 |

## 从 Properties 文件加载配置

```java
InputStream is = getClass().getResourceAsStream("/jmcomic.properties");
JmConfiguration config = new JmConfiguration.Builder()
        .loadFromProperties(is)
        .build();
```

`jmcomic.properties` 示例：

```properties
client.type=API
proxy.host=127.0.0.1
proxy.port=7890
timeout.seconds=60
image.timeout.seconds=120
retry.times=10
download.thread.pool.size=12
cache.size=104857600
domain.probe.interval.ms=600000
domain.probe.timeout.ms=3000
close.timeout.ms=30000
header.User-Agent=custom-ua
api.domains=https://api1.example.com, https://api2.example.com
html.domains=https://www.example.com
```
