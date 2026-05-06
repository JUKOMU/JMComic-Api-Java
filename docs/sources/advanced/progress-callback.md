# 进度回调

下载过程中可注册进度回调，实时获取下载进度。

## 基本用法

```java
client.download(album)
        .withProgress(p -> System.out.printf(
                "进度: %d/%d 图片, %d/%d 章节%n",
                p.completedImages(), p.totalImages(),
                p.completedPhotos(), p.totalPhotos()))
        .execute();
```

## `DownloadProgress` 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `completedImages()` | `int` | 已完成的图片数（含成功/跳过/部分成功） |
| `FailedImages()` | `int` | 已结束但未完成的图片数（含失败/取消） |
| `totalImages()` | `int` | 总图片数 |
| `completedPhotos()` | `int` | 已完成的章节数（含成功/跳过/部分成功） |
| `FailedPhotos()` | `int` | 已结束但未完成的章节数（含失败/取消） |
| `totalPhotos()` | `int` | 总章节数 |
| `albumId()` | `String` | 本子ID |
| `albumTitle()` | `String` | 本子标题 |
| `photoId()` | `String` | 当前章节ID（章节级有效） |
| `photoTitle()` | `String` | 当前章节标题（章节级有效） |
| `isAlbumLevel()` | `boolean` | 是否为本子级下载 |
| `downloadedBytes()` | `long` | 已下载字节数（用于速度统计） |
| `createTimestamp()` | `String` | 进度快照创建时间戳 |

## 完整示例

```java
private static void downloadWithProgress(AbstractJmClient client, String albumId) {
    JmAlbum album = client.getAlbum(albumId);
    System.out.println("本子: " + album.getTitle());

    DownloadResult result = client.download(album)
            .withProgress(p -> {
                double pct = (double) p.completedImages() / p.totalImages();
                System.out.printf("[%.0f%%] 图片 %d/%d, 章节 %d/%d%n",
                        pct * 100,
                        p.completedImages(), p.totalImages(),
                        p.completedPhotos(), p.totalPhotos());
            })
            .execute();

    if (result.isAllSuccess()) {
        System.out.println("完成! 共 " + result.getSuccessfulFiles().size() + " 张");
    }
}
```
