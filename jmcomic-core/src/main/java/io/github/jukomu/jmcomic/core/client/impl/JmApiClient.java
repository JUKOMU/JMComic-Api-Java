package io.github.jukomu.jmcomic.core.client.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jukomu.jmcomic.api.enums.TimeOption;
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
 * @Description: JmClient 接口的API实现
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmApiClient extends AbstractJmClient {

    private static final Logger logger = LoggerFactory.getLogger(JmApiClient.class);

    public JmApiClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        super(config, httpClient, cookieManager, domainManager);
    }

    @Override
    protected void initialize() {
        updateDomains();
    }

    @Override
    public void updateDomains() {
        logger.info("开始获取最新域名列表");
        boolean success = false;
        String oldDomains = domainManager.getDomains().toString();

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
     * 获取API客户端配置
     */
    public Map setting() {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("setting")
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
        // API端点 /album?id=...
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("album")
                .addQueryParameter("id", albumId)
                .build();
        JmApiResponse jmApiResponse;
        try {
            jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        } catch (ResourceNotFoundException e) {
            throw new AlbumNotFoundException(albumId, e);
        }
        JmAlbum jmAlbum = ApiParser.parseAlbum(jmApiResponse.getDecodedData());
        cacheJmAlbum(jmAlbum);
        return jmAlbum;
    }

    @Override
    public JmPhoto getPhoto(String photoId) throws PhotoNotFoundException {
        JmPhoto cachedJmPhoto = getCachedJmPhoto(photoId);
        if (cachedJmPhoto != null) {
            return cachedJmPhoto;
        }
        // API端点 /chapter?id=...
        HttpUrl photoUrl = newHttpUrlBuilder()
                .addPathSegment("chapter")
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
        // 获取 scramble_id 的网页端点
        HttpUrl scrambleUrl = newHttpUrlBuilder()
                .addPathSegment("chapter_view_template")
                .addQueryParameter("id", photoId)
                .addQueryParameter("mode", "vertical")
                .addQueryParameter("page", "0")
                .addQueryParameter("app_img_shunt", "1")
                .addQueryParameter("express", "off")
                .addQueryParameter("v", String.valueOf(Instant.now().getEpochSecond()))
                .build();
        // 此请求使用不同的密钥
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
                .addPathSegment("search")
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
                .addPathSegment("favorite")
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
        // API的分类筛选接口排序参数 'o' 的格式为 "排序_时间"
        String orderParam = query.getOrderBy().getValue();
        if (query.getTimeOption() != TimeOption.ALL) {
            orderParam += "_" + query.getTimeOption().getValue();
        }

        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("categories")
                .addPathSegment("filter")
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
        HttpUrl url = newHttpUrlBuilder().addPathSegment("login").build();

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
        } catch (Exception e) {
            // Gson throws different exceptions, so catch a more general one
            throw new ParseResponseException("Failed to parse login response JSON", e);
        }
    }

    // == 用户交互层实现 ==

    @Override
    public JmComment postComment(String entityId, String commentText, String status) {
        throw new UnsupportedOperationException("Posting comment via API client is not currently supported.");
    }

    @Override
    public JmComment replyToComment(String entityId, String commentText, String parentCommentId) {
        throw new UnsupportedOperationException("Replying to comment via API client is not currently supported.");
    }

    @Override
    public void addAlbumToFavorite(String albumId, String folderId) {
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("favorite")
                .build();

        // API 添加收藏只需要 aid
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
                throw new ResponseException("Failed to add to favorites: " + msg);
            }
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse 'add to favorite' response", e);
        }
    }

    // == 辅助方法 ==

    private JmApiResponse executeGetRequest(HttpUrl url, String secret) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String[] token = JmCryptoTool.generateToken(timestamp, secret, "");
        Request request = addAppHeader(getGetRequestBuilder(url), token[0], token[1]).build();
        JmResponse response = executeRequest(request);
        JmApiResponse jmApiResponse = new JmApiResponse(response, timestamp);
        jmApiResponse.requireSuccess();
        return jmApiResponse;
    }

    private JmApiResponse executePostRequest(HttpUrl url, RequestBody requestBody) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String[] token = JmCryptoTool.generateToken(timestamp, JmConstants.APP_TOKEN_SECRET, JmConstants.APP_VERSION);
        Request request = addAppHeader(getPostRequestBuilder(url, requestBody), token[0], token[1]).build();
        JmResponse response = executeRequest(request);
        JmApiResponse jmApiResponse = new JmApiResponse(response, timestamp);
        jmApiResponse.requireSuccess();
        return jmApiResponse;
    }

    private Request.Builder addAppHeader(Request.Builder builder, String token1, String token2) {
        return builder
                .header(JmConstants.APP_HEADER_TOKEN, token1)
                .header(JmConstants.APP_HEADER_TOKEN_PARAM, token2);
    }
}
