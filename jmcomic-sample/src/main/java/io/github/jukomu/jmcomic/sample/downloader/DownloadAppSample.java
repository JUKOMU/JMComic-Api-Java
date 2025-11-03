package io.github.jukomu.jmcomic.sample.downloader;

import io.github.jukomu.jmcomic.api.client.DownloadResult;
import io.github.jukomu.jmcomic.api.client.JmClient;
import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.core.JmComic;

/**
 * @author JUKOMU
 * @Description: 下载器示例
 * @Project: jmcomic-api-java
 * @Date: 2025/11/2
 */
public class DownloadAppSample {
    private static JmClient client;

    public static void main(String[] args) {
        // 配置 JmClient
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API) // 使用API客户端
                .build();
        client = JmComic.newClient(config);
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