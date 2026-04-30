<p align="center">
  <a href="./README.md">中文</a>
  <span>&nbsp;</span>
  <strong>English</strong>
</p>

# Java API For JMComic

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen.svg)

**A Java API library for fetching data from JMComic**

---

## Reference Project

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=hect0x7&repo=JMComic-Crawler-Python)](https://github.com/hect0x7/JMComic-Crawler-Python)

---

## Features Overview

This project adopts a modular design, separating the **Public API** from the **Core Implementation**.

### `jmcomic-api` Module: Interfaces and Data Models

This module defines the public contracts of the library. It contains no third-party network library dependencies and can be integrated independently.

* **Domain Models**: Provides a set of immutable data objects based on Java `Record`, covering comics (`JmAlbum`, `JmPhoto`, `JmImage`), novels (`JmNovelDetail`, `JmNovelChapter`), creators (`JmCreatorMeta`, `JmCreatorWorkInfo`), comments (`JmComment`, `JmCommentList`), notifications (`JmNotification`), check-in (`JmDailyCheckInStatus`), and more.
* **Client Interface (`JmClient`)**: Abstracts and unifies all business operations, including entity fetching, search, user sessions, comment interactions, favorite management, check-in, notifications, browsing & discovery, and more.
* **Subsystem Interfaces**:
  * `JmNovelClient` — Novel subsystem (listing, detail, chapter reading, comments, favorites)
  * `JmCreatorClient` — Creator subsystem (author listing, work browsing, work detail)
  * `JmDownloadClient` — Download subsystem (chain-style API, progress callbacks, path and thread pool injection)
* **Strategy Interfaces**: Defines `IAlbumPathGenerator`, `IPhotoPathGenerator`, and `IDownloadPathGenerator` strategy interfaces, allowing callers to inject custom logic for external interaction behaviors like file storage.
* **Configuration Model (`JmConfiguration`)**: Provides a `Builder` pattern for programmatic configuration, supporting proxies, timeouts, concurrency, domain probing, image timeout, and more.

### `jmcomic-core` Module: Core Implementation

This module contains the specific implementation logic for all features, handling direct interactions with the JMComic server.

* **Client Implementations**:
  * **API Client**: Interacts by calling the JMComic mobile API (Recommended).
  * **HTML Client**: Interacts by requesting and parsing HTML pages from the JMComic website.
* **Network Processing**:
  * **Dynamic Domains**: Automatically fetches the latest API and HTML domains during client initialization.
  * **Request Retry**: A stateful retry mechanism that polls a preset list of domains when a request fails.
  * **Background Domain Probing**: Periodically checks domain availability with automatic failover.
* **Data Processing**:
  * **API Encryption/Decryption**: Automatically handles Header signature generation for API requests and AES decryption for response bodies.
  * **Image Reassembly**: Implements the de-obfuscation algorithm for JMComic's image slicing, restoring scrambled blocks to the original image.
* **Concurrent Downloading**:
  * Provides `downloadAlbum` and `downloadPhoto` methods with built-in concurrent scheduling based on `ExecutorService` and `CompletableFuture`.
  * New **chain-style download API** (`client.download(album).withProgress(...).execute()`) with real-time progress callbacks.
  * Batch download operations return a `DownloadResult` object with detailed success/failure reports.

### Feature Categories

**Comics**
* Album details — complete information including chapter metadata, tags, authors
* Chapter reading — all images within a chapter with download URLs
* Search — multi-dimensional filtering by keyword, category, sort order, time range
* Category ranking — browse albums by category ranking
* Category list — category tree and tag blocks

**Download**
* Concurrent download — built-in thread pool scheduling for parallel chapter/album downloads
* Chain-style API — `client.download(album).withProgress(cb).withPath(path).execute()`
* Progress callback — real-time image/chapter completion status via progress callback
* Custom paths — three granularity levels of path generators
* Custom thread pool — inject external thread pool for full concurrency control

**User**
* Login/logout — username/password login to obtain user information
* Profile — view/edit user profile, update nickname and other fields

**Comments**
* Comment listing — supports multiple entity types: comic, novel, novel chapter, blog, user
* Post/reply — post and reply to comments on comics, blogs, and novels

**Favorites**
* Favorites list — browse favorites filtered by folder/page
* Folder management — toggle favorite status, create/delete/rename/move folders
* Tag management — get/add/remove favorite tags

**Novels**
* Novel list — browse with sort criteria
* Novel detail — chapter metadata and related info
* Chapter reading — chapter content
* Novel search — keyword search for novels
* Novel comments/favorites — like, favorite, post/reply to comments

**Creators**
* Author list — browse creators with pagination
* Work browsing — filter works by language and source
* Work detail — work information and detail

**Daily Check-in**
* Check-in status — daily progress and event information
* Perform check-in — complete daily check-in
* Check-in history — query historical check-in records

**Notifications & Tracking**
* Notification list — system notifications
* Read/unread — mark notification status
* Serial tracking — browse tracking list, set/check tracking status

**Discovery**
* Hot tags — hot search keywords
* Latest updates — paginated latest albums
* Random recommendations — recommendation list
* Weekly picks — weekly picks categories and details
* Promotions — banner content

**Other**
* Watch history — get/delete watch history
* Task system — fetch task list

### Future Plans

| Feature | Implementation Status |
|:---------------------------|:---------------------------------|
| **Cloudflare Bypass Solution** | 🔴 |
| **Android Support** | ✅ |

---

## Design Philosophy

The core of this project is a **data acquisition and management tool**, not a rigid downloader application. Its design is based on the following principles:

* **Inversion of Control**: The library itself is not responsible for specific thread scheduling and file I/O. Callers can fully control concurrency behavior and file storage logic by injecting strategy interfaces like `ExecutorService` and `PathGenerator`.
* **Process Transparency**: Every intermediate data model in the API call chain (`Album` → `Photo` → `Image`) is accessible. This allows developers to inspect, filter, or custom-process data at any stage.
* **Designed for Integration**: The goal of the library is to serve as a reliable underlying module that can be easily integrated into larger applications, such as Android Apps, desktop tools, or backend services.

---

## Installation

* Install jmcomic-api
   ```xml
    <dependency>
        <groupId>io.github.jukomu</groupId>
        <artifactId>jmcomic-api</artifactId>
        <version>1.0.0</version>
    </dependency>
   ```
* Install jmcomic-core
   ```xml
    <dependency>
        <groupId>io.github.jukomu</groupId>
        <artifactId>jmcomic-core</artifactId>
        <version>1.0.0</version>
    </dependency>
   ```
* (Android platform) Install jmcomic-android-support
   ```xml
    <dependency>
        <groupId>io.github.jukomu</groupId>
        <artifactId>jmcomic-android-support</artifactId>
        <version>1.0.0</version>
    </dependency>
   ```

---

## Quick Start

### Download Comics

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

            // Download album with progress callback
            JmAlbum album = client.getAlbum("1064000");
            DownloadResult result = client.download(album)
                    .withProgress(p -> System.out.printf(
                            "Progress: images %d/%d, chapters %d/%d%n",
                            p.completedImages(), p.totalImages(),
                            p.completedPhotos(), p.totalPhotos()))
                    .execute();

            if (result.isAllSuccess()) {
                System.out.println("Download complete! " + result.getSuccessfulFiles().size() + " images");
            } else {
                result.getFailedTasks().forEach((img, err) ->
                        System.err.println("Failed: " + img.getTag() + " - " + err.getMessage()));
            }
        }
    }
}
```

### Fetch Data

```java
try (AbstractJmClient client = JmComic.newApiClient(config)) {
    // Get album details
    JmAlbum album = client.getAlbum("540709");
    System.out.println("Title: " + album.title() + ", Authors: " + album.authors());

    // Search comics
    JmSearchPage result = client.search(
        new SearchQuery.Builder().text("NTR").page(1).build());
    result.content().forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));

    // Get hot tags
    List<String> hotTags = client.getHotTags();
    System.out.println("Hot tags: " + hotTags);
}
```

---

## Advanced Usage

### Configuration Reference

```java
JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.HTML)           // Client type
        .proxy("127.0.0.1", 7890)              // HTTP proxy
        .timeout(Duration.ofSeconds(60))        // Request timeout
        .imageTimeout(Duration.ofSeconds(120))  // Image download timeout
        .retryTimes(10)                         // Max retry attempts
        .downloadThreadPoolSize(12)             // Download thread pool size
        .cacheSize(100 * 1024 * 1024)           // Cache size (Bytes)
        .closeTimeoutMs(30_000)                 // Close timeout (ms)
        .domainProbeIntervalMs(600_000)         // Domain probe interval (ms)
        .domainProbeTimeoutMs(3000)             // Domain probe timeout (ms)
        .build();
