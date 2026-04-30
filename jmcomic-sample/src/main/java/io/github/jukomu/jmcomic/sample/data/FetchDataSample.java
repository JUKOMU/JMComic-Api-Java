package io.github.jukomu.jmcomic.sample.data;

import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.enums.ForumMode;
import io.github.jukomu.jmcomic.api.enums.TimeOption;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

import java.util.List;

/**
 * 演示如何获取 JMComic 各种核心数据实体的示例代码。
 *
 * <p>涵盖漫画、评论、收藏、发现、追踪等场景。
 */
public class FetchDataSample {

    public static void main(String[] args) {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();

        try (AbstractJmClient client = JmComic.newApiClient(config)) {

            // == 漫画 ==
            System.out.println("--- 获取本子详情 ---");
            fetchAlbumDetails(client, "540709");

            System.out.println("\n--- 获取章节详情 ---");
            fetchPhotoDetails(client, "1064001");

            System.out.println("\n--- 搜索本子 ---");
            searchAlbums(client, "NTR");

            System.out.println("\n--- 分类排行 ---");
            searchCategories(client);

            System.out.println("\n--- 分类列表 ---");
            fetchCategoriesList(client);

            // == 评论 ==
            System.out.println("\n--- 本子评论 ---");
            fetchAlbumComments(client, "1173049");

            // == 收藏 ==
            System.out.println("\n--- 收藏夹 ---");
            fetchFavorites(client);

            // == 发现 ==
            System.out.println("\n--- 热门标签 ---");
            fetchHotTags(client);

            System.out.println("\n--- 最新上架 ---");
            fetchLatest(client);

            System.out.println("\n--- 随机推荐 ---");
            fetchRandomRecommend(client);

            System.out.println("\n--- 每周必看 ---");
            fetchWeeklyPicks(client);

            // == 连载追踪 ==
            System.out.println("\n--- 追踪列表 ---");
            fetchTracking(client);
        }
    }

    // ========== 漫画 ==========

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
        searchPage.content().forEach(albumMeta ->
                System.out.printf("  - [ID: %s] %s%n", albumMeta.id(), albumMeta.title()));
    }

    private static void searchCategories(AbstractJmClient client) {
        System.out.println("Searching categories (time=MONTH, category=ALL) ...");
        SearchQuery query = new SearchQuery.Builder()
                .time(TimeOption.MONTH)
                .page(1)
                .build();
        JmSearchPage searchPage = client.getCategories(query);
        System.out.println("Success! Total pages found: " + searchPage.totalPages());
        if (!searchPage.content().isEmpty()) {
            JmAlbumMeta first = searchPage.content().get(0);
            System.out.printf("  First: [ID: %s] %s%n", first.id(), first.title());
        }
    }

    private static void fetchCategoriesList(AbstractJmClient client) {
        System.out.println("Fetching categories list ...");
        JmCategoryList categoriesList = client.getCategoriesList();
        List<JmCategoryBlock> blocks = categoriesList.blocks();
        System.out.println("Success! Blocks count: " + blocks.size());
        blocks.forEach(block ->
                System.out.printf("  Block: %s, tags: %d%n", block.title(), block.content().size()));
        // 打印第一个区块的前 5 个分类
        if (!blocks.isEmpty()) {
            JmCategoryBlock firstBlock = blocks.get(0);
            List<JmCategoryListItem> categories = categoriesList.categories();
            categories.stream().limit(5).forEach(item ->
                    System.out.printf("    - %s (id=%s)%n", item.name(), item.id()));
        }
    }

    // ========== 评论 ==========

    private static void fetchAlbumComments(AbstractJmClient client, String albumId) {
        System.out.println("Fetching comments for album: " + albumId);
        JmCommentList comments = client.getComments(
                ForumQuery.album(albumId).mode(ForumMode.ALL).page(1).build());
        System.out.println("Success! Total comments: " + comments.getTotal());
        comments.getList().stream().limit(5).forEach(c ->
                System.out.printf("  [%s]: %s%n", c.getUsername(), c.getContent()));
    }

    // ========== 收藏 ==========

    private static void fetchFavorites(AbstractJmClient client) {
        System.out.println("Fetching favorites (folderId=0, page=1) ...");
        JmFavoritePage favPage = client.getFavorites(
                new FavoriteQuery.Builder().folderId(0).page(1).build());
        System.out.println("Success! Total: " + favPage.getTotalItems());
        favPage.getContent().forEach(meta ->
                System.out.printf("  - [ID: %s] %s%n", meta.id(), meta.title()));
    }

    // ========== 发现 ==========

    private static void fetchHotTags(AbstractJmClient client) {
        System.out.println("Fetching hot tags ...");
        List<String> hotTags = client.getHotTags();
        System.out.println("Success! Tags count: " + hotTags.size());
        System.out.println("  Tags: " + hotTags);
    }

    private static void fetchLatest(AbstractJmClient client) {
        System.out.println("Fetching latest (page=1) ...");
        JmSearchPage latest = client.getLatest(1);
        System.out.println("Success! Total pages: " + latest.totalPages());
        if (!latest.content().isEmpty()) {
            JmAlbumMeta first = latest.content().get(0);
            System.out.printf("  First: [ID: %s] %s%n", first.id(), first.title());
        }
    }

    private static void fetchRandomRecommend(AbstractJmClient client) {
        System.out.println("Fetching random recommendations ...");
        List<JmAlbumMeta> random = client.getRandomRecommend();
        System.out.println("Success! Count: " + random.size());
        random.stream().limit(5).forEach(meta ->
                System.out.printf("  - [ID: %s] %s%n", meta.id(), meta.title()));
    }

    private static void fetchWeeklyPicks(AbstractJmClient client) {
        System.out.println("Fetching weekly picks ...");
        JmWeeklyPicksList picks = client.getWeeklyPicksList();
        System.out.println("Success! Categories: " + picks.getCategories().size());
        picks.getCategories().stream().limit(3).forEach(cat ->
                System.out.printf("  - %s (id=%s)%n", cat.getTitle(), cat.getId()));
    }

    // ========== 连载追踪 ==========

    private static void fetchTracking(AbstractJmClient client) {
        System.out.println("Fetching tracking list (page=1) ...");
        JmTrackingPage tracking = client.getAlbumTrackingList(1);
        System.out.println("Success! Total: " + tracking.getTotalCnt());
        tracking.getItem().stream().limit(5).forEach(item ->
                System.out.printf("  - [ID: %s] %s (updated=%s)%n",
                        item.getId(), item.getName(), item.getUpdateAt()));
    }
}
