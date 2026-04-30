# 下载

库内置了并发下载能力，支持本子级和章节级的下载操作。

## 下载本子

```java
JmAlbum album = client.getAlbum("1064000");
DownloadResult result = client.downloadAlbum(album);
```

## 下载章节

```java
JmAlbum album = client.getAlbum("540709");
JmPhoto photo = client.getPhoto(album.getPhotoMeta(0).getId());
DownloadResult result = client.downloadPhoto(photo);
```

## 链式下载 API

通过链式调用设置进度回调、自定义路径等：

```java
DownloadResult result = client.download(album)
        .withPath(Path.of("downloads", album.getId()))
        .withProgress(p -> System.out.printf(
                "进度: %d/%d 图片, %d/%d 章节%n",
                p.completedImages(), p.totalImages(),
                p.completedPhotos(), p.totalPhotos()))
        .execute();
```

可用方法：

| 方法 | 说明 |
|------|------|
| `withPath(Path)` | 指定下载目录 |
| `withProgress(Consumer<DownloadProgress>)` | 注册进度回调 |
| `withExecutor(ExecutorService)` | 指定线程池 |
| `execute()` | 执行下载，返回 `DownloadResult` |

## 下载结果

`DownloadResult` 包含详情：

```java
if (result.isAllSuccess()) {
    System.out.println("全部成功，共 " + result.getSuccessfulFiles().size() + " 张");
} else {
    result.getFailedTasks().forEach((image, error) ->
            System.err.println("失败: " + image.getTag() + " - " + error.getMessage()));
}
```

## 下载单张图片

```java
byte[] bytes = client.fetchImageBytes(image);
// 或直接保存到文件
client.downloadImage(image, Path.of("output.jpg"));
```

## 并发控制

通过 `JmConfiguration` 控制：

```java
new JmConfiguration.Builder()
        .downloadThreadPoolSize(12)   // 下载线程池大小
        .build();
```

设为 `-1` 使用默认值（CPU 核心数）。设为 `1` 即串行下载。

---

更多进阶用法见 [自定义下载路径](../advanced/custom-path.md)、[自定义线程池](../advanced/custom-executor.md)、[进度回调](../advanced/progress-callback.md)。
