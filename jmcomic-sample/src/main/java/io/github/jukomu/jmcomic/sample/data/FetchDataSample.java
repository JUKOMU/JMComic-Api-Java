package io.github.jukomu.jmcomic.sample.data;

import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.api.model.JmSearchPage;
import io.github.jukomu.jmcomic.api.model.SearchQuery;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

/**
 * @author JUKOMU
 * @Description: 演示如何获取JMComic各种核心数据实体的示例代码
 * @Project: jmcomic-api-java
 * @Date: 2025/11/3
 */
public class FetchDataSample {
    private static AbstractJmClient client;

    static {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();
        client = JmComic.newApiClient(config);
    }

    public static void main(String[] args) {
        try {
            System.out.println("--- Running Fetch Album Details Sample ---");
            fetchAlbumDetails("540709");

            System.out.println("\n--- Running Fetch Photo Details Sample ---");
            fetchPhotoDetails("1064001");

            System.out.println("\n--- Running Search Sample ---");
            searchAlbums("NTR");

            // ... 调用其他示例方法

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 在所有示例运行完毕后关闭客户端
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * 示例 1: 获取单个本子 (JmAlbum) 的详细信息
     */
    public static void fetchAlbumDetails(String albumId) {
        System.out.println("Fetching album with ID: " + albumId);
        JmAlbum album = client.getAlbum(albumId);
        System.out.println("Success! Title: " + album.title());
        System.out.println("Authors: " + album.authors());
        System.out.println("Photo count: " + album.photoMetas().size());
    }

    /**
     * 示例 2: 获取单个章节 (JmPhoto) 的详细信息
     */
    public static void fetchPhotoDetails(String photoId) {
        System.out.println("Fetching photo with ID: " + photoId);
        JmPhoto photo = client.getPhoto(photoId);
        System.out.println("Success! Title: " + photo.title());
        System.out.println("Image count: " + photo.images().size());
        System.out.println("First image URL: " + photo.images().get(0).getDownloadUrl());
    }

    /**
     * 示例 3: 执行一次搜索 (JmSearchPage)
     */
    public static void searchAlbums(String keyword) {
        System.out.println("Searching for keyword: " + keyword);
        SearchQuery query = new SearchQuery.Builder().text(keyword).page(1).build();
        JmSearchPage searchPage = client.search(query);
        System.out.println("Success! Total pages found: " + searchPage.totalPages());
        System.out.println("Results on first page:");
        searchPage.content().forEach(albumMeta -> {
            System.out.printf("  - [ID: %s] %s%n", albumMeta.id(), albumMeta.title());
        });
    }

    /**
     * 示例 4: 获取收藏夹 (需要登录)
     */
    public static void fetchFavorites() {
        // ... (登录逻辑)
        // JmFavoritePage favPage = client.getFavorites(1);
        // ...
    }
}
