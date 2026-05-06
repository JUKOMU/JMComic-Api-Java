# JmDownloadClient 接口

`JmDownloadClient` 定义了下载操作。`AbstractJmClient` 同时实现了 `JmClient` 和 `JmDownloadClient`，所以无需单独获取。

## 链式下载

```java
client.download(album)
        .withPath(path)
        .withProgress(callback)
        .withExecutor(executor)
        .execute();
```

## 直接下载方法

### 图片下载

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `downloadImage(JmImage)` | `void` | 下载图片到默认路径 |
| `downloadImage(JmImage, Path)` | `void` | 下载图片到指定路径 |
| `downloadImage(String, Path)` | `void` | 按 URL 下载图片 |

### 章节下载

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `downloadPhoto(JmPhoto)` | `DownloadResult` | 下载章节到默认路径 |
| `downloadPhoto(JmPhoto, Path)` | `DownloadResult` | 下载章节到指定路径 |
| `downloadPhoto(JmPhoto, IPhotoPathGenerator)` | `DownloadResult` | 使用路径生成器 |
| `downloadPhoto(JmPhoto, Path, ExecutorService)` | `DownloadResult` | 指定线程池 |
| `downloadPhoto(JmPhoto, IPhotoPathGenerator, ExecutorService)` | `DownloadResult` | 生成器+线程池 |
| `downloadPhoto(JmPhoto, IDownloadPathGenerator, ExecutorService)` | `DownloadResult` | 完整路径生成器 |

### 本子下载

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `downloadAlbum(JmAlbum)` | `DownloadResult` | 下载本子到默认路径 |
| `downloadAlbum(JmAlbum, Path)` | `DownloadResult` | 指定路径 |
| `downloadAlbum(JmAlbum, IAlbumPathGenerator)` | `DownloadResult` | 使用路径生成器 |
| `downloadAlbum(JmAlbum, Path, ExecutorService)` | `DownloadResult` | 指定线程池 |
| `downloadAlbum(JmAlbum, IAlbumPathGenerator, ExecutorService)` | `DownloadResult` | 生成器+线程池 |
| `downloadAlbum(JmAlbum, IDownloadPathGenerator, ExecutorService)` | `DownloadResult` | 完整路径生成器 |

## 下载任务系统

通过 `createDownloadTask` 创建任务，由 `downloadManager()` 提交和控制：

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `createDownloadTask(JmAlbum, Path)` | `BaseDownloadTask` | 创建本子下载任务（含章节/图片子任务树） |
| `createDownloadTask(JmPhoto, Path)` | `BaseDownloadTask` | 创建章节下载任务（含图片子任务） |
| `createDownloadTask(JmImage, Path)` | `BaseDownloadTask` | 创建单图下载任务 |
| `downloadManager()` | `IDownloadManager` | 获取任务管理器 |

```java
BaseDownloadTask task = client.createDownloadTask(album, path);
client.downloadManager().submit(task);
```

详细用法见 [下载任务系统](../../advanced/download-task-system.md)。

## 生命周期

`JmDownloadClient` 继承 `AutoCloseable`，使用完毕后需关闭：

```java
try (AbstractJmClient client = JmComic.newApiClient(config)) {
    client.downloadAlbum(album);
} // 自动关闭
```
