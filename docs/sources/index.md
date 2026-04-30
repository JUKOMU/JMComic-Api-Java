# JMComic API for Java

> 一个用于获取 JMComic（禁漫天堂）数据的 Java API 库

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen.svg)](https://github.com/JUKOMU/JMComic-Api-Java)

---

## 项目简介

本项目封装了一套可用于访问禁漫天堂数据的 Java API，采用**模块化设计**，将公共接口与核心实现分离。

你可以通过简单的几行 Java 代码，实现获取禁漫数据、下载本子等功能。

## 模块架构

```
jmcomic-api          → 公共接口与数据模型（零第三方依赖，可独立集成）
jmcomic-core         → 核心实现（API/HTML 双客户端，加解密，并发下载）
jmcomic-sample       → 使用示例
jmcomic-android-support → Android 平台适配
```

## 功能概览

| 分类 | 功能 |
|------|------|
| **漫画** | 本子详情、章节阅读、搜索、分类排行、分类列表 |
| **下载** | 并发下载、链式 API、进度回调、自定义路径/线程池 |
| **评论** | 评论列表、发表/回复（支持漫画/小说/博客） |
| **收藏** | 收藏夹管理、标签管理、收藏状态切换 |
| **用户** | 登录/登出、个人资料、每日签到 |
| **小说** | 列表、详情、章节阅读、搜索、评论/收藏 |
| **创作者** | 作者列表、作品浏览、作品详情 |
| **发现** | 热门标签、最新上架、随机推荐、每周必看 |
| **通知** | 通知列表、已读标记、连载追踪 |

## 快速开始

```xml
<dependency>
    <groupId>io.github.jukomu</groupId>
    <artifactId>jmcomic-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.API)
        .build();

try (AbstractJmClient client = JmComic.newApiClient(config)) {
    // 下载本子
    client.download(client.getAlbum("1064000"))
            .withProgress(p -> System.out.printf("进度: %d/%d%n",
                    p.completedImages(), p.totalImages()))
            .execute();
}
```

## 参考项目

本项目参考了 [JMComic-Crawler-Python](https://github.com/hect0x7/JMComic-Crawler-Python)，感谢原作者的贡献。
