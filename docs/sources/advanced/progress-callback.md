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
| `completedImages()` | `int` | 已完成图片数 |
| `totalImages()` | `int` | 总图片数 |
| `completedPhotos()` | `int` | 已完成章节数 |
| `totalPhotos()` | `int` | 总章节数 |
| `albumTitle()` | `String` | 本子标题 |
| `photoTitle()` | `String` | 当前章节标题 |

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
