# 下载第一个本子

## 最简示例

以下代码下载本子 `JM1064000` 的全部章节图片：

```java
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

public class QuickStart {
    public static void main(String[] args) {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();

        try (AbstractJmClient client = JmComic.newApiClient(config)) {
            client.downloadAlbum(client.getAlbum("1064000"));
        }
    }
}
```

## 带进度的下载

```java
try (AbstractJmClient client = JmComic.newApiClient(config)) {
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
```

## 仅下载某个章节

```java
JmAlbum album = client.getAlbum("540709");
JmPhoto photo = client.getPhoto(album.getPhotoMeta(1).getId());

client.download(photo)
        .withProgress(p -> System.out.printf("章节进度: %d/%d%n",
                p.completedImages(), p.totalImages()))
        .execute();
```

## 关键点

- client 实现了 `AutoCloseable`，**必须用 try-with-resources 或手动 close()**
- 默认下载到当前工作目录，可通过配置修改（见 [自定义下载路径](../advanced/custom-path.md)）
- 图片默认保存为 jpg 格式
