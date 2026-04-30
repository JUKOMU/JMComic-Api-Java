package io.github.jukomu.jmcomic.core.client.impl;

import com.google.gson.*;
import io.github.jukomu.jmcomic.api.client.JmCreatorClient;
import io.github.jukomu.jmcomic.api.client.JmNovelClient;
import io.github.jukomu.jmcomic.api.enums.FavoriteFolderType;
import io.github.jukomu.jmcomic.api.enums.TimeOption;
import io.github.jukomu.jmcomic.api.enums.VoteType;
import io.github.jukomu.jmcomic.api.exception.*;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.crypto.JmCryptoTool;
import io.github.jukomu.jmcomic.core.net.model.JmApiResponse;
import io.github.jukomu.jmcomic.core.net.model.JmHtmlResponse;
import io.github.jukomu.jmcomic.core.net.model.JmResponse;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import io.github.jukomu.jmcomic.core.parser.ApiParser;
import io.github.jukomu.jmcomic.core.util.JsonUtils;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author JUKOMU
 * @Description: JmClient 接口的 API 实现。
 * <p>
 * 调用禁漫移动端 API（与官方 APK 同源），请求要 App 签名认证，响应走 AES/GCM 加密。
 * 功能覆盖面比 HtmlClient 全（评论投票、通知、签到、创作者、小说等），优先用这个。
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmApiClient extends AbstractJmClient implements JmNovelClient, JmCreatorClient {

    private static final Logger logger = LoggerFactory.getLogger(JmApiClient.class);

    public JmApiClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        super(config, httpClient, cookieManager, domainManager);
    }

    @Override
    protected void initialize() {
        updateSetting();
    }

    @Override
    public void updateDomains() {
        logger.info("开始获取最新域名列表");
        boolean success = false;
        String oldDomains = domainManager.getDomains().toString();

        /*
         * 遍历域名服务器列表，请求拿加密的 HTML -> 解密 -> 解析 JSON 提取域名，
         * 拿到非空列表就停。
         */
        for (String url : JmConstants.API_URL_DOMAIN_SERVER_LIST) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                JmResponse jmResponse = executeRequest(request);
                JmHtmlResponse jmHtmlResponse = new JmHtmlResponse(jmResponse);
                String decodedJson = JmCryptoTool.decryptApiResponse(jmHtmlResponse.getHtml(), "", JmConstants.API_DOMAIN_SERVER_SECRET);
                List<String> newDomains = ApiParser.parseDomainsFromDomainServer(decodedJson);

                if (newDomains.isEmpty()) {
                    logger.warn("从 {} 获取的域名列表为空，尝试下一个地址。", url);
                    continue;
                }

                domainManager.updateDomains(newDomains);
                logger.info("获取最新域名列表成功: {} -> {}", oldDomains, newDomains);
                success = true;
                break;
            } catch (Exception e) {
                logger.warn("从 {} 获取域名列表失败: {}", url, e.getMessage());
            }
        }

        if (!success) {
            logger.error("获取最新域名列表失败。");
        }
    }

    /**
     * 从服务器拿最新配置，动态更新本地版本号和图片 CDN 域名。
     */
    private void updateSetting() {
        logger.info("开始获取最新API客户端设置");
        Map setting = setting();
        String jm3Version = (String) setting.getOrDefault("jm3_version", null);
        String imgHost = (String) setting.getOrDefault("img_host", null);

        // 仅在远程版本号高于本地版本号时更新，避免版本回退
        if (compareVersion(jm3Version, JmConstants.APP_VERSION) > 0) {
            logger.info("当前API客户端版本[{}]，更新API客户端版本[{}] -> [{}]", JmConstants.APP_VERSION, JmConstants.APP_VERSION, jm3Version);
            JmConstants.APP_VERSION = jm3Version;
        } else {
            logger.info("当前API客户端版本[{}]，更新API客户端版本[{}] -> [{}]", JmConstants.APP_VERSION, JmConstants.APP_VERSION, JmConstants.APP_VERSION);
        }

        if (imgHost != null) {
            JmConstants.DEFAULT_IMAGE_DOMAINS.add(imgHost);
        }
    }

    /**
     * 比较两个版本号
     *
     * @param v1 版本号1
     * @param v2 版本号2
     * @return 如果 v1 > v2 返回 1，如果 v1 < v2 返回 -1，相等返回 0
     */
    private static int compareVersion(String v1, String v2) {
        if (v1 == null && v2 == null) {
            return 0;
        }

        if (v1 == null) {
            return -1;
        }

        if (v2 == null) {
            return 1;
        }

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            // 获取每一段的数值，如果超出数组范围，则默认为 0
            int num1 = (i < parts1.length) ? Integer.parseInt(parts1[i]) : 0;
            int num2 = (i < parts2.length) ? Integer.parseInt(parts2[i]) : 0;

            if (num1 > num2) {
                return 1;
            } else if (num1 < num2) {
                return -1;
            }
        }

        return 0;
    }

    /**
     * 获取API客户端配置
     */
    public Map setting() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_SETTING)
                .build();
        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        String decodedData = jmApiResponse.getDecodedData();
        return JsonUtils.fromJson(decodedData, Map.class);
    }

    // == 核心数据获取层实现 ==

    @Override
    public JmAlbum getAlbum(String albumId) throws AlbumNotFoundException {
        JmAlbum cachedJmAlbum = getCachedJmAlbum(albumId);
        if (cachedJmAlbum != null) {
            return cachedJmAlbum;
        }
        // GET /album?id=...
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_ALBUM)
                .addQueryParameter("id", albumId)
                .build();
        JmApiResponse jmApiResponse;
        try {
            jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        } catch (ResourceNotFoundException e) {
            throw new AlbumNotFoundException(albumId, e);
        }
        String decodedData = jmApiResponse.getDecodedData();
        JmAlbum jmAlbum = ApiParser.parseAlbum(decodedData);
        cacheJmAlbum(jmAlbum);
        return jmAlbum;
    }

    @Override
    public JmAlbum getComicRead(String comicId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMIC_READ)
                .addQueryParameter("id", comicId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        String decodedData = jmApiResponse.getDecodedData();
        return ApiParser.parseComicRead(decodedData);
    }

    @Override
    public JmPhoto getPhoto(String photoId) throws PhotoNotFoundException {
        JmPhoto cachedJmPhoto = getCachedJmPhoto(photoId);
        if (cachedJmPhoto != null) {
            return cachedJmPhoto;
        }

        /*
         * 获取章节详情需要两个请求：
         * 一个拿章节元数据（APP_TOKEN_SECRET），
         * 另一个拿 scramble_id（APP_TOKEN_SECRET_2，另一个密钥），
         * 用来反解被加扰的图片 URL。
         */
        // GET /chapter?id=...
        HttpUrl photoUrl = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CHAPTER)
                .addQueryParameter("id", photoId)
                .build();
        String photoJson;
        JmApiResponse response;
        try {
            response = executeGetRequest(photoUrl, JmConstants.APP_TOKEN_SECRET);
        } catch (ResourceNotFoundException e) {
            throw new PhotoNotFoundException(photoId, e);
        }
        photoJson = response.getDecodedData();
        // 获取 scramble_id 的网页端点（使用不同的密钥和用户代理）
        HttpUrl scrambleUrl = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CHAPTER_VIEW_TEMPLATE)
                .addQueryParameter("id", photoId)
                .addQueryParameter("mode", "vertical")
                .addQueryParameter("page", "0")
                .addQueryParameter("app_img_shunt", "1")
                .addQueryParameter("express", "off")
                .addQueryParameter("v", String.valueOf(Instant.now().getEpochSecond()))
                .build();
        // 这个请求用的是另一个密钥
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String[] token = JmCryptoTool.generateToken(timestamp, JmConstants.APP_TOKEN_SECRET_2, "");
        Request request = addAppHeader(getGetRequestBuilder(scrambleUrl), token[0], token[1]).build();
        JmResponse response1 = executeRequest(request);
        JmHtmlResponse jmHtmlResponse = new JmHtmlResponse(response1);
        String scrambleId = ApiParser.parsePhotoScrambleId(jmHtmlResponse.getHtml());
        JmPhoto jmPhoto = ApiParser.parsePhoto(photoJson, scrambleId);
        cacheJmPhoto(jmPhoto);
        return jmPhoto;

    }

    @Override
    public JmSearchPage search(SearchQuery query) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_SEARCH)
                .addQueryParameter("main_tag", String.valueOf(query.getMainTag().getValue()))
                .addQueryParameter("search_query", query.getSearchQuery())
                .addQueryParameter("page", String.valueOf(query.getPage()))
                .addQueryParameter("o", query.getOrderBy().getValue())
                .addQueryParameter("t", query.getTimeOption().getValue())
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseSearchPage(jmApiResponse.getDecodedData(), query.getPage());

    }

    @Override
    public JmFavoritePage getFavorites(FavoriteQuery query) {
        JmFavoritePage cachedJmFavoritePage = getCachedJmFavoritePage(query);
        if (cachedJmFavoritePage != null) {
            return cachedJmFavoritePage;
        }
        int folderId = query.getFolderId();
        int page = query.getPage();
        HttpUrl.Builder url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_FAVORITE)
                .addQueryParameter("page", String.valueOf(page));
        if (folderId != 0) {
            url.addQueryParameter("folder_id", String.valueOf(folderId));
        }

        JmApiResponse jmApiResponse = executeGetRequest(url.build(), JmConstants.APP_TOKEN_SECRET);
        JmFavoritePage jmFavoritePage = ApiParser.parseFavoritePage(jmApiResponse.getDecodedData(), query);
        cacheJmFavoritePage(jmFavoritePage);
        return jmFavoritePage;

    }

    @Override
    public JmSearchPage getCategories(SearchQuery query) {
        // 分类筛选的排序参数格式是 "排序_时间"
        String orderParam = query.getOrderBy().getValue();
        if (query.getTimeOption() != TimeOption.ALL) {
            orderParam += "_" + query.getTimeOption().getValue();
        }

        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CATEGORIES_LIST)
                .addPathSegment(JmConstants.API_FILTER)
                .addQueryParameter("page", String.valueOf(query.getPage()))
                .addQueryParameter("order", "")
                .addQueryParameter("c", query.getCategory().getValue())
                .addQueryParameter("o", orderParam)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseSearchPage(jmApiResponse.getDecodedData(), query.getPage());

    }

    // == 会话管理层实现 ==

    @Override
    public JmUserInfo login(String username, String password) {
        HttpUrl url = newHttpUrlBuilder().addPathSegment(JmConstants.API_LOGIN).build();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            String decodedData = jmApiResponse.getDecodedData();
            JsonObject jsonObject = JsonParser.parseString(decodedData).getAsJsonObject();

            String uid = jsonObject.has("uid") && !jsonObject.get("uid").isJsonNull() ? jsonObject.get("uid").getAsString() : "";
            String usernameResult = jsonObject.has("username") && !jsonObject.get("username").isJsonNull() ? jsonObject.get("username").getAsString() : "";
            String email = jsonObject.has("email") && !jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "";
            String emailVerifiedStr = jsonObject.has("emailverified") && !jsonObject.get("emailverified").isJsonNull() ? jsonObject.get("emailverified").getAsString() : "";
            String photo = jsonObject.has("photo") && !jsonObject.get("photo").isJsonNull() ? jsonObject.get("photo").getAsString() : "";
            String fname = jsonObject.has("fname") && !jsonObject.get("fname").isJsonNull() ? jsonObject.get("fname").getAsString() : "";
            String gender = jsonObject.has("gender") && !jsonObject.get("gender").isJsonNull() ? jsonObject.get("gender").getAsString() : "";
            String message = jsonObject.has("message") && !jsonObject.get("message").isJsonNull() ? jsonObject.get("message").getAsString() : "";
            int coin = jsonObject.has("coin") && !jsonObject.get("coin").isJsonNull() ? jsonObject.get("coin").getAsInt() : 0;
            int albumFavorites = jsonObject.has("album_favorites") && !jsonObject.get("album_favorites").isJsonNull() ? jsonObject.get("album_favorites").getAsInt() : 0;
            int level = jsonObject.has("level") && !jsonObject.get("level").isJsonNull() ? jsonObject.get("level").getAsInt() : 0;
            String levelName = jsonObject.has("level_name") && !jsonObject.get("level_name").isJsonNull() ? jsonObject.get("level_name").getAsString() : "";
            long nextLevelExp = jsonObject.has("nextLevelExp") && !jsonObject.get("nextLevelExp").isJsonNull() ? jsonObject.get("nextLevelExp").getAsLong() : 0L;
            long exp = jsonObject.has("exp") && !jsonObject.get("exp").isJsonNull() ? jsonObject.get("exp").getAsLong() : 0L;
            double expPercent = jsonObject.has("expPercent") && !jsonObject.get("expPercent").isJsonNull() ? jsonObject.get("expPercent").getAsDouble() : 0.0;
            int albumFavoritesMax = jsonObject.has("album_favorites_max") && !jsonObject.get("album_favorites_max").isJsonNull() ? jsonObject.get("album_favorites_max").getAsInt() : 0;


            JmUserInfo userInfo = new JmUserInfo(
                    StringUtils.defaultIfBlank(uid, ""),
                    StringUtils.defaultIfBlank(usernameResult, ""),
                    StringUtils.defaultIfBlank(email, ""),
                    "yes".equalsIgnoreCase(emailVerifiedStr),
                    JmConstants.PROTOCOL_HTTPS + JmConstants.DEFAULT_IMAGE_DOMAINS.get(new Random().nextInt(JmConstants.DEFAULT_IMAGE_DOMAINS.size())) + "/media/users/" + StringUtils.defaultIfBlank(photo, ""),
                    StringUtils.defaultIfBlank(fname, ""),
                    StringUtils.defaultIfBlank(gender, ""),
                    StringUtils.defaultIfBlank(message, ""),
                    coin,
                    albumFavorites,
                    level,
                    StringUtils.defaultIfBlank(levelName, ""),
                    nextLevelExp,
                    exp,
                    expPercent,
                    albumFavoritesMax
            );

            // 提取 's' 字段并创建 AVS Cookie
            String avsCookieValue = jsonObject.has("s") && !jsonObject.get("s").isJsonNull() ? jsonObject.get("s").getAsString() : null;
            if (StringUtils.isNotBlank(avsCookieValue)) {
                Cookie avsCookie = new Cookie.Builder()
                        .name("AVS")
                        .value(avsCookieValue)
                        .domain(newHttpUrlBuilder().build().host())
                        .path("/")
                        .build();
                httpClient.cookieJar().saveFromResponse(newHttpUrlBuilder().build(), List.of(avsCookie));
            }

            // 缓存用户名
            if (userInfo.username() != null) {
                super.cacheUsername(userInfo.username());
            }

            return userInfo;
        } catch (ResponseException e) {
            logger.error("Failed to login with error message :{}", e.getMessage());
            throw e;
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse login response JSON", e);
        }
    }

    // == 用户交互层实现 ==

    @Override
    public JmCommentList getComments(ForumQuery query) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_FORUM)
                .addQueryParameter(query.getIdParam(), query.getEntityId())
                .addQueryParameter("mode", query.getMode().getValue())
                .addQueryParameter("page", String.valueOf(query.getPage()));
        if (query.getChapterId() != null && !query.getChapterId().isEmpty()) {
            urlBuilder.addQueryParameter("ncid", query.getChapterId());
        }

        JmApiResponse jmApiResponse = executeGetRequest(urlBuilder.build(), JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseCommentList(jmApiResponse.getDecodedData());
    }

    @Override
    public JmComment postComment(String entityId, String commentText) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMMENT)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("aid", entityId)
                .add("comment", commentText)
                .add("comment_id", "0")
                .build();

        JmApiResponse jmApiResponse = executePostRequest(url, formBody);
        return ApiParser.parseCommentSubmitResult(
                jmApiResponse.getDecodedData(), entityId, commentText,
                getLoggedInUserName() != null ? getLoggedInUserName() : "");
    }

    @Override
    public JmComment replyToComment(String entityId, String commentText, String parentCommentId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMMENT)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("aid", entityId)
                .add("comment", commentText)
                .add("comment_id", parentCommentId)
                .build();

        JmApiResponse jmApiResponse = executePostRequest(url, formBody);
        return ApiParser.parseCommentSubmitResult(
                jmApiResponse.getDecodedData(), entityId, commentText,
                getLoggedInUserName() != null ? getLoggedInUserName() : "");
    }

    @Override
    public JmComment postBlogComment(String albumId, String blogId, String commentText) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMMENT)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("aid", albumId)
                .add("bid", blogId)
                .add("comment", commentText)
                .build();

        JmApiResponse jmApiResponse = executePostRequest(url, formBody);
        return ApiParser.parseCommentSubmitResult(
                jmApiResponse.getDecodedData(), albumId, commentText,
                getLoggedInUserName() != null ? getLoggedInUserName() : "");
    }

    @Override
    public JmComment replyToBlogComment(String albumId, String blogId, String commentText, String parentCommentId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMMENT)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("aid", albumId)
                .add("bid", blogId)
                .add("comment", commentText)
                .add("comment_id", parentCommentId)
                .build();

        JmApiResponse jmApiResponse = executePostRequest(url, formBody);
        return ApiParser.parseCommentSubmitResult(
                jmApiResponse.getDecodedData(), albumId, commentText,
                getLoggedInUserName() != null ? getLoggedInUserName() : "");
    }

    @Override
    @Deprecated
    public JmVoteResult voteComment(String commentId, VoteType voteType) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMMENT_VOTE)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("comment_id", commentId)
                .add("vote_type", voteType.getValue())
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            return ApiParser.parseVoteResult(jmApiResponse.getDecodedData());
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse vote comment response", e);
        }
    }

    @Override
    public void toggleAlbumLike(String albumId) {
        doToggleLike(albumId, null);
    }

    @Override
    public List<String> getHotTags() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_HOT_TAGS)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        /*
         * hot_tags 响应可能是纯 JSON 数组或者 {"list": [...]} 两种格式，
         * 先试直接解析 List，不行就尝试取 list 字段。
         * 解密也失败就返回空列表，不阻塞调用方。
         */
        try {
            String decodedData = jmApiResponse.getDecodedData();
            try {
                return JsonUtils.fromJson(decodedData, List.class);
            } catch (Exception e) {
                JsonObject jsonObject = JsonParser.parseString(decodedData).getAsJsonObject();
                List<String> tags = new java.util.ArrayList<>();
                if (jsonObject.has("list") && jsonObject.get("list").isJsonArray()) {
                    for (JsonElement item : jsonObject.getAsJsonArray("list")) {
                        tags.add(item.getAsString());
                    }
                }
                return tags;
            }
        } catch (JmComicException e) {
            logger.warn("Failed to decrypt hot_tags response, returning empty list", e);
            return List.of();
        }
    }

    @Override
    public JmSearchPage getLatest(int page) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_LATEST)
                .addQueryParameter("page", String.valueOf(page))
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseLatestOrPromoteList(jmApiResponse.getDecodedData(), page);
    }

    @Override
    public JmAlbumDownloadInfo getAlbumDownloadInfo(String albumId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_ALBUM_DOWNLOAD)
                .addPathSegment(albumId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseAlbumDownloadInfo(jmApiResponse.getDecodedData());
    }

    @Override
    public JmFavoriteFolderResult manageFavoriteFolder(FavoriteFolderType type, String folderId, String folderName, String albumId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_FAVORITE_FOLDER)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("type", type.getValue());

        if (folderId != null && !folderId.isEmpty()) {
            formBuilder.add("folder_id", folderId);
        }
        if (folderName != null && !folderName.isEmpty()) {
            formBuilder.add("folder_name", folderName);
        }
        if (albumId != null && !albumId.isEmpty()) {
            formBuilder.add("aid", albumId);
        }

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
            return ApiParser.parseFavoriteFolderResult(jmApiResponse.getDecodedData());
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse manage favorite folder response", e);
        }
    }

    // == 会话管理扩展 ==

    @Override
    @Deprecated
    public Map register(String username, String password, String passwordConfirm, String email) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_MEMBER_REGISTER)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("password_confirm", passwordConfirm)
                .add("email", email)
                .add("gender", "")
                .add("adult", "false")
                .add("PrivacyPolicy", "false")
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse register response", e);
        }
    }

    @Override
    public void logout() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_MEMBER_LOGOUT)
                .build();

        executePostRequest(url, new FormBody.Builder().build());
    }

    @Override
    @Deprecated
    public void forgotPassword(String email) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_MEMBER_FORGOT)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .build();

        executePostRequest(url, formBody);
    }

    @Override
    public JmUserProfile getUserProfile(String uid) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_USEREDIT)
                .addPathSegment(uid)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseUserProfile(jmApiResponse.getDecodedData());
    }

    @Override
    public JmUserProfile editUserProfile(String uid, Map<String, String> params) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_USEREDIT)
                .addPathSegment(uid)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }

        JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
        return ApiParser.parseUserProfile(jmApiResponse.getDecodedData());
    }

    // == 历史/推荐/标签/分类 ==

    @Override
    public List<JmAlbumMeta> getWatchHistory(int page) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_HISTORY_LIST)
                .addQueryParameter("page", String.valueOf(page))
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseHistoryList(jmApiResponse.getDecodedData());
    }

    @Override
    public void deleteWatchHistory(String id) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_HISTORY_LIST)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
                .build();

        executePostRequest(url, formBody);
    }

    @Override
    public List<JmAlbumMeta> getRandomRecommend() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMIC_RANDOM_RECOMMEND)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseRandomRecommendList(jmApiResponse.getDecodedData());
    }

    @Override
    public Map getPromote() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMIC_PROMOTE)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        // API 返回的是数组，接口要 Map，包一层 {"list": [...]} 兼容一下
        List<Map> list = JsonUtils.fromJson(jmApiResponse.getDecodedData(), List.class);
        return Map.of("list", list);
    }

    @Override
    public JmSearchPage getSerialization(int page) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMIC_SER_MORE_LIST)
                .addQueryParameter("page", String.valueOf(page))
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseSearchPage(jmApiResponse.getDecodedData(), page);
    }

    @Override
    public List<JmTagFavorite> getTagsFavorite() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_TAGS_FAVORITE)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseTagFavoriteList(jmApiResponse.getDecodedData());
    }

    @Override
    public void addFavoriteTags(List<String> tags) {
        updateFavoriteTags("add", tags);
    }

    @Override
    public void removeFavoriteTags(List<String> tags) {
        updateFavoriteTags("remove", tags);
    }

    /**
     * 更新收藏标签（内部公共实现）。
     *
     * @param type 操作类型："add" 或 "remove"
     * @param tags 标签名称列表
     * @throws ResponseException 服务器返回 status != "ok" 时
     */
    private void updateFavoriteTags(String type, List<String> tags) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_TAGS_FAVORITE_UPDATE)
                .build();
        RequestBody formBody = new FormBody.Builder()
                .add("type", type)
                .add("tags", String.join(",", tags))
                .build();
        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            String decodedData = jmApiResponse.getDecodedData();
            JsonObject jsonObject = JsonParser.parseString(decodedData).getAsJsonObject();
            String status = jsonObject.has("status") && !jsonObject.get("status").isJsonNull()
                    ? jsonObject.get("status").getAsString() : "";
            if (!"ok".equals(status)) {
                String msg = jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()
                        ? jsonObject.get("msg").getAsString() : "";
                throw new ResponseException(msg);
            }
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse update tags favorite response", e);
        }
    }

    @Override
    public JmCategoryList getCategoriesList() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CATEGORIES_LIST)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseCategoryList(jmApiResponse.getDecodedData());
    }

    @Override
    public void toggleAlbumFavorite(String albumId, String folderId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_FAVORITE)
                .build();

        // Toggle 操作：POST { aid }，服务端自动判定收藏/取消收藏
        RequestBody formBody = new FormBody.Builder()
                .add("aid", albumId)
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            JsonObject jsonObject = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();

            String status = "";
            if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
                status = jsonObject.get("status").getAsString();
            }

            if (!"ok".equalsIgnoreCase(status)) {
                String msg = "";
                if (jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()) {
                    msg = jsonObject.get("msg").getAsString();
                }
                throw new ResponseException("Failed to toggle favorite: " + msg);
            }
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse 'toggle favorite' response", e);
        }
    }

    // == 小说子系统实现 ==

    @Override
    public JmNovelPage getNovelList(String order, int page) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOVEL_LIST)
                .addQueryParameter("o", order != null ? order : "")
                .addQueryParameter("t", String.valueOf(Instant.now().getEpochSecond()))
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseNovelPage(jmApiResponse.getDecodedData());
    }

    @Override
    public JmNovelDetail getNovelDetail(String novelId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOVEL_DETAIL)
                .addQueryParameter("nid", novelId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseNovelDetail(jmApiResponse.getDecodedData());
    }

    @Override
    public JmNovelChapter getNovelChapter(String chapterId, String lang) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOVEL_CHAPTERS)
                .addQueryParameter("ncid", chapterId)
                .addQueryParameter("lang", lang)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseNovelChapter(jmApiResponse.getDecodedData());
    }

    @Override
    public JmNovelPage searchNovels(String searchQuery) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOVEL_SEARCH)
                .addQueryParameter("search_query", searchQuery)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseNovelPage(jmApiResponse.getDecodedData());
    }

    @Override
    public void toggleNovelLike(String novelId) {
        doToggleLike(novelId, "novel");
    }

    @Override
    public JmComment postNovelComment(String novelId, String commentText, String chapterId) {
        return doPostNovelComment(novelId, commentText, null, chapterId);
    }

    @Override
    public JmComment replyToNovelComment(String novelId, String commentText, String parentCommentId, String chapterId) {
        return doPostNovelComment(novelId, commentText, parentCommentId, chapterId);
    }

    private JmComment doPostNovelComment(String novelId, String commentText, String parentCommentId, String chapterId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COMMENT)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("nid", novelId)
                .add("comment", commentText);
        if (parentCommentId != null && !parentCommentId.isEmpty()) {
            formBuilder.add("comment_id", parentCommentId);
        }
        if (chapterId != null && !chapterId.isEmpty()) {
            formBuilder.add("ncid", chapterId);
        }

        JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
        return ApiParser.parseCommentSubmitResult(
                jmApiResponse.getDecodedData(), novelId, commentText,
                getLoggedInUserName() != null ? getLoggedInUserName() : "");
    }

    @Override
    public void toggleNovelFavorite(String novelId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOVEL_FAVORITES)
                .build();

        // Toggle 操作：POST { nid }，服务端自动判定收藏/取消收藏
        RequestBody formBody = new FormBody.Builder()
                .add("nid", novelId)
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            JsonObject jsonObject = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();

            String status = "";
            if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
                status = jsonObject.get("status").getAsString();
            }

            if (!"ok".equalsIgnoreCase(status)) {
                String msg = "";
                if (jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()) {
                    msg = jsonObject.get("msg").getAsString();
                }
                throw new ResponseException("Failed to toggle novel favorite: " + msg);
            }
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse 'toggle novel favorite' response", e);
        }
    }

    @Override
    public JmNovelFavoritesPage getNovelFavorites(int page, String folderId, String order) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOVEL_FAVORITES)
                .addQueryParameter("page", String.valueOf(page))
                .addQueryParameter("o", order != null ? order : "");
        if (folderId != null && !folderId.isEmpty()) {
            urlBuilder.addQueryParameter("folder_id", folderId);
        }

        JmApiResponse jmApiResponse = executeGetRequest(urlBuilder.build(), JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseNovelFavoritesPage(jmApiResponse.getDecodedData());
    }

    @Override
    public JmFavoriteFolderResult manageNovelFavoriteFolder(FavoriteFolderType type, String folderId, String folderName, String novelId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_EDIT_NOVEL_FAVORITES)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("type", type.getValue());
        if (folderId != null && !folderId.isEmpty()) {
            formBuilder.add("folder_id", folderId);
        }
        if (folderName != null && !folderName.isEmpty()) {
            formBuilder.add("folder_name", folderName);
        }
        if (novelId != null && !novelId.isEmpty()) {
            formBuilder.add("nid", novelId);
        }

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
            return ApiParser.parseFavoriteFolderResult(jmApiResponse.getDecodedData());
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse manage novel favorite folder response", e);
        }
    }

    @Override
    @Deprecated
    public Map buyNovelChapter(String chapterId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOVEL_COIN_BUY)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("id", chapterId)
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse buy novel chapter response", e);
        }
    }

    // == 创作者子系统实现 ==

    @Override
    public JmCreatorPage getCreatorAuthors(int page, String searchQuery) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CREATOR_AUTHOR)
                .addQueryParameter("page", String.valueOf(page));
        if (searchQuery != null && !searchQuery.isEmpty()) {
            urlBuilder.addQueryParameter("search_query", searchQuery);
        }

        JmApiResponse jmApiResponse = executeGetRequest(urlBuilder.build(), JmConstants.APP_TOKEN_SECRET);
        // 响应格式: {status, data: {total, content}}
        try {
            JsonObject root = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();
            String innerData = root.getAsJsonObject("data").toString();
            return ApiParser.parseCreatorPage(innerData);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse creator author list response", e);
        }
    }

    @Override
    public JmCreatorWorkPage getCreatorWorks(int page, String searchValue, String lang, String source) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CREATOR_WORK)
                .addQueryParameter("page", String.valueOf(page));
        if (searchValue != null && !searchValue.isEmpty()) {
            urlBuilder.addQueryParameter("search_value", searchValue);
        }
        if (lang != null && !lang.isEmpty()) {
            urlBuilder.addQueryParameter("lang", lang);
        }
        if (source != null && !source.isEmpty()) {
            urlBuilder.addQueryParameter("source", source);
        }

        JmApiResponse jmApiResponse = executeGetRequest(urlBuilder.build(), JmConstants.APP_TOKEN_SECRET);
        // 响应格式: {"status":200, "data":{content, total, filters}}
        try {
            JsonObject root = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();
            String innerData = root.getAsJsonObject("data").toString();
            return ApiParser.parseCreatorWorkPage(innerData);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse creator work list response", e);
        }
    }

    @Override
    public JmCreatorAuthorWorksPage getCreatorAuthorWorks(String creatorId, String language, String source, int page) {
        HttpUrl.Builder urlBuilder = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CREATOR_WORK_DETAIL)
                // APK 用的参数名是 "id" 不是 "creatorId"
                .addQueryParameter("id", creatorId)
                .addQueryParameter("page", String.valueOf(page));
        if (language != null && !language.isEmpty()) {
            // APK 用的是 "lang" 不是 "language"
            urlBuilder.addQueryParameter("lang", language);
        }
        if (source != null && !source.isEmpty()) {
            urlBuilder.addQueryParameter("source", source);
        }

        JmApiResponse jmApiResponse = executeGetRequest(urlBuilder.build(), JmConstants.APP_TOKEN_SECRET);
        // APK 响应路径: e.data.data
        try {
            JsonObject root = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();
            if (root.has("error")) {
                return new JmCreatorAuthorWorksPage("", "", "", "", "", List.of(), List.of(), Map.of("error", root.get("error").getAsString()));
            }
            return ApiParser.parseCreatorAuthorWorksPage(root.getAsJsonObject("data").toString());
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse creator author works response", e);
        }
    }

    @Override
    public JmCreatorWorkInfo getCreatorWorkInfo(String workId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CREATOR_WORK_INFO)
                .addQueryParameter("id", workId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        try {
            JsonObject root = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();
            return ApiParser.parseCreatorWorkInfo(root.getAsJsonObject("data").toString());
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse creator work info response", e);
        }
    }

    @Override
    public JmCreatorWorkDetail getCreatorWorkDetail(String workId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_CREATOR_WORK_INFO_DETAIL)
                .addQueryParameter("id", workId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        try {
            return ApiParser.parseCreatorWorkDetail(jmApiResponse.getDecodedData());
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse creator work detail response", e);
        }
    }

    // == 通知系统实现 ==

    @Override
    public JmNotificationPage getNotifications() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOTIFICATIONS)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseNotificationPage(jmApiResponse.getDecodedData());
    }

    @Override
    public void markNotification(String id, int read) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOTIFICATIONS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
                .add("read", String.valueOf(read))
                .build();

        executePostRequest(url, formBody);
    }

    @Override
    public Map getUnreadCount() {
        HttpUrl url = newHttpUrlBuilder()
                .addEncodedPathSegments(JmConstants.API_NOTIFICATIONS_UNREAD)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
    }

    @Override
    public boolean getAlbumSertracking(String id) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOTIFICATIONS_SERTRACK)
                .addQueryParameter("id", id)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        // API 直接返回 "true"/"false" 字符串
        return Boolean.parseBoolean(jmApiResponse.getDecodedData());
    }

    @Override
    public void setAlbumSertracking(String id) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOTIFICATIONS_SERTRACK)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
                .build();

        executePostRequest(url, formBody);
    }

    @Override
    public JmTrackingPage getAlbumTrackingList(int page) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_NOTIFICATIONS_TRACK_LIST)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("page", String.valueOf(page))
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            return ApiParser.parseTrackingPage(jmApiResponse.getDecodedData());
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to get tracking list", e);
        }
    }

    // == 任务/硬币系统实现 ==

    @Override
    public JmTaskList getTasks(String type, String filter) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_TASKS_LIST)
                .addQueryParameter("type", type)
                .addQueryParameter("filter", filter)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseTaskList(jmApiResponse.getDecodedData());
    }

    @Override
    @Deprecated
    public Map claimTask(Map<String, String> body) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_TASKS_LIST)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (body != null) {
            body.forEach(formBuilder::add);
        }

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
            return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to claim task", e);
        }
    }

    @Override
    @Deprecated
    public Map getCoinBuyList(Map<String, String> body) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_TASKS_BUY_LIST)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (body != null) {
            body.forEach(formBuilder::add);
        }

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
            return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to get coin buy list", e);
        }
    }

    @Override
    @Deprecated
    public Map buyComicWithCoin(String comicId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COIN_BUY_COMICS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("id", comicId)
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);
            return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to buy comic with coin", e);
        }
    }

    @Override
    @Deprecated
    public Map chargeCoins() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_COIN_BUY_CHARGE)
                .build();

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, new FormBody.Builder().build());
            return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to charge coins", e);
        }
    }

    @Override
    @Deprecated
    public Map setAdFree(Map<String, String> body) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_AD_FREE)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (body != null) {
            body.forEach(formBuilder::add);
        }

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
            return JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to set ad free", e);
        }
    }

    // == 签到系统实现 ==

    @Override
    public JmDailyCheckInStatus getDailyCheckInStatus(String userId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_DAILY)
                .addQueryParameter("user_id", userId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseDailyStatus(jmApiResponse.getDecodedData());
    }

    @Override
    public void doDailyCheckin(String userId, String dailyId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_DAILY_CHECK)
                .build();

        FormBody formBody = new FormBody.Builder()
                .add("user_id", userId)
                .add("daily_id", dailyId)
                .build();

        try {
            // 第一级校验：外层 code == 200（executePostRequest → requireSuccess() 内完成）
            JmApiResponse jmApiResponse = executePostRequest(url, formBody);

            // 解密后的 data = { msg: "签到成功" } 或 { msg: "已签到过" }
            JsonObject jsonObject = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();

            String msg = jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()
                    ? jsonObject.get("msg").getAsString() : "";

            // 第二级校验：APK 端通过 msg 内容判定（"已签到过" = 重复签到/失败）
            if (msg.contains("已签到过")) {
                throw new ResponseException("签到失败: " + msg);
            }
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse daily checkin response", e);
        }
    }

    @Override
    public List getDailyCheckInOptions(String userId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_DAILY_LIST)
                .addQueryParameter("user_id", userId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        try {
            // API返回格式: {"list": [{"title":"2024"},{"title":"2025"},...]}
            Map<String, Object> resultMap = JsonUtils.fromJson(jmApiResponse.getDecodedData(), Map.class);
            return (List) resultMap.getOrDefault("list", List.of());
        } catch (JmComicException e) {
            logger.warn("Failed to decrypt daily checkin options response, returning empty list", e);
            return List.of();
        }
    }

    @Override
    public List filterDailyCheckInList(String filter) {
        HttpUrl url = newHttpUrlBuilder()
                .addEncodedPathSegments(JmConstants.API_DAILY_LIST_FILTER)
                .build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("data", filter != null ? filter : "");

        try {
            JmApiResponse jmApiResponse = executePostRequest(url, formBuilder.build());
            JsonObject jsonObject = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();
            return JsonUtils.fromJson(jsonObject.getAsJsonArray("list").toString(), List.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to filter daily checkin list", e);
        }
    }

    // == 每周必看系统实现 ==

    @Override
    public JmWeeklyPicksList getWeeklyPicksList() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_WEEK)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseWeeklyList(jmApiResponse.getDecodedData());
    }

    @Override
    public JmWeeklyPicksDetail getWeeklyPicksDetail(String categoryId) {
        HttpUrl url = newHttpUrlBuilder()
                .addEncodedPathSegments(JmConstants.API_WEEK_FILTER_LIST)
                .addQueryParameter("id", categoryId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseWeeklyDetail(jmApiResponse.getDecodedData());
    }

    // == 辅助方法 ==

    /**
     * 点赞/取消点赞的底层实现，共享端点 /like。
     * <p>
     * 两级校验：
     * 第一级 - executePostRequest → requireSuccess() 校验外层 code == 200
     * 第二级 - 解密 data 中校验 status == "success"
     *
     * @param id       资源ID（本子ID 或 小说ID）
     * @param likeType 类型标识，本子传 null，小说传 "novel"
     * @throws ResponseException 第一级校验失败（requireSuccess）或第二级业务失败时
     */
    private void doToggleLike(String id, String likeType) {
        FormBody.Builder builder = new FormBody.Builder().add("id", id);
        if (likeType != null) {
            builder.add("like_type", likeType);
        }

        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment(JmConstants.API_LIKE)
                .build();

        try {
            // 第一级校验：外层 code == 200，在 executePostRequest → requireSuccess() 内完成
            JmApiResponse jmApiResponse = executePostRequest(url, builder.build());

            // 解密后的 data = { code: 200, msg: "...", status: "success" }
            // 内层 code 与 status 语义重叠，仅校验 status 即可
            JsonObject jsonObject = JsonParser.parseString(jmApiResponse.getDecodedData()).getAsJsonObject();

            String status = jsonObject.has("status") && !jsonObject.get("status").isJsonNull()
                    ? jsonObject.get("status").getAsString() : "";

            // 第二级校验：status == "success"
            if (!"success".equalsIgnoreCase(status)) {
                String msg = jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()
                        ? jsonObject.get("msg").getAsString() : "";
                throw new ResponseException("Failed to toggle like: status=" + status + ", msg=" + msg);
            }
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ParseResponseException("Failed to parse toggle like response", e);
        }
    }

    /**
     * 执行 API GET 请求：生成时间戳 -> 密钥生成 token -> 加认证头 -> 请求 -> 解密响应
     *
     * @param url    请求地址
     * @param secret 加密密钥
     */
    private JmApiResponse executeGetRequest(HttpUrl url, String secret) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String[] token = JmCryptoTool.generateToken(timestamp, secret, "");
        Request request = addAppHeader(getGetRequestBuilder(url), token[0], token[1]).build();
        JmResponse response = executeRequest(request);
        JmApiResponse jmApiResponse = new JmApiResponse(response, timestamp);
        jmApiResponse.requireSuccess();
        return jmApiResponse;
    }

    /**
     * 执行 API POST 请求，用默认的 APP_TOKEN_SECRET 和 APP_VERSION 签名。
     *
     * @param url         请求地址
     * @param requestBody POST 请求体
     */
    private JmApiResponse executePostRequest(HttpUrl url, RequestBody requestBody) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String[] token = JmCryptoTool.generateToken(timestamp, JmConstants.APP_TOKEN_SECRET, JmConstants.APP_VERSION);
        Request request = addAppHeader(getPostRequestBuilder(url, requestBody), token[0], token[1]).build();
        JmResponse response = executeRequest(request);
        JmApiResponse jmApiResponse = new JmApiResponse(response, timestamp);
        jmApiResponse.requireSuccess();
        return jmApiResponse;
    }

    /**
     * 给请求添加禁漫 API 认证头（token + token param）。
     *
     * @param builder Request.Builder
     * @param token1  认证令牌
     * @param token2  令牌参数
     */
    private Request.Builder addAppHeader(Request.Builder builder, String token1, String token2) {
        return builder
                .header(JmConstants.APP_HEADER_TOKEN, token1)
                .header(JmConstants.APP_HEADER_TOKEN_PARAM, token2);
    }
}
