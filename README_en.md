<p align="center">
  <a href="./README.md">中文</a>
  <span>&nbsp;</span>
  <strong>English</strong>
</p>

# Java API For JMComic

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
<!-- ![Maven Central](https://img.shields.io/maven-central/v/io.github.jukomu/jmcomic-core) -->
<!-- ![CI](https://github.com/your-username/jmcomic-api-java/actions/workflows/maven.yml/badge.svg) -->

**A Java API library for fetching data from JMComic**

---

## ⚠️ Project Status: Under Development ⚠️

**Please note**: This project is currently in an active development and testing phase. The API may undergo incompatible changes in future versions.

---

## Reference Project

[![Readme Card](https://github-readme-stats.vercel.app/api/pin/?username=hect0x7&repo=JMComic-Crawler-Python)](https://github.com/hect0x7/JMComic-Crawler-Python)

---

## Features Overview

This project adopts a modular design, separating the **Public API** from the **Core Implementation**.

### `jmcomic-api` Module: Interfaces and Data Models

This module defines the public contracts of the library. It contains no third-party network library dependencies and can be integrated independently.

* **Domain Models**: Provides a set of immutable data objects based on Java `Record` to describe core entities such as `JmAlbum`, `JmPhoto`, `JmImage`, and `JmSearchPage`.
* **Client Interface (`JmClient`)**: Abstracts and unifies all business operations, including entity fetching (`getAlbum`, `getPhoto`), list querying (`search`, `getCategories`, `getFavorites`), user sessions (`login`), and downloading (`downloadAlbum`, etc.).
* **Strategy Interfaces**: Defines strategy interfaces such as `PhotoPathGenerator`, allowing callers to inject custom logic to control external interaction behaviors like file storage.
* **Configuration Model (`JmConfiguration`)**: Provides a `Builder` pattern for programmatic configuration of client behavior, supporting settings for proxies, timeouts, concurrency models, domain lists, and other parameters.

### `jmcomic-core` Module: Core Implementation

This module contains the specific implementation logic for all features, handling direct interactions with the JMComic server.

* **Client Implementations**:
    * **API Client**: Interacts with data by calling the JMComic mobile API (Recommended).
    * **HTML Client**: Interacts with data by requesting and parsing HTML pages from the JMComic website.
* **Network Processing**:
    * **Dynamic Domains**: Includes a mechanism to automatically fetch the latest API and HTML domains during client initialization.
    * **Request Retry**: Implements a stateful retry logic that polls a preset list of domains when a request fails.
* **Data Processing**:
    * **API Encryption/Decryption**: Automatically handles Header signature generation for API requests and AES decryption for response bodies.
    * **Image Reassembly**: Implements the de-obfuscation algorithm for JMComic's specific image slicing, restoring scrambled image blocks to the original image.
* **Concurrent Downloading**:
    * Provides advanced methods like `downloadAlbum` and `downloadPhoto`, featuring built-in concurrent download scheduling based on `ExecutorService` and `CompletableFuture`.
    * Batch download operations return a `DownloadResult` object, which contains detailed reports of successful and failed tasks.

### Future Plans

| Feature | Implementation Status |
|:---------------------------|:---------------------------------|
| **Cloudflare Bypass Solution** | 🔴 |
| **Android Support** | ✅ |

---

## Design Philosophy

The core of this project is a **data acquisition and management tool**, rather than a rigidly functioned downloader application. Its design is based on the following principles:

* **Inversion of Control**: The library itself is not responsible for specific thread scheduling and file I/O. Callers can fully control concurrency behavior and file storage logic by injecting strategy interfaces like `ExecutorService` and `PathGenerator`.
* **Process Transparency**: Every intermediate data model in the API call chain (`Album` → `Photo` → `Image`) is accessible. This allows developers to inspect, filter, or custom-process data at any stage of the download process.
* **Designed for Integration**: The goal of the library is to serve as a reliable underlying module that can be easily integrated into other large-scale applications, such as Android Apps, desktop tools, or backend services.

---

## Installation

This project has not yet been published to the Maven Central Repository. You can use it locally via the following methods:

1. Clone this repository:
   ```bash
   git clone https://github.com/jukomu/jmcomic-api-java.git
   cd jmcomic-api-java
   ```

2. Install it in your local Maven repository:
   ```bash
   mvn clean install
   ```

3. Add the dependency to your `pom.xml` file:
   ```xml
   <dependency>
       <groupId>io.github.jukomu</groupId>
       <artifactId>jmcomic-core</artifactId>
       <version>xxx</version> <!-- TO DO: Confirm current project version -->
   </dependency>
   ```

---

## Quick Start

Below is a complete example of a downloader:

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
 * @Description: Downloader sample
 * @Project: jmcomic-api-java
 * @Date: 2025/11/2
 */
public class DownloaderSample {
    private static AbstractJmClient client;

    public static void main(String[] args) {
        // Configure JmClient
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API) // Use API Client
                .build();
        client = JmComic.newApiClient(config);
        
        // Download an album with multiple photos (chapters)
        downloadAlbumWithAllPhotos("1064000");
        // Download an album with only one photo (chapter)
        downloadAlbumWithAllPhotos("540709");
        // Download a single photo (chapter)
        JmAlbum album = client.getAlbum("1064000");
        downloadPhoto(album.getPhotoMeta(2).getId());
        
        client.close();
    }

    private static void downloadAlbumWithAllPhotos(String albumId) {
        // Get album information
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

## Advanced Usage

### Custom Network Configuration

```java
import java.time.Duration;

JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.HTML) // Switch to HTML Client
        .proxy("127.0.0.1", 7890) // Set HTTP proxy
        .timeout(Duration.ofSeconds(60)) // Set network timeout to 60 seconds
        .retryTimes(10) // Set maximum retry attempts
        .downloadThreadPoolSize(12) // Set download thread pool size
        .cacheSize(100 * 1024 * 1024) // Set cache pool size in Bytes
        .concurrentPhotoDownloads(2) // Set number of concurrent photo (chapter) downloads
        .concurrentImageDownloads(15) // Set number of concurrent image downloads
        .build();
```

### Custom File Storage Path

You can implement custom path strategies:

```java
IAlbumPathGenerator generator = new IAlbumPathGenerator() {
    @Override
    public Path generatePath(JmAlbum album) {
        // Save the album to the "Title\Author\ID" directory
        return Path.of(FileUtils.sanitizeFilename(album.getTitle()),
                FileUtils.sanitizeFilename(album.getPrimaryAuthor()),
                album.getId());
    }
};
client.downloadAlbum(album, generator);
```

### Using an External Thread Pool

```java
// Create and manage your own thread pool
ExecutorService myExecutor = Executors.newFixedThreadPool(16);

try (AbstractJmClient client = JmComic.newApiClient(config)) {
    // ...
    // Inject the thread pool into the download method
    DownloadResult result = client.downloadAlbum(album, pathGenerator, myExecutor);
    // ...
} finally {
    // When the application exits, you are responsible for shutting down the thread pool
    myExecutor.shutdown();
    myExecutor.awaitTermination(1, TimeUnit.MINUTES);
}
```

---

## API vs HTML Client Comparison

| Feature | `API Client` (Recommended) | `HTML Client` |
|:---------------|:------------------|:---------------------------|
| **Stability** | ✅ High (Based on official API) | ⚠️ Medium (Prone to web page updates) |
| **Performance** | ✅ High | ⚠️ Medium (Requires parsing large amounts of HTML) |
| **Cloudflare** | 🟢 **Hardly affected** | 🔴 **May be blocked** (No built-in bypass in current version) |
| **Data Integrity** | 🟢 Most data is complete | ✅ Very complete (All page-visible info) |
| **Usage Scenarios** | Most regular data fetching and downloading | Fetching specific info not in the API, or as a fallback |

### Note on Cloudflare (`HTML Client`)

The JMComic website uses Cloudflare for security protection. `HTML Client` requests may be intercepted by Cloudflare's human verification (CAPTCHA), causing requests to fail.

**The current version (`jmcomic-api-java`) does not yet feature a built-in mechanism to automatically bypass Cloudflare.**

---

## Contributing

Contributions of any kind are welcome! If you find a BUG or have new feature suggestions, please feel free to submit an [Issue](https://github.com/jukomu/jmcomic-api-java/issues).

If you want to contribute code, please Fork this project first, make modifications on your branch, and then submit a Pull Request.

---

## License

This project is open-sourced under the [MIT License](LICENSE).
