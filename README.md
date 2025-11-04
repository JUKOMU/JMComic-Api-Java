# Java API For JMComic (禁漫天堂)

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
<!-- ![Maven Central](https://img.shields.io/maven-central/v/io.github.jukomu/jmcomic-core) -->
<!-- ![CI](https://github.com/your-username/jmcomic-api-java/actions/workflows/maven.yml/badge.svg) -->

**一个用于获取JMComic(禁漫天堂)数据的Java API库**

---

## ⚠️ 项目状态：开发阶段 ⚠️

**请注意**: 本项目目前正处于积极的开发和测试阶段。API 可能会在未来的版本中发生不兼容的变更。

**Please note**: This project is currently in an active development and testing phase. The API may undergo incompatible
changes in future versions.

---

## 参考项目

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=hect0x7&repo=JMComic-Crawler-Python)](https://github.com/hect0x7/JMComic-Crawler-Python)

---

## 功能概述

本项目采用模块化设计，将 **公共接口(API)** 与 **核心实现(Core)** 分离。

### `jmcomic-api` 模块: 接口与数据模型

此模块定义了库的公共契约，不包含第三方网络库依赖，可独立集成。

* **领域模型**: 提供一套基于Java `Record` 的不可变数据对象，用于描述 `JmAlbum`, `JmPhoto`, `JmImage`, `JmSearchPage`
  等核心实体。
* **客户端接口 (`JmClient`)**: 抽象并统一了所有业务操作，包括实体获取 (`getAlbum`, `getPhoto`)、列表查询 (`search`,
  `getCategories`, `getFavorites`)、用户会话 (`login`) 和下载 (`downloadAlbum` 等)。
* **策略接口**: 定义了如 `PhotoPathGenerator` 等策略接口，允许调用者注入自定义逻辑来控制文件存储等外部交互行为。
* **配置模型 (`JmConfiguration`)**: 提供 `Builder` 模式用于程序化配置客户端行为，支持代理、超时、并发模型、域名列表等参数的设置。

### `jmcomic-core` 模块: 核心实现

此模块包含了所有功能的具体实现逻辑，处理与JMComic服务端的直接交互。

* **客户端实现**:
    * **API客户端**: 通过调用JMComic移动端API进行数据交互（推荐）。
    * **HTML客户端**: 通过请求和解析JMComic网站的HTML页面进行数据交互。
* **网络处理**:
    * **动态域名**: 包含在客户端初始化时自动获取最新API及HTML域名的机制。
    * **请求重试**: 实现了一套有状态的重试逻辑，当请求失败时，能够在预设的域名列表中进行轮询。
* **数据处理**:
    * **API加解密**: 自动完成API请求的Header签名生成和响应体的AES解密。
    * **图片重组**: 实现了对JMComic特定图片切割的反混淆算法，将分块图片还原为原始图片。
* **并发下载**:
    * 提供了 `downloadAlbum` 和 `downloadPhoto` 等高级方法，内置了基于 `ExecutorService` 和 `CompletableFuture`
      的并发下载调度能力。
    * 批量下载操作返回 `DownloadResult` 对象，其中包含了成功与失败任务的详细报告。

### 未来规划

| 功能 | 实现情况 |
|:---------------------------|:---------------------------------|
| **Cloudflare 解决方案** | 🔴 |
| **Android 支持** | ✅ |

---

## 设计哲学

本项目的核心是一个**数据获取与管理工具**，而非一个功能固化的下载应用。其设计基于以下原则：

* **控制权移交**: 库本身不负责具体的线程调度和文件I/O。调用者可以通过注入 `ExecutorService` 和 `PathGenerator`
  等策略接口，完全掌控并发行为和文件存储逻辑。
* **过程透明化**: API调用链 (`Album` → `Photo` → `Image`) 中的每一个中间数据模型都是可访问的。这允许开发者在下载过程的任何阶段进行检查、过滤或自定义处理。
* **为集成而设计**: 库的目标是作为一个可靠的底层模块，被轻松地集成到其他大型应用中，如Android App、桌面工具或后端服务。

---

## 安装 (Installation)

本项目尚未发布到Maven中央仓库。您可以通过以下方式在本地使用：

1. 克隆本仓库:
   ```bash
   git clone https://github.com/jukomu/jmcomic-api-java.git
   cd jmcomic-api-java
   ```

2. 在本地Maven仓库中安装:
   ```bash
   mvn clean install
   ```

3. 在您的 `pom.xml` 文件中添加依赖:
   ```xml
   <dependency>
       <groupId>io.github.jukomu</groupId>
       <artifactId>jmcomic-core</artifactId>
       <version>xxx</version> <!-- TO DO: 确认当前项目版本 -->
   </dependency>
   ```

---

## 快速上手 (Quick Start)

以下是一个下载器的完整示例：

```java
package io.github.jukomu.jmcomic.sample.downloader;

import io.github.jukomu.jmcomic.api.client.DownloadResult;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

/**
 * @author JUKOMU
 * @Description: 下载器示例
 * @Project: jmcomic-api-java
 * @Date: 2025/11/2
 */
public class DownloaderSample {
    private static AbstractJmClient client;

    public static void main(String[] args) {
        // 配置 JmClient
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API) // 使用API客户端
                .build();
        client = JmComic.newApiClient(config);
        // 下载含多个章节的本子
        downloadAlbumWithAllPhotos("1064000");
        // 下载只有一个章节的本子
        downloadAlbumWithAllPhotos("540709");
        // 下载一个章节
        JmAlbum album = client.getAlbum("1064000");
        downloadPhoto(album.getPhotoMeta(2).getId());
        client.close();
    }

    private static void downloadAlbumWithAllPhotos(String albumId) {
        // 获取本子的信息
        JmAlbum album = client.getAlbum(albumId);
        System.out.println("Downloading album: " + album.getId() + " ...");
        DownloadResult downloadAlbumResult = client.downloadAlbum(album);
        if (downloadAlbumResult.isAllSuccess()) {
            System.out.println("Download complete! All " + downloadAlbumResult.getSuccessfulFiles().size() + " images saved.");
        } else {
            System.out.println("Download partially complete.");
            System.out.println("Success: " + downloadAlbumResult.getSuccessfulFiles().size());
            System.out.println("Failed: " + downloadAlbumResult.getFailedTasks().size());
            downloadAlbumResult.getFailedTasks().forEach((image, error) ->
                    System.err.println("  - Failed to download " + image.getTag() + ": " + error.getMessage())
            );
        }
    }

    private static void downloadPhoto(String photoId) {
        JmPhoto photo = client.getPhoto(photoId);
        System.out.println("Downloading photo: " + photo.getId() + " ...");
        DownloadResult downloadAlbumResult = client.downloadPhoto(photo);
        if (downloadAlbumResult.isAllSuccess()) {
            System.out.println("Download complete! All " + downloadAlbumResult.getSuccessfulFiles().size() + " images saved.");
        } else {
            System.out.println("Download partially complete.");
            System.out.println("Success: " + downloadAlbumResult.getSuccessfulFiles().size());
            System.out.println("Failed: " + downloadAlbumResult.getFailedTasks().size());
            downloadAlbumResult.getFailedTasks().forEach((image, error) ->
                    System.err.println("  - Failed to download " + image.getTag() + ": " + error.getMessage())
            );
        }
    }
}
```

---

## 进阶用法 (Advanced Usage)

### 自定义网络配置

```java
import java.time.Duration;

JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.HTML) // 切换为HTML客户端
        .proxy("127.0.0.1", 7890) // 设置HTTP代理
        .timeout(Duration.ofSeconds(60)) // 设置网络超时为60秒
        .retryTimes(10) // 设置最大重试次数
        .downloadThreadPoolSize(12) // 设置下载线程池大小
        .cacheSize(100 * 1024 * 1024) // 设置缓存池大小,单位: Byte
        .concurrentPhotoDownloads(2) // 设置同时下载的章节数
        .concurrentImageDownloads(15) // 设置同时下载的图片数
        .build();
```

### 自定义文件存储路径

可以自定义实现路径策略

```java
IAlbumPathGenerator generator = new IAlbumPathGenerator() {
    @Override
    public Path generatePath(JmAlbum album) {
        // 将本子存储到"标题\作者\ID"的目录
        return Path.of(FileUtils.sanitizeFilename(album.getTitle()),
                FileUtils.sanitizeFilename(album.getPrimaryAuthor()),
                album.getId());
    }
};
client.downloadAlbum(album, generator);
```

### 使用外部线程池

```java
// 创建并管理你自己的线程池
ExecutorService myExecutor = Executors.newFixedThreadPool(16);

try (AbstractJmClient client = JmComic.newApiClient(config)) {
    // ...
    // 将线程池注入到下载方法中
    DownloadResult result = client.downloadAlbum(album, pathGenerator, myExecutor);
    // ...
} finally {
    // 在应用退出时，由您自己负责关闭线程池
    myExecutor.shutdown();
    myExecutor.awaitTermination(1, TimeUnit.MINUTES);
}

```

---

## API vs HTML 客户端对比

| 特性             | `API Client` (推荐) | `HTML Client`              |
|:---------------|:------------------|:---------------------------|
| **稳定性**        | ✅ 高 (基于官方API)     | ⚠️ 中 (易受网页改版影响)            |
| **性能**         | ✅ 高               | ⚠️ 中 (需要解析大量HTML)          |
| **Cloudflare** | 🟢 **几乎不受影响**     | 🔴 **可能被拦截** (当前版本无内置绕过方案) |
| **数据完整性**      | 🟢 大部分数据齐全        | ✅ 非常完整 (所有页面可见信息)          |
| **使用场景**       | 绝大多数常规的数据获取、下载    | 获取API没有的特定信息，或作为备用方案       |

### 关于 Cloudflare 的说明 (`HTML Client`)

JMComic 网站使用 Cloudflare 作为其安全防护， `HTML Client` 请求可能会被 Cloudflare 的人机验证所拦截，导致请求失败。

**当前版本 (`jmcomic-api-java`) 尚未内置自动绕过 Cloudflare 的机制。**

---

## 如何贡献 (Contributing)

欢迎任何形式的贡献！如果您发现了BUG或有新的功能建议，请随时提交 [Issues](https://github.com/jukomu/jmcomic-api-java/issues)。

如果您想贡献代码，请先 Fork 本项目，在您的分支上进行修改，然后提交 Pull Request。

---

## 许可证 (License)

本项目基于 [MIT License](LICENSE) 开源。
