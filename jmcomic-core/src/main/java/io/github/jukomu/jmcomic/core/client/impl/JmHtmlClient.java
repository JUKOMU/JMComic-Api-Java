package io.github.jukomu.jmcomic.core.client.impl;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.Category;
import io.github.jukomu.jmcomic.api.exception.ApiResponseException;
import io.github.jukomu.jmcomic.api.exception.NetworkException;
import io.github.jukomu.jmcomic.api.exception.ParseResponseException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.net.model.JmHtmlResponse;
import io.github.jukomu.jmcomic.core.net.model.JmResponse;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import io.github.jukomu.jmcomic.core.parser.HtmlParser;
import okhttp3.*;

import java.io.IOException;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author JUKOMU
 * @Description: JmClient 接口的HTML实现
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmHtmlClient extends AbstractJmClient {

    public JmHtmlClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        super(config, httpClient, cookieManager, domainManager);
    }

    @Override
    protected void initialize() {
        updateDomains();
    }

    @Override
    protected void updateDomains() {
        try {
            List<String> htmlDomainAll = getHtmlDomainAll();
            if (!htmlDomainAll.isEmpty()) {
                domainManager.updateDomains(htmlDomainAll);
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            List<String> htmlDomainAllViaGithub = getHtmlDomainAllViaGithub();
            if (!htmlDomainAllViaGithub.isEmpty()) {
                domainManager.updateDomains(htmlDomainAllViaGithub);
                return;
            }
        } catch (Exception ignored) {
        }
    }

    // == 核心数据获取层实现 ==

    @Override
    public JmAlbum getAlbum(String albumId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("album")
                .addPathSegment(albumId)
                .build();

        JmHtmlResponse jmHtmlResponse = executeGetRequest(url);
        return HtmlParser.parseAlbum(jmHtmlResponse.getHtml());

    }

    @Override
    public JmPhoto getPhoto(String photoId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("photo")
                .addPathSegment(photoId)
                .build();

        JmHtmlResponse jmHtmlResponse = executeGetRequest(url);
        return HtmlParser.parsePhoto(jmHtmlResponse.getHtml());

    }

    @Override
    public byte[] fetchImageBytes(JmImage image) {
        Request request = new Request.Builder()
                .url(image.getDownloadUrl())
                .get()
                .build();

        try {
            JmResponse response = executeRequest(request);
            return response.getContent();
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to fetch image: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to fetch image due to I/O error", e);
        }
    }

    @Override
    public JmSearchPage search(SearchQuery query) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment("search")
                .addPathSegment("photos");

        // 构建网页端特有的分类路径
        buildCategoryPath(urlBuilder, query.getCategory(), query.getSubCategory().orElse(null));

        urlBuilder.addQueryParameter("search_query", query.getSearchQuery())
                .addQueryParameter("page", String.valueOf(query.getPage()))
                .addQueryParameter("o", query.getOrderBy().getValue())
                .addQueryParameter("t", query.getTimeOption().getValue());

        try {
            JmHtmlResponse jmHtmlResponse = executeGetRequest(urlBuilder.build());
            // 检查返回的页面是否是详情页而不是列表页
            if (isAlbumDetailPage(jmHtmlResponse.getHtml())) {
                JmAlbum album = HtmlParser.parseAlbum(jmHtmlResponse.getHtml());
                // 将单个 Album 包装成 SearchPage
                JmAlbumMeta meta = new JmAlbumMeta(album.id(), album.title(), album.authors(), album.tags());
                return new JmSearchPage(1, 1, 1, List.of(meta));
            } else {
                return HtmlParser.parseSearchPage(jmHtmlResponse.getHtml(), query.getPage());
            }
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to search " + query + ": " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to search " + query + " due to I/O error", e);
        }
    }

    @Override
    public JmSearchPage getCategories(SearchQuery query) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment("albums");

        // 构建网页端特有的分类路径
        buildCategoryPath(urlBuilder, query.getCategory(), query.getSubCategory().orElse(null));

        urlBuilder.addQueryParameter("page", String.valueOf(query.getPage()))
                .addQueryParameter("o", query.getOrderBy().getValue())
                .addQueryParameter("t", query.getTimeOption().getValue());

        try {
            JmHtmlResponse jmHtmlResponse = executeGetRequest(urlBuilder.build());
            return HtmlParser.parseSearchPage(jmHtmlResponse.getHtml(), query.getPage());
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to search " + query + ": " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to search " + query + " due to I/O error", e);
        }
    }

    @Override
    public JmFavoritePage getFavorites(int page) {
        String username = getLoggedInUserName();

        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("user")
                .addPathSegment(username)
                .addPathSegment("favorite")
                .addPathSegment("albums")
                .addQueryParameter("page", String.valueOf(page))
                .build();

        try {
            JmHtmlResponse jmHtmlResponse = executeGetRequest(url);
            return HtmlParser.parseFavoritePage(jmHtmlResponse.getHtml(), page);
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to get favorites: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to get favorites due to I/O error", e);
        }
    }

    // == 会话管理层实现 ==

    @Override
    public JmUserInfo login(String username, String password) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("login")
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("id_remember", "on")
                .add("login_remember", "on")
                .add("submit_login", "")
                .build();

        // 禁止重定向
        try {
            JmHtmlResponse jmHtmlResponse = executePostRequest(url, formBody, false);
            super.cacheUsername(username);
            return JmUserInfo.partial(username);
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Login failed: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Login failed due to I/O error", e);
        }
    }

    @Override
    public JmComment postComment(String entityId, String commentText, String status) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("ajax")
                .addPathSegment("album_comment")
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("video_id", entityId)
                .add("comment", commentText);
        // 发表新评论的参数
        formBuilder.add("originator", "")
                .add("status", "true");

        try {
            JmHtmlResponse jmHtmlResponse = executePostRequest(url, formBuilder.build());
            String json = jmHtmlResponse.getHtml();
            // 解析评论成功后的返回JSON
            // {"err":false,"cid":"336109","message":"\u8a55\u8ad6\u5df2\u767c\u4f48"}
            JSONObject jsonObject = JSONObject.parseObject(json);
            if (jsonObject.getBooleanValue("err", true)) {
                throw new ApiResponseException("Failed to post comment :" + jsonObject.getString("message"));
            }
            /*
              API响应中只包含评论ID (cid)，不包含新评论的完整信息。
              因此，返回的 JmComment 对象是部分填充的，
              其中 userId, username, postDate 等字段将为空或默认值。
             */
            return new JmComment(jsonObject.getString("cid"), "", getLoggedInUserName(), commentText, "");
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Post comment failed" + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Post comment request failed", e);
        }
    }

    @Override
    public JmComment replyToComment(String entityId, String commentText, String parentCommentId) {
        Objects.requireNonNull(parentCommentId, "Parent comment ID cannot be null for a reply.");
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("ajax")
                .addPathSegment("album_comment")
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("video_id", entityId)
                .add("comment", commentText);
        // 回复评论的参数
        formBuilder.add("comment_id", parentCommentId)
                .add("is_reply", "1")
                .add("forum_subject", "1");

        try {
            JmHtmlResponse jmHtmlResponse = executePostRequest(url, formBuilder.build());
            String json = jmHtmlResponse.getHtml();
            // 解析评论成功后的返回JSON
            // {"err":false,"cid":"336109","message":"\u8a55\u8ad6\u5df2\u767c\u4f48"}
            JSONObject jsonObject = JSONObject.parseObject(json);
            if (jsonObject.getBooleanValue("err", true)) {
                throw new ApiResponseException("Failed to post comment :" + jsonObject.getString("message"));
            }
            /*
              API响应中只包含评论ID (cid)，不包含新评论的完整信息。
              因此，返回的 JmComment 对象是部分填充的，
              其中 userId, username, postDate 等字段将为空或默认值。
             */
            return new JmComment(jsonObject.getString("cid"), "", getLoggedInUserName(), commentText, "");
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Post comment failed" + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Post comment request failed", e);
        }
    }

    @Override
    public void addAlbumToFavorite(String albumId, String folderId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("ajax")
                .addPathSegment("favorite_album")
                .addQueryParameter("album_id", albumId)
                .addQueryParameter("fid", folderId == null ? "0" : folderId)
                .build();

        try {
            JmHtmlResponse jmHtmlResponse = executeGetRequest(url);
            // 解析返回的简单JSON，检查状态
            try {
                JSONObject jsonObject = JSONObject.parseObject(jmHtmlResponse.getHtml());
                if (jsonObject.getIntValue("status", 0) != 1) {
                    String message = jsonObject.getString("msg");
                    // 已经在收藏夹中
                    throw new ApiResponseException("Failed to add to favorites: " + message);
                }
            } catch (JSONException e) {
                throw new ParseResponseException("Failed to parse 'add to favorite' response", e);
            }
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to add to favorites: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to add to favorites", e);
        }
    }

    // == 辅助方法 ==

    /**
     * 获取一个可用的禁漫网址
     *
     * @return 可用的禁漫网址
     */
    public String getHtmlUrl() {
        HttpUrl url = HttpUrl.parse(JmConstants.JM_REDIRECT_URL);
        try {
            JmHtmlResponse jmHtmlResponse = executeGetRequest(url, false);
            if (jmHtmlResponse.isRedirect()) {
                String location = String.valueOf(jmHtmlResponse.getHeaders().get("Location"));
                if (location != null && !location.isEmpty()) {
                    return location;
                }
            }
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to get html url: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to get html url", e);
        }
        throw new ApiResponseException("Failed to get html url");
    }

    /**
     * 获取一个可用的禁漫域名
     *
     * @return 可用的禁漫域名
     */
    public String getHtmlDomain() {
        return HtmlParser.parseUrlDomain(getHtmlUrl());
    }

    /**
     * 获取所有禁漫网页域名。
     *
     * @return 禁漫网页域名列表
     */
    public List<String> getHtmlDomainAll() {
        try {
            JmHtmlResponse jmHtmlResponse = executeGetRequest(HttpUrl.parse(JmConstants.JM_REDIRECT_URL));
            return HtmlParser.parseJmPubHtml(jmHtmlResponse.getHtml());
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to get html domains: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to get html domains", e);
        }
    }

    /**
     * 通过GitHub页面获取所有禁漫网页域名。
     *
     * @return 禁漫网页域名Set。
     */
    public List<String> getHtmlDomainAllViaGithub() {
        String template = "https://jmcmomic.github.io/go/{}.html";
        int[] indexRange = new int[]{300, 309};
        Set<String> domainSet = ConcurrentHashMap.newKeySet();
        List<String> urlsToFetch = new ArrayList<>();
        for (int i = indexRange[0]; i <= indexRange[1]; i++) {
            urlsToFetch.add(String.format(template, i));
        }

        int poolSize = Math.min(urlsToFetch.size(), Runtime.getRuntime().availableProcessors() * 2);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        try {
            // 每个URL创建一个异步任务，并收集所有的 CompletableFuture
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String url : urlsToFetch) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    OkHttpClient client = this.httpClient;
                    client = client.newBuilder().followRedirects(false).build();
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        JmHtmlResponse jmHtmlResponse = new JmHtmlResponse(response);
                        jmHtmlResponse.requireSuccess();
                        for (String domain : HtmlParser.parseJmPubHtml(jmHtmlResponse.getHtml())) {
                            if (domain.startsWith("jm365")) {
                                continue;
                            }
                            domainSet.add(domain);
                        }
                    } catch (IOException e) {
                        throw new NetworkException("Request failed due to I/O error", e);
                    }

                }, executor);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
            try {
                // 设置超时时间
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return domainSet.stream().toList();
    }

    private JmHtmlResponse executeGetRequest(HttpUrl url, boolean followRedirect) {
        OkHttpClient client = this.httpClient;
        if (!followRedirect) {
            client = client.newBuilder().followRedirects(false).build();
        }
        Request request = getGetRequestBuilder(url).build();
        try (Response response = client.newCall(request).execute()) {
            // 对于HTML客户端，非200的响应码通常意味着需要用户介入（如Cloudflare验证）
            // 或者资源不存在（会被重定向到错误页，但OkHttp默认不抛异常）
            // 具体的错误逻辑（如检查是否重定向到/error/album_missing）可以在这里添加
            // 或者依赖Parser在解析失败时抛出异常
            JmHtmlResponse jmHtmlResponse = new JmHtmlResponse(response);
            jmHtmlResponse.requireSuccess();
            return jmHtmlResponse;
        } catch (IOException e) {
            throw new NetworkException("Request failed due to I/O error", e);
        }
    }

    private JmHtmlResponse executeGetRequest(HttpUrl url) {
        return executeGetRequest(url, true); // 默认跟随重定向
    }

    private JmHtmlResponse executePostRequest(HttpUrl url, RequestBody requestBody, boolean followRedirect) {
        OkHttpClient client = this.httpClient;
        if (!followRedirect) {
            client = client.newBuilder().followRedirects(false).build();
        }
        Request request = getPostRequestBuilder(url, requestBody).build();
        try (Response response = client.newCall(request).execute()) {
            JmHtmlResponse jmHtmlResponse = new JmHtmlResponse(response);
            jmHtmlResponse.requireSuccess();
            return jmHtmlResponse;
        } catch (IOException e) {
            throw new NetworkException("Request failed due to I/O error", e);
        }
    }

    private JmHtmlResponse executePostRequest(HttpUrl url, RequestBody requestBody) {
        return executePostRequest(url, requestBody, true);
    }

    private void buildCategoryPath(HttpUrl.Builder builder, Category category, String subCategory) {
        if (category == null || category == Category.ALL) {
            return;
        }
        builder.addPathSegment(category.getValue());
        if (subCategory != null && !subCategory.isEmpty()) {
            builder.addPathSegment("sub").addPathSegment(subCategory);
        }
    }

    private boolean isAlbumDetailPage(String html) {
        // 通过检查页面中是否存在详情页特有的、唯一的元素来判断
        // 例如，ID为 "book-name" 的元素。
        return html.contains("id=\"book-name\"");
    }
}
