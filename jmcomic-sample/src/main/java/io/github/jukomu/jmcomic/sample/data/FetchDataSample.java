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
 * 演示如何获取 JMComic 各种核心数据实体的示例代码。
 */
public class FetchDataSample {

    public static void main(String[] args) {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();

        try (AbstractJmClient client = JmComic.newApiClient(config)) {

            System.out.println("--- 获取本子详情 ---");
            fetchAlbumDetails(client, "540709");

            System.out.println("\n--- 获取章节详情 ---");
            fetchPhotoDetails(client, "1064001");

            System.out.println("\n--- 搜索本子 ---");
            searchAlbums(client, "NTR");

        }
    }

    private static void fetchAlbumDetails(AbstractJmClient client, String albumId) {
        System.out.println("Fetching album with ID: " + albumId);
        JmAlbum album = client.getAlbum(albumId);
        System.out.println("Success! Title: " + album.title());
        System.out.println("Authors: " + album.authors());
        System.out.println("Photo count: " + album.photoMetas().size());
    }

    private static void fetchPhotoDetails(AbstractJmClient client, String photoId) {
        System.out.println("Fetching photo with ID: " + photoId);
        JmPhoto photo = client.getPhoto(photoId);
        System.out.println("Success! Title: " + photo.title());
        System.out.println("Image count: " + photo.images().size());
        System.out.println("First image URL: " + photo.images().get(0).getDownloadUrl());
    }

    private static void searchAlbums(AbstractJmClient client, String keyword) {
        System.out.println("Searching for keyword: " + keyword);
        SearchQuery query = new SearchQuery.Builder().text(keyword).page(1).build();
        JmSearchPage searchPage = client.search(query);
        System.out.println("Success! Total pages found: " + searchPage.totalPages());
        System.out.println("Results on first page:");
        searchPage.content().forEach(albumMeta -> {
            System.out.printf("  - [ID: %s] %s%n", albumMeta.id(), albumMeta.title());
        });
    }
}
