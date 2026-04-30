package io.github.jukomu.jmcomic.core.client.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jukomu.jmcomic.api.client.JmCreatorClient;
import io.github.jukomu.jmcomic.api.client.JmNovelClient;
import io.github.jukomu.jmcomic.api.enums.Category;
import io.github.jukomu.jmcomic.api.enums.FavoriteFolderType;
import io.github.jukomu.jmcomic.api.enums.SubCategory;
import io.github.jukomu.jmcomic.api.enums.VoteType;
import io.github.jukomu.jmcomic.api.exception.*;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.net.model.JmHtmlResponse;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import io.github.jukomu.jmcomic.core.parser.HtmlParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieManager;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author JUKOMU
 * @Description: JmClient 接口的 HTML 实现。
 * <p>
 * 模拟浏览器访问禁漫网页端（HTML）来获取数据，不需要 App 签名认证。
 * 功能相对较少，主要支持本子/章节获取、搜索、收藏、评论、登录等基础功能。
 * 不支持的操作会抛 UnsupportedOperationException，建议改用 JmApiClient。
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmHtmlClient extends AbstractJmClient implements JmNovelClient, JmCreatorClient {

    private static final Logger logger = LoggerFactory.getLogger(JmHtmlClient.class);

    public JmHtmlClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        super(config, httpClient, cookieManager, domainManager);
    }

    @Override
    protected void initialize() {
//        updateDomains();
    }

    @Override
    protected void updateDomains() {
        logger.info("开始获取最新域名列表");
        String oldDomains = domainManager.getDomains().toString();

        /*
         * HTML 客户端没有专用域名服务器，需要从其他地方捞域名。
         * 首选从 JmPub 页面获取，不行就从 GitHub Pages 拿。
         */
        try {
            logger.info("尝试从 JmPub 页面获取域名...");
            List<String> newDomains = getHtmlDomainAll();
            if (!newDomains.isEmpty()) {
                domainManager.updateDomains(newDomains);
                logger.info("获取最新域名列表成功 (JmPub): {} -> {}", oldDomains, newDomains);
                return;
            }
            logger.warn("从 JmPub 获取的域名列表为空。");
        } catch (Exception e) {
            logger.warn("从 JmPub 获取域名列表失败: {}", e.getMessage());
        }

        try {
            logger.info("尝试从 Github 页面获取域名...");
            List<String> newDomains = getHtmlDomainAllViaGithub();
            if (!newDomains.isEmpty()) {
                domainManager.updateDomains(newDomains);
                logger.info("获取最新域名列表成功 (Github): {} -> {}", oldDomains, newDomains);
                return;
            }
            logger.warn("从 Github 获取的域名列表为空。");
        } catch (Exception e) {
            logger.warn("从 Github 获取域名列表失败: {}", e.getMessage());
        }

        logger.error("获取最新域名列表失败: 所有方法均无法获取有效域名。");
    }

    // == 核心数据获取层实现 ==

    @Override
    public JmAlbum getAlbum(String albumId) throws AlbumNotFoundException {
        JmAlbum cachedJmAlbum = getCachedJmAlbum(albumId);
        if (cachedJmAlbum != null) {
            return cachedJmAlbum;
        }
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("album")
                .addPathSegment(albumId)
                .build();
        JmHtmlResponse jmHtmlResponse;
        try {
            jmHtmlResponse = executeGetRequest(url);
        } catch (ResourceNotFoundException e) {
            throw new AlbumNotFoundException(albumId, e);
        }
        JmAlbum jmAlbum = HtmlParser.parseAlbum(jmHtmlResponse.getHtml());
        cacheJmAlbum(jmAlbum);
        return jmAlbum;

    }

    @Override
    public JmAlbum getComicRead(String comicId) {
        throw new UnsupportedOperationException("Getting comic read data via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmPhoto getPhoto(String photoId) throws PhotoNotFoundException {
        JmPhoto cachedJmPhoto = getCachedJmPhoto(photoId);
        if (cachedJmPhoto != null) {
            return cachedJmPhoto;
        }
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("photo")
                .addPathSegment(photoId)
                .build();
        JmHtmlResponse jmHtmlResponse;
        try {
            jmHtmlResponse = executeGetRequest(url);
        } catch (ResourceNotFoundException e) {
            throw new PhotoNotFoundException(photoId, e);
        }
        JmPhoto jmPhoto = HtmlParser.parsePhoto(jmHtmlResponse.getHtml());
        cacheJmPhoto(jmPhoto);
        return jmPhoto;

    }

    @Override
    public JmSearchPage search(SearchQuery query) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment("search")
                .addPathSegment("photos");

        // 构建网页端特有的分类路径
        buildCategoryPath(urlBuilder, query.getCategory(), query.getSubCategory());

        urlBuilder.addQueryParameter("main_tag", String.valueOf(query.getMainTag().getValue()))
                .addQueryParameter("search_query", query.getSearchQuery())
                .addQueryParameter("page", String.valueOf(query.getPage()))
                .addQueryParameter("o", query.getOrderBy().getValue())
                .addQueryParameter("t", query.getTimeOption().getValue());

        JmHtmlResponse jmHtmlResponse = executeGetRequest(urlBuilder.build());
        /*
         * 搜索条件精确匹配到唯一本子时，禁漫会 302 到详情页而不是返回列表，
         * 这时需把详情页包装成单条结果的 SearchPage。
         */
        if (isAlbumDetailPage(jmHtmlResponse.getHtml())) {
            JmAlbum album = HtmlParser.parseAlbum(jmHtmlResponse.getHtml());
            // 将单个 Album 包装成 SearchPage
            JmAlbumMeta meta = new JmAlbumMeta(album.id(), album.title(), album.authors(), album.tags());
            return new JmSearchPage(1, 1, 1, List.of(meta));
        } else {
            return HtmlParser.parseSearchPage(jmHtmlResponse.getHtml(), query.getPage());
        }
    }

    @Override
    public JmSearchPage getCategories(SearchQuery query) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment("albums");

        // 构建网页端特有的分类路径
        buildCategoryPath(urlBuilder, query.getCategory(), query.getSubCategory());

        urlBuilder.addQueryParameter("page", String.valueOf(query.getPage()))
                .addQueryParameter("o", query.getOrderBy().getValue())
                .addQueryParameter("t", query.getTimeOption().getValue());

        JmHtmlResponse jmHtmlResponse = executeGetRequest(urlBuilder.build());
        return HtmlParser.parseSearchPage(jmHtmlResponse.getHtml(), query.getPage());
    }

    @Override
    public JmFavoritePage getFavorites(FavoriteQuery query) {
        JmFavoritePage cachedJmFavoritePage = getCachedJmFavoritePage(query);
        if (cachedJmFavoritePage != null) {
            return cachedJmFavoritePage;
        }
        int folderId = query.getFolderId();
        int page = query.getPage();
        String username = getLoggedInUserName();

        HttpUrl.Builder url = newHttpUrlBuilder()
                .addPathSegment("user")
                .addPathSegment(username)
                .addPathSegment("favorite")
                .addPathSegment("albums")
                .addQueryParameter("page", String.valueOf(page));
        if (folderId != 0) {
            url.addQueryParameter("folder", String.valueOf(folderId));
        }

        JmHtmlResponse jmHtmlResponse = executeGetRequest(url.build());
        JmFavoritePage jmFavoritePage = HtmlParser.parseFavoritePage(jmHtmlResponse.getHtml(), page);
        cacheJmFavoritePage(jmFavoritePage);
        return jmFavoritePage;
    }

    // == 会话管理层实现 ==

    @Override
    public JmUserInfo login(String username, String password) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("login")
                .build();

        /*
         * 禁漫网页端登录需要提交 HTML 表单格式的 POST 请求，
         * 包含 username、password 以及记住登录状态的复选框参数。
         * 登录成功后会设置会话 Cookie，后续请求自动携带。
         */
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("id_remember", "on")
                .add("login_remember", "on")
                .add("submit_login", "")
                .build();

        JmHtmlResponse jmHtmlResponse = executePostRequest(url, formBody);
        super.cacheUsername(username);
        // TODO 获取用户信息（当前 HTML 客户端仅缓存用户名）
        return JmUserInfo.partial(username);
    }

    @Override
    public JmComment postComment(String entityId, String commentText) {
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

        JmHtmlResponse jmHtmlResponse = executePostRequest(url, formBuilder.build());
        try {
            /*
             * 网页端评论接口返回 JSON，只带了评论 ID (cid)，没有完整信息。
             * 返回的 JmComment 只有部分字段有值。
             */
            String json = jmHtmlResponse.getHtml();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            boolean err = true;
            if (jsonObject.has("err") && !jsonObject.get("err").isJsonNull()) {
                err = jsonObject.get("err").getAsBoolean();
            }

            if (err) {
                String message = "";
                if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull()) {
                    message = jsonObject.get("message").getAsString();
                }
                throw new ResponseException("Failed to post comment :" + message);
            }
            String cid = "";
            if (jsonObject.has("cid") && !jsonObject.get("cid").isJsonNull()) {
                cid = jsonObject.get("cid").getAsString();
            }
            return new JmComment(cid, "", getLoggedInUserName(), commentText, "", "", "", entityId, "", List.of(), 0, 0);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse post comment response", e);
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

        JmHtmlResponse jmHtmlResponse = executePostRequest(url, formBuilder.build());
        try {
            String json = jmHtmlResponse.getHtml();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            boolean err = true;
            if (jsonObject.has("err") && !jsonObject.get("err").isJsonNull()) {
                err = jsonObject.get("err").getAsBoolean();
            }

            if (err) {
                String message = "";
                if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull()) {
                    message = jsonObject.get("message").getAsString();
                }
                throw new ResponseException("Failed to post comment :" + message);
            }
            String cid = "";
            if (jsonObject.has("cid") && !jsonObject.get("cid").isJsonNull()) {
                cid = jsonObject.get("cid").getAsString();
            }
            return new JmComment(cid, "", getLoggedInUserName(), commentText, "", "", "", entityId, "", List.of(), 0, 0);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse reply comment response", e);
        }
    }

    @Override
    public JmComment postBlogComment(String albumId, String blogId, String commentText) {
        throw new UnsupportedOperationException("Posting blog comment via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmComment replyToBlogComment(String albumId, String blogId, String commentText, String parentCommentId) {
        throw new UnsupportedOperationException("Replying to blog comment via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void toggleAlbumFavorite(String albumId, String folderId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("ajax")
                .addPathSegment("favorite_album")
                .addQueryParameter("album_id", albumId)
                .addQueryParameter("fid", folderId == null ? "0" : folderId)
                .build();

        JmHtmlResponse jmHtmlResponse = executeGetRequest(url);
        try {
            /*
             * 网页端收藏接口返回 JSON，status=1 成功，=0 表示已收藏过。
             */
            JsonObject jsonObject = JsonParser.parseString(jmHtmlResponse.getHtml()).getAsJsonObject();

            int status = 0;
            if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
                status = jsonObject.get("status").getAsInt();
            }

            if (status != 1) {
                String message = "";
                if (jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()) {
                    message = jsonObject.get("msg").getAsString();
                }
                throw new ResponseException("Failed to toggle favorite: " + message);
            }
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse 'toggle favorite' response", e);
        }
    }

    @Override
    public JmCommentList getComments(ForumQuery query) {
        throw new UnsupportedOperationException("Getting comment list via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public JmVoteResult voteComment(String commentId, VoteType voteType) {
        throw new UnsupportedOperationException("Voting on comments via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void toggleAlbumLike(String albumId) {
        throw new UnsupportedOperationException("Toggling album like via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public List<String> getHotTags() {
        throw new UnsupportedOperationException("Getting hot tags via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmSearchPage getLatest(int page) {
        throw new UnsupportedOperationException("Getting latest albums via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmAlbumDownloadInfo getAlbumDownloadInfo(String albumId) {
        throw new UnsupportedOperationException("Getting album download info via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmFavoriteFolderResult manageFavoriteFolder(FavoriteFolderType type, String folderId, String folderName, String albumId) {
        throw new UnsupportedOperationException("Managing favorite folders via HTML client is not currently supported. Use JmApiClient instead.");
    }

    // == HTML 客户端暂不实现（使用 JmApiClient） ==

    @Override
    @Deprecated
    public Map register(String username, String password, String passwordConfirm, String email) {
        throw new UnsupportedOperationException("Register via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException("Logout via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public void forgotPassword(String email) {
        throw new UnsupportedOperationException("Forgot password via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmUserProfile getUserProfile(String uid) {
        throw new UnsupportedOperationException("Get user profile via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmUserProfile editUserProfile(String uid, Map<String, String> params) {
        throw new UnsupportedOperationException("Edit user profile via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public List<JmAlbumMeta> getWatchHistory(int page) {
        throw new UnsupportedOperationException("Get history via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void deleteWatchHistory(String id) {
        throw new UnsupportedOperationException("Delete history via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public List<JmAlbumMeta> getRandomRecommend() {
        throw new UnsupportedOperationException("Get random recommend via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public Map getPromote() {
        throw new UnsupportedOperationException("Get promote via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmSearchPage getSerialization(int page) {
        throw new UnsupportedOperationException("Get serialization via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public List<JmTagFavorite> getTagsFavorite() {
        throw new UnsupportedOperationException("Get tags favorite via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void addFavoriteTags(List<String> tags) {
        throw new UnsupportedOperationException("Add favorite tags via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void removeFavoriteTags(List<String> tags) {
        throw new UnsupportedOperationException("Remove favorite tags via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmCategoryList getCategoriesList() {
        throw new UnsupportedOperationException("Get categories list via HTML client is not currently supported. Use JmApiClient instead.");
    }

    // == 小说 + 创作者（暂不实现，用 JmApiClient） ==

    @Override
    public JmNovelPage getNovelList(String order, int page) {
        throw new UnsupportedOperationException("Get novel list via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmNovelDetail getNovelDetail(String novelId) {
        throw new UnsupportedOperationException("Get novel detail via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmNovelChapter getNovelChapter(String chapterId, String lang) {
        throw new UnsupportedOperationException("Get novel chapter via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmNovelPage searchNovels(String searchQuery) {
        throw new UnsupportedOperationException("Search novels via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void toggleNovelLike(String novelId) {
        throw new UnsupportedOperationException("Toggling novel like via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmComment postNovelComment(String novelId, String commentText, String chapterId) {
        throw new UnsupportedOperationException("Posting novel comment via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmComment replyToNovelComment(String novelId, String commentText, String parentCommentId, String chapterId) {
        throw new UnsupportedOperationException("Replying to novel comment via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void toggleNovelFavorite(String novelId) {
        throw new UnsupportedOperationException("Toggle novel favorite via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmNovelFavoritesPage getNovelFavorites(int page, String folderId, String order) {
        throw new UnsupportedOperationException("Get novel favorites via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmFavoriteFolderResult manageNovelFavoriteFolder(FavoriteFolderType type, String folderId, String folderName, String novelId) {
        throw new UnsupportedOperationException("Edit novel favorites via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public Map buyNovelChapter(String chapterId) {
        throw new UnsupportedOperationException("Buy novel chapter via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmCreatorPage getCreatorAuthors(int page, String searchQuery) {
        throw new UnsupportedOperationException("Get creator authors via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmCreatorWorkPage getCreatorWorks(int page, String searchValue, String lang, String source) {
        throw new UnsupportedOperationException("Get creator works via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmCreatorAuthorWorksPage getCreatorAuthorWorks(String creatorId, String language, String source, int page) {
        throw new UnsupportedOperationException("Get creator author works via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmCreatorWorkInfo getCreatorWorkInfo(String workId) {
        throw new UnsupportedOperationException("Get creator work info via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmCreatorWorkDetail getCreatorWorkDetail(String workId) {
        throw new UnsupportedOperationException("Get creator work detail via HTML client is not currently supported. Use JmApiClient instead.");
    }

    // == 通知/任务/签到/每周必看/设置（暂不实现，用 JmApiClient） ==

    @Override
    public JmNotificationPage getNotifications() {
        throw new UnsupportedOperationException("Get notifications via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void markNotification(String id, int read) {
        throw new UnsupportedOperationException("Post notification via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public Map getUnreadCount() {
        throw new UnsupportedOperationException("Get unread count via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public boolean getAlbumSertracking(String id) {
        throw new UnsupportedOperationException("Get sertracking via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void setAlbumSertracking(String id) {
        throw new UnsupportedOperationException("Set sertracking via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmTrackingPage getAlbumTrackingList(int page) {
        throw new UnsupportedOperationException("Get tracking list via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmTaskList getTasks(String type, String filter) {
        throw new UnsupportedOperationException("Get tasks via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public Map claimTask(Map<String, String> body) {
        throw new UnsupportedOperationException("Claim task via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public Map getCoinBuyList(Map<String, String> body) {
        throw new UnsupportedOperationException("Get coin buy list via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public Map buyComicWithCoin(String comicId) {
        throw new UnsupportedOperationException("Buy comic with coin via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public Map chargeCoins() {
        throw new UnsupportedOperationException("Charge coins via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    @Deprecated
    public Map setAdFree(Map<String, String> body) {
        throw new UnsupportedOperationException("Set ad free via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmDailyCheckInStatus getDailyCheckInStatus(String userId) {
        throw new UnsupportedOperationException("Get daily checkin status via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public void doDailyCheckin(String userId, String dailyId) {
        throw new UnsupportedOperationException("Do daily checkin via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public List getDailyCheckInOptions(String userId) {
        throw new UnsupportedOperationException("Get daily checkin options via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public List filterDailyCheckInList(String filter) {
        throw new UnsupportedOperationException("Filter daily checkin list via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmWeeklyPicksList getWeeklyPicksList() {
        throw new UnsupportedOperationException("Get weekly picks list via HTML client is not currently supported. Use JmApiClient instead.");
    }

    @Override
    public JmWeeklyPicksDetail getWeeklyPicksDetail(String categoryId) {
        throw new UnsupportedOperationException("Get weekly picks detail via HTML client is not currently supported. Use JmApiClient instead.");
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
        } catch (ResponseException e) {
            throw new ResponseException("Failed to get html url: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to get html url", e);
        }
        throw new ResponseException("Failed to get html url");
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
            JmHtmlResponse jmHtmlResponse = executeGetRequest(HttpUrl.parse(JmConstants.JM_PUB_URL));
            return HtmlParser.parseJmPubHtml(jmHtmlResponse.getHtml());
        } catch (ResponseException e) {
            throw new ResponseException("Failed to get html domains: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to get html domains", e);
        }
    }

    /**
     * 从 GitHub Pages 并发获取所有禁漫域名。
     * GitHub 上多个索引页（/go/300.html ~ /go/309.html）各列了一些可用域名，
     * 汇总去重后返回，同时过滤掉 jm365 开头的（实测不可用）。
     * 请求不跟随重定向，因为中间域名可能只返回 302。
     *
     * @return 禁漫网页域名列表
     */
    public List<String> getHtmlDomainAllViaGithub() {
        String template = "https://jmcmomic.github.io/go/";
        int[] indexRange = new int[]{300, 309};
        Set<String> domainSet = ConcurrentHashMap.newKeySet();
        List<String> urlsToFetch = new ArrayList<>();
        for (int i = indexRange[0]; i <= indexRange[1]; i++) {
            urlsToFetch.add(template + i + ".html");
        }

        // 并发数取"URL数量"和"CPU核心数*2"的较小值，避免创建过多线程
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

            // 等待所有并发请求完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
            try {
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

    /**
     * 往 URL 路径里追加分类信息（网页端用路径传分类，和 API 客户端的查询参数不同）。
     *
     * @param builder     URL 构建器
     * @param category    主分类，null 或 ALL 时不追加
     * @param subCategory 子分类，可选
     */
    private void buildCategoryPath(HttpUrl.Builder builder, Category category, SubCategory subCategory) {
        if (category == null || category == Category.ALL) {
            return;
        }
        builder.addPathSegment(category.getValue());
        if (subCategory != null) {
            builder.addPathSegment("sub").addPathSegment(subCategory.getValue());
        }
    }

    /**
     * 判断页面是不是本子详情页。精确匹配时禁漫会 302 到详情页，
     * 详情页有 id="book-name"，列表页没有。
     */
    private boolean isAlbumDetailPage(String html) {
        return html.contains("id=\"book-name\"");
    }
}