```

> `concurrentPhotoDownloads` and `concurrentImageDownloads` are deprecated. Concurrency is now uniformly controlled by `downloadThreadPoolSize`.

### Custom Download Paths

Three levels of path strategy granularity:

```java
// Level 1: Album-level path generator
IAlbumPathGenerator albumPath = album ->
    Path.of("downloads", album.getId());

// Level 2: Photo-level path generator
IPhotoPathGenerator photoPath = photo ->
    Path.of(String.format("%03d", photo.getSortOrder()));

// Level 3: Complete path generator (controls album/photo/image all at once)
IDownloadPathGenerator totalPath = (album, photo, image) ->
    Path.of(album.getId(),
            String.format("%03d", photo.getSortOrder()),
            image.getFilename());

// Usage
client.downloadAlbum(album, totalPath, executor);
```

### External Thread Pool

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

### Comment System

```java
// Get album comments
JmCommentList comments = client.getComments(
    ForumQuery.album("1173049").mode(ForumMode.ALL).page(1).build());
comments.getList().forEach(c ->
    System.out.println(c.getUsername() + ": " + c.getContent()));

// Post a comment (login required)
JmComment comment = client.postComment("1173049", "Great!");

// Reply to a comment
JmComment reply = client.replyToComment("1173049", "+1", parentCommentId);

