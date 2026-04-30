# 模块化集成

## 模块依赖关系

```
jmcomic-api          ← 纯接口，零第三方依赖
    ↑
jmcomic-core         ← 完整实现（OkHttp + Gson + Jsoup）
    ↑               ↑
jmcomic-sample       jmcomic-android-support
```

## 仅使用接口模块

如果你不需要下载功能，只需要数据模型和接口定义：

```xml
<dependency>
    <groupId>io.github.jukomu</groupId>
    <artifactId>jmcomic-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

这适合：
- 只在项目中定义 JM 相关数据模型
- 需要自定义实现 JmClient 接口
- 最小化依赖的场合

## 常用集成

一般直接使用 core 模块即可：

```xml
<dependency>
    <groupId>io.github.jukomu</groupId>
    <artifactId>jmcomic-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

这会传递引入 `jmcomic-api`、OkHttp、Gson、Jsoup、Commons Lang3。

## 控制传递依赖

如果想排除某些传递依赖（例如不需要 HTML 客户端，排除 Jsoup）：

```xml
<dependency>
    <groupId>io.github.jukomu</groupId>
    <artifactId>jmcomic-core</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

> 排除 Jsoup 后，HTML 客户端将不可用，但 API 客户端正常。

## Android 集成

见 [Android 集成](../android.md)。
