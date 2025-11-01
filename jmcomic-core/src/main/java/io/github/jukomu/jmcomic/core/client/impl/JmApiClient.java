package io.github.jukomu.jmcomic.core.client.impl;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.TimeOption;
import io.github.jukomu.jmcomic.api.exception.ApiResponseException;
import io.github.jukomu.jmcomic.api.exception.ParseResponseException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.crypto.JmCryptoTool;
import io.github.jukomu.jmcomic.core.net.model.JmApiResponse;
import io.github.jukomu.jmcomic.core.net.model.JmHtmlResponse;
import io.github.jukomu.jmcomic.core.net.model.JmResponse;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import io.github.jukomu.jmcomic.core.parser.ApiParser;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.net.CookieManager;
import java.time.Instant;
import java.util.List;

/**
 * @author JUKOMU
 * @Description: JmClient 接口的API实现
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmApiClient extends AbstractJmClient {

    public JmApiClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        super(config, httpClient, cookieManager, domainManager);
    }

    @Override
    protected void initialize() {
        updateDomains();
    }

    @Override
    public void updateDomains() {
        for (String url : JmConstants.API_URL_DOMAIN_SERVER_LIST) {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            JmResponse jmResponse = executeRequest(request);
            JmHtmlResponse jmHtmlResponse = new JmHtmlResponse(jmResponse);
            String decodedJson = JmCryptoTool.decryptApiResponse(jmHtmlResponse.getHtml(), "", JmConstants.API_DOMAIN_SERVER_SECRET);
            List<String> domains = ApiParser.parseDomainsFromDomainServer(decodedJson);
            if (domains.isEmpty()) {
                continue;
            }
            domainManager.updateDomains(domains);
            break;
        }
    }

    // == 核心数据获取层实现 ==

    @Override
    public JmAlbum getAlbum(String albumId) {
        JmAlbum cachedJmAlbum = getCachedJmAlbum(albumId);
        if (cachedJmAlbum != null) {
            return cachedJmAlbum;
        }
        // API端点 /album?id=...
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("album")
                .addQueryParameter("id", albumId)
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        JmAlbum jmAlbum = ApiParser.parseAlbum(jmApiResponse.getDecodedData());
        cacheJmAlbum(jmAlbum);
        return jmAlbum;
    }

    @Override
    public JmPhoto getPhoto(String photoId) {
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
        JmApiResponse response = executeGetRequest(photoUrl, JmConstants.APP_TOKEN_SECRET);
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
                .addQueryParameter("main_tag", "0")
                .addQueryParameter("search_query", query.getSearchQuery())
                .addQueryParameter("page", String.valueOf(query.getPage()))
                .addQueryParameter("o", query.getOrderBy().getValue())
                .addQueryParameter("t", query.getTimeOption().getValue())
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        return ApiParser.parseSearchPage(jmApiResponse.getDecodedData(), query.getPage());

    }

    @Override
    public JmFavoritePage getFavorites(int page) {
        JmFavoritePage cachedJmFavoritePage = getCachedJmFavoritePage(page);
        if (cachedJmFavoritePage != null) {
            return cachedJmFavoritePage;
        }
        HttpUrl url = newHttpUrlBuilder()
                .addPathSegment("favorite")
                .addQueryParameter("page", String.valueOf(page))
                // API的收藏夹接口默认使用最新排序，不需要 'o' 参数
                .build();

        JmApiResponse jmApiResponse = executeGetRequest(url, JmConstants.APP_TOKEN_SECRET);
        JmFavoritePage jmFavoritePage = ApiParser.parseFavoritePage(jmApiResponse.getDecodedData(), page);
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
            JSONObject jsonObject = JSONObject.parseObject(decodedData);
            JmUserInfo userInfo = new JmUserInfo(
                    // 2. 使用 Fastjson 的方法逐个提取字段
                    StringUtils.defaultIfBlank(jsonObject.getString("uid"), ""),
                    StringUtils.defaultIfBlank(jsonObject.getString("username"), ""),
                    StringUtils.defaultIfBlank(jsonObject.getString("email"), ""),
                    "yes".equalsIgnoreCase(jsonObject.getString("emailverified")),
                    StringUtils.defaultIfBlank(jsonObject.getString("photo"), ""),
                    StringUtils.defaultIfBlank(jsonObject.getString("fname"), ""),
                    StringUtils.defaultIfBlank(jsonObject.getString("gender"), ""),
                    StringUtils.defaultIfBlank(jsonObject.getString("message"), ""),
                    jsonObject.getIntValue("coin"),
                    jsonObject.getIntValue("album_favorites"),
                    jsonObject.getIntValue("level"),
                    StringUtils.defaultIfBlank(jsonObject.getString("level_name"), ""),
                    jsonObject.getLongValue("nextLevelExp"),
                    jsonObject.getLongValue("exp"),
                    jsonObject.getDoubleValue("expPercent"),
                    jsonObject.getIntValue("album_favorites_max")
            );

            // 提取 's' 字段并创建 AVS Cookie
            String avsCookieValue = jsonObject.getString("s");
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
        } catch (JSONException e) {
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
            JSONObject jsonObject = JSONObject.parseObject(jmApiResponse.getDecodedData());
            if (!"ok".equalsIgnoreCase(jsonObject.getString("status"))) {
                throw new ApiResponseException("Failed to add to favorites: " + jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
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