// Get novel comments
client.getComments(ForumQuery.novel("12345").page(1).build());

// Get novel chapter comments
client.getComments(ForumQuery.novelChapter("12345", "67890").page(1).build());
```

### Favorite Management

```java
// Get favorites
JmFavoritePage favPage = client.getFavorites(
    new FavoriteQuery.Builder().folderId(0).page(1).build());

// Toggle favorite status
client.toggleAlbumFavorite("1064000", "0");

// Manage favorite folders
client.manageFavoriteFolder(FavoriteFolderType.ADD, "0", "New Folder", null);
client.manageFavoriteFolder(FavoriteFolderType.DEL, "folderId", null, null);

// Favorite tags
List<JmTagFavorite> tags = client.getTagsFavorite();
client.addFavoriteTags(List.of("tag1", "tag2"));
client.removeFavoriteTags(List.of("tag1"));
```

### User & Daily Check-in

```java
// Login
JmUserInfo user = client.login("username", "password");

// View / edit profile
JmUserProfile profile = client.getUserProfile(user.getUid());
client.editUserProfile(user.getUid(), Map.of("nickname", "new_nick"));

// Daily check-in
JmDailyCheckInStatus status = client.getDailyCheckInStatus(user.getUid());
client.doDailyCheckin(user.getUid(), status.dailyId());

// Check-in history
List options = client.getDailyCheckInOptions(user.getUid());
List records = client.filterDailyCheckInList("2025");
```

### Novel Subsystem

```java
// Get novel list
JmNovelPage novels = client.getNovelList("mr", 1);

// Get novel detail
JmNovelDetail detail = client.getNovelDetail("novelId");

// Get chapter content
JmNovelChapter chapter = client.getNovelChapter("chapterId", "0");

// Search novels
JmNovelPage searchResult = client.searchNovels("keyword");

// Novel comments
client.postNovelComment("novelId", "Amazing!", null);
client.replyToNovelComment("novelId", "Agreed", parentCommentId, null);
```

### Creator Subsystem

```java
// Get authors list
JmCreatorPage authors = client.getCreatorAuthors(1, "");

// Get works list
JmCreatorWorkPage works = client.getCreatorWorks(1, "", "", "");

// Get works by a specific author
JmCreatorAuthorWorksPage authorWorks =
    client.getCreatorAuthorWorks("creatorId", "", "", 1);

// Get work info & detail
JmCreatorWorkInfo info = client.getCreatorWorkInfo("workId");
JmCreatorWorkDetail workDetail = client.getCreatorWorkDetail("workId");
```

### Discovery & Browsing

```java
// Latest updates
JmSearchPage latest = client.getLatest(1);

// Random recommendations
List<JmAlbumMeta> random = client.getRandomRecommend();

// Weekly picks
JmWeeklyPicksList picks = client.getWeeklyPicksList();
JmWeeklyPicksDetail pickDetail = client.getWeeklyPicksDetail(categoryId);

// Serial tracking
JmTrackingPage tracking = client.getAlbumTrackingList(1);
client.setAlbumSertracking("albumId");
boolean isTracking = client.getAlbumSertracking("albumId");

// Watch history
List<JmAlbumMeta> history = client.getWatchHistory(1);
client.deleteWatchHistory("albumId");
```

---

## API vs HTML Client Comparison

| Feature | `API Client` (Recommended) | `HTML Client` |
|:---------------|:------------------|:---------------------------|
| **Stability** | ✅ High (Based on official API) | ⚠️ Medium (Prone to web page updates) |
| **Performance** | ✅ High | ⚠️ Medium (Requires parsing large HTML) |
| **Cloudflare** | 🟢 **Hardly affected** | 🔴 **May be blocked** (No built-in bypass) |
| **Data Integrity** | 🟢 Most data is complete | ✅ Very complete (All page-visible info) |
| **Usage Scenarios** | Most regular data fetching and downloading | Specific info not in the API, or as fallback |

### Note on Cloudflare (`HTML Client`)

The JMComic website uses Cloudflare for security protection. `HTML Client` requests may be intercepted by Cloudflare's human verification (CAPTCHA), causing requests to fail.

**The current version does not feature a built-in mechanism to automatically bypass Cloudflare.**

---

## Contributing

Contributions of any kind are welcome! If you find a BUG or have new feature suggestions, please feel free to submit an [Issue](https://github.com/jukomu/jmcomic-api-java/issues).

If you want to contribute code, please Fork this project first, make modifications on your branch, and then submit a Pull Request.

---

## License

This project is open-sourced under the [MIT License](LICENSE).
