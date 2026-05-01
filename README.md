<p align="center">
  <a href="./README_en.md">English</a>
  <span>&nbsp;</span>
  <strong>中文</strong>
</p>

# Java API For JMComic (禁漫天堂)

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen.svg)

**一个用于获取JMComic(禁漫天堂)数据的Java API库**

---

## 参考项目

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=hect0x7&repo=JMComic-Crawler-Python)](https://github.com/hect0x7/JMComic-Crawler-Python)

---

## 功能概述

### 功能分类

**漫画**
* `本子详情 — 获取包含章节元数据、标签、作者等完整信息`
* `章节阅读 — 获取章节内所有图片及下载URL`
* `搜索本子 — 支持关键词、分类、排序、时间范围等多维度筛选`
* `分类排行 — 按分类获取排行列表`
* `分类列表 — 获取分类树及标签区块`

**下载**
* `并发下载 — 内置线程池调度，支持章节/本子并行下载`
* `链式API — client.download(album).withProgress(cb).withPath(path).execute()`
* `进度回调 — 注册进度回调，实时获取图片/章节完成进度`
* `自定义路径 — 支持三层粒度的路径生成器`
* `自定义线程池 — 注入外部线程池，完全掌控并发模型`

**用户**
* `登录/登出 — 用户名密码登录获取用户信息`
* `个人资料 — 查看/编辑用户资料，修改昵称等字段`

**评论**
* `评论列表 — 支持漫画、小说、小说章节、博客、用户等多实体类型`
* `发表/回复 — 支持漫画评论、博客评论、小说评论的发表与回复`

**收藏**
* `收藏夹 — 获取收藏列表，按文件夹/页码筛选`
* `收藏管理 — 切换收藏状态，收藏夹增/删/改/移`
* `标签管理 — 获取/添加/删除收藏标签`

**小说**
* `小说列表 — 按排序条件获取`
* `小说详情 — 获取章节元数据、关联信息`
* `章节阅读 — 获取章节正文`
* `小说搜索 — 关键词搜索小说`
* `小说评论/收藏 — 点赞、收藏、发表/回复评论`

**创作者**
* `作者列表 — 分页浏览创作者`
* `作品浏览 — 按语言、来源筛选作品`
* `作品详情 — 获取作品信息与详情`

**签到**
* `签到状态 — 获取当日签到进度及活动信息`
* `执行签到 — 完成每日签到`
* `签到历史 — 查询历史签到记录`

**通知与追踪**
* `通知列表 — 获取系统通知`
* `已读/未读 — 标记通知状态`
* `连载追踪 — 获取追踪列表，设置/查看追踪状态`

**发现**
* `热门标签 — 获取热门搜索关键词`
* `最新上架 — 分页获取最新本子`
* `随机推荐 — 获取推荐列表`
* `每周必看 — 获取每周期数列表及详情`
* `首页推广 — 获取Banner内容`

**其他**
* `浏览历史 — 获取/删除观看历史`
* `任务系统 — 获取任务列表`

本项目采用模块化设计，将 **公共接口(API)** 与 **核心实现(Core)** 分离。

### `jmcomic-api` 模块: 接口与数据模型

此模块定义了库的公共契约，不包含第三方网络库依赖，可独立集成。

* **领域模型**: 提供一套基于Java `Record` 的不可变数据对象，涵盖漫画 (`JmAlbum`, `JmPhoto`, `JmImage`)、小说 (`JmNovelDetail`, `JmNovelChapter`)、创作者 (`JmCreatorMeta`, `JmCreatorWorkInfo`)、评论 (`JmComment`, `JmCommentList`)、通知 (`JmNotification`)、签到 (`JmDailyCheckInStatus`) 等核心实体。
* **客户端接口 (`JmClient`)**: 抽象并统一了所有业务操作，包括实体获取、搜索、用户会话、评论互动、收藏管理、签到、通知、浏览发现等。
* **子系统接口**:
  * `JmNovelClient` — 小说子系统（列表、详情、章节阅读、评论、收藏）
  * `JmCreatorClient` — 创作者子系统（作者列表、作品浏览、作品详情）
  * `JmDownloadClient` — 下载子系统（支持链式API、进度回调、路径和线程池注入）
* **策略接口**: 定义了 `IAlbumPathGenerator`、`IPhotoPathGenerator` 和 `IDownloadPathGenerator` 等策略接口，允许调用者注入自定义逻辑来控制文件存储等外部交互行为。
* **配置模型 (`JmConfiguration`)**: 提供 `Builder` 模式用于程序化配置客户端行为，支持代理、超时、并发、域名探活、图片超时等参数设置。

### `jmcomic-core` 模块: 核心实现

此模块包含了所有功能的具体实现逻辑，处理与JMComic服务端的直接交互。

* **客户端实现**:
  * **API客户端**: 通过调用JMComic移动端API进行数据交互（推荐）。
  * **HTML客户端**: 通过请求和解析JMComic网站的HTML页面进行数据交互。
* **网络处理**:
  * **动态域名**: 包含在客户端初始化时自动获取最新API及HTML域名的机制。
  * **请求重试**: 实现了一套有状态的重试逻辑，当请求失败时能够在预设的域名列表中进行轮询。
  * **域名后台探活**: 定期探测域名可用性，自动切换。
* **数据处理**:
  * **API加解密**: 自动完成API请求的Header签名生成和响应体的AES解密。
  * **图片重组**: 实现了对JMComic特定图片切割的反混淆算法，将分块图片还原为原始图片。
* **并发下载**:
  * 提供了 `downloadAlbum` 和 `downloadPhoto` 等下载方法，内置基于 `ExecutorService` 和 `CompletableFuture` 的并发调度能力。
  * 新增 **链式下载API** (`client.download(album).withProgress(...).execute()`)，支持实时进度回调。
  * 批量下载操作返回 `DownloadResult` 对象，包含成功与失败任务的详细报告。


### 未来规划

| 功能 | 实现情况 |
|:---------------------------|:---------------------------------|
| **Cloudflare 解决方案** | 🔴 |
| **Android 支持** | ✅ |

---

## 设计哲学

本项目的核心是一个**数据获取与管理工具**，而非一个功能固化的下载应用。其设计基于以下原则：

* **控制权移交**: 库本身不负责具体的线程调度和文件I/O。调用者可以通过注入 `ExecutorService` 和 `PathGenerator` 等策略接口，完全掌控并发行为和文件存储逻辑。
* **过程透明化**: API调用链 (`Album` → `Photo` → `Image`) 中的每一个中间数据模型都是可访问的。这允许开发者在下载过程的任何阶段进行检查、过滤或自定义处理。
* **为集成而设计**: 库的目标是作为一个可靠的底层模块，被轻松地集成到其他大型应用中，如Android App、桌面工具或后端服务。

---

## 安装 (Installation)

* 安装 jmcomic-api
   ```xml
    <dependency>
        <groupId>io.github.jukomu</groupId>
        <artifactId>jmcomic-api</artifactId>
        <version>1.0.0</version>
    </dependency>
   ```
* 安装 jmcomic-core
   ```xml
    <dependency>
        <groupId>io.github.jukomu</groupId>
        <artifactId>jmcomic-core</artifactId>
        <version>1.0.0</version>
    </dependency>
   ```
* Android平台额外安装 jmcomic-android-support
   ```xml
    <dependency>
        <groupId>io.github.jukomu</groupId>
        <artifactId>jmcomic-android-support</artifactId>
        <version>1.0.0</version>
    </dependency>
   ```

---

## 快速上手 (Quick Start)

### 下载漫画

```java
import io.github.jukomu.jmcomic.api.client.DownloadProgress;
import io.github.jukomu.jmcomic.api.client.DownloadResult;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

public class QuickStart {
    public static void main(String[] args) {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();

        try (AbstractJmClient client = JmComic.newApiClient(config)) {

            // 下载整个本子（含进度回调）
            JmAlbum album = client.getAlbum("1064000");
            DownloadResult result = client.download(album)
                    .withProgress(p -> System.out.printf(
                            "进度: 图片 %d/%d, 章节 %d/%d%n",
                            p.completedImages(), p.totalImages(),
                            p.completedPhotos(), p.totalPhotos()))
                    .execute();

            if (result.isAllSuccess()) {
                System.out.println("下载完成! 共 " + result.getSuccessfulFiles().size() + " 张");
            } else {
                result.getFailedTasks().forEach((img, err) ->
                        System.err.println("失败: " + img.getTag() + " - " + err.getMessage()));
            }
        }
    }
}
```

### 获取数据

```java
try (AbstractJmClient client = JmComic.newApiClient(config)) {
    // 获取本子详情
    JmAlbum album = client.getAlbum("540709");
    System.out.println("标题: " + album.title() + ", 作者: " + album.authors());

    // 搜索漫画
    JmSearchPage result = client.search(
        new SearchQuery.Builder().text("NTR").page(1).build());
    result.content().forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));

    // 获取热门标签
    List<String> hotTags = client.getHotTags();
    System.out.println("热门: " + hotTags);
}
```

---

## 进阶用法 (Advanced Usage)

### 配置项一览

```java
JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.HTML)           // 客户端类型
        .proxy("127.0.0.1", 7890)              // HTTP代理
        .timeout(Duration.ofSeconds(60))        // 请求超时
        .imageTimeout(Duration.ofSeconds(120))  // 图片下载超时
        .retryTimes(10)                         // 重试次数
        .downloadThreadPoolSize(12)             // 下载线程池大小
        .cacheSize(100 * 1024 * 1024)           // 缓存大小 (Byte)
        .closeTimeoutMs(30_000)                 // 关闭超时 (ms)
        .domainProbeIntervalMs(600_000)         // 域名探活间隔 (ms)
        .domainProbeTimeoutMs(3000)             // 域名探活超时 (ms)
        .build();
```

> `concurrentPhotoDownloads` 和 `concurrentImageDownloads` 已废弃，由统一的 `downloadThreadPoolSize` 控制并发。

### 自定义下载路径

提供三种粒度的路径策略：

```java
// 方式1: 本子级路径生成器
IAlbumPathGenerator albumPath = album ->
    Path.of("downloads", album.getId());

// 方式2: 章节级路径生成器
IPhotoPathGenerator photoPath = photo ->
    Path.of(String.format("%03d", photo.getSortOrder()));

// 方式3: 完整路径生成器（一次性控制 album/photo/image 三层）
IDownloadPathGenerator totalPath = (album, photo, image) ->
    Path.of(album.getId(),
            String.format("%03d", photo.getSortOrder()),
            image.getFilename());

// 使用
client.downloadAlbum(album, totalPath, executor);
```

### 使用外部线程池

```java
ExecutorService myExecutor = Executors.newFixedThreadPool(16);
try (AbstractJmClient client = JmComic.newApiClient(config)) {
    DownloadResult result = client.download(album)
            .withExecutor(myExecutor)
            .withProgress(p -> System.out.printf("%d/%d%n",
                    p.completedImages(), p.totalImages()))
            .execute();
} finally {
    myExecutor.shutdown();
    myExecutor.awaitTermination(1, TimeUnit.MINUTES);
}
```

### 评论系统

```java
// 获取本子评论
JmCommentList comments = client.getComments(
    ForumQuery.album("1173049").mode(ForumMode.ALL).page(1).build());
comments.getList().forEach(c ->
    System.out.println(c.getUsername() + ": " + c.getContent()));

// 发表评论（需先登录）
JmComment comment = client.postComment("1173049", "好看!");

// 回复评论
JmComment reply = client.replyToComment("1173049", "+1", parentCommentId);

// 获取小说评论
client.getComments(ForumQuery.novel("12345").page(1).build());

// 获取小说某章评论
client.getComments(ForumQuery.novelChapter("12345", "67890").page(1).build());
```

### 收藏管理

```java
// 获取收藏夹
JmFavoritePage favPage = client.getFavorites(
    new FavoriteQuery.Builder().folderId(0).page(1).build());

// 切换收藏状态
client.toggleAlbumFavorite("1064000", "0");

// 管理收藏文件夹
client.manageFavoriteFolder(FavoriteFolderType.ADD, "0", "新文件夹", null);
client.manageFavoriteFolder(FavoriteFolderType.DEL, "folderId", null, null);

// 收藏标签
List<JmTagFavorite> tags = client.getTagsFavorite();
client.addFavoriteTags(List.of("标签1", "标签2"));
client.removeFavoriteTags(List.of("标签1"));
```

### 用户与签到

```java
// 登录
JmUserInfo user = client.login("username", "password");

// 获取/编辑资料
JmUserProfile profile = client.getUserProfile(user.getUid());
client.editUserProfile(user.getUid(), Map.of("nickname", "新昵称"));

// 每日签到
JmDailyCheckInStatus status = client.getDailyCheckInStatus(user.getUid());
client.doDailyCheckin(user.getUid(), status.dailyId());

// 签到历史筛选
List options = client.getDailyCheckInOptions(user.getUid());
List records = client.filterDailyCheckInList("2025");
```

### 小说子系统

```java
// 获取小说列表
JmNovelPage novels = client.getNovelList("mr", 1);

// 获取小说详情
JmNovelDetail detail = client.getNovelDetail("novelId");

// 获取章节内容
JmNovelChapter chapter = client.getNovelChapter("chapterId", "0");

// 搜索小说
JmNovelPage searchResult = client.searchNovels("关键词");

// 小说评论
client.postNovelComment("novelId", "精彩!", null);
client.replyToNovelComment("novelId", "同感", parentCommentId, null);
```

### 创作者子系统

```java
// 获取作者列表
JmCreatorPage authors = client.getCreatorAuthors(1, "");

// 获取作品列表
JmCreatorWorkPage works = client.getCreatorWorks(1, "", "", "");

// 获取指定作者的作品
JmCreatorAuthorWorksPage authorWorks =
    client.getCreatorAuthorWorks("creatorId", "", "", 1);

// 获取作品信息与详情
JmCreatorWorkInfo info = client.getCreatorWorkInfo("workId");
JmCreatorWorkDetail workDetail = client.getCreatorWorkDetail("workId");
```

### 发现与浏览

```java
// 最新上架
JmSearchPage latest = client.getLatest(1);

// 随机推荐
List<JmAlbumMeta> random = client.getRandomRecommend();

// 每周必看
JmWeeklyPicksList picks = client.getWeeklyPicksList();
JmWeeklyPicksDetail pickDetail = client.getWeeklyPicksDetail(categoryId);

// 连载追踪
JmTrackingPage tracking = client.getAlbumTrackingList(1);
client.setAlbumSertracking("albumId");
boolean isTracking = client.getAlbumSertracking("albumId");

// 浏览历史
List<JmAlbumMeta> history = client.getWatchHistory(1);
client.deleteWatchHistory("albumId");
```

---

## API vs HTML 客户端对比

| 特性 | `API Client` (推荐) | `HTML Client` |
|:---------------|:------------------|:---------------------------|
| **稳定性** | ✅ 高 (基于官方API) | ⚠️ 中 (易受网页改版影响) |
| **性能** | ✅ 高 | ⚠️ 中 (需解析大量HTML) |
| **Cloudflare** | 🟢 **几乎不受影响** | 🔴 **可能被拦截** (无内置绕过方案) |
| **数据完整性** | 🟢 大部分数据齐全 | ✅ 非常完整 (所有页面可见信息) |
| **使用场景** | 绝大多数常规的数据获取、下载 | 获取API没有的特定信息，或作为备用方案 |

### 关于 Cloudflare 的说明 (`HTML Client`)

JMComic 网站使用 Cloudflare 作为其安全防护，`HTML Client` 请求可能会被 Cloudflare 的人机验证所拦截，导致请求失败。

**当前版本尚未内置自动绕过 Cloudflare 的机制。**

---

## 如何贡献 (Contributing)

欢迎任何形式的贡献！如果您发现了BUG或有新的功能建议，请随时提交 [Issues](https://github.com/jukomu/jmcomic-api-java/issues)。

如果您想贡献代码，请先 Fork 本项目，在您的分支上进行修改，然后提交 Pull Request。

---

## 许可证 (License)

本项目基于 [MIT License](LICENSE) 开源。
