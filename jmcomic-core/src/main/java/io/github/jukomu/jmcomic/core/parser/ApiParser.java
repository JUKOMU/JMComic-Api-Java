package io.github.jukomu.jmcomic.core.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jukomu.jmcomic.api.exception.ParseResponseException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JUKOMU
 * @Description: 内部工具类，负责将JMComic移动端API的JSON响应解析为Java数据模型
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class ApiParser {

    private static final Random RANDOM = new Random();

    private ApiParser() {
    }

    /**
     * 解析本子详情页 (Album Page) 的API JSON响应
     *
     * @param json API返回的JSON字符串
     * @return 一个 JmAlbum 对象
     */
    public static JmAlbum parseAlbum(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            // API返回的 "author" 字段实际是作者列表
            String albumId = "";
            if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
                albumId = StringUtils.defaultIfBlank(jsonObject.get("id").getAsString(), "");
            }

            String name = "";
            if (jsonObject.has("name") && !jsonObject.get("name").isJsonNull()) {
                name = StringUtils.defaultIfBlank(jsonObject.get("name").getAsString(), "");
            }

            String description = "";
            if (jsonObject.has("description") && !jsonObject.get("description").isJsonNull()) {
                description = StringUtils.defaultIfBlank(jsonObject.get("description").getAsString(), "");
            }

            String likes = "0";
            if (jsonObject.has("likes") && !jsonObject.get("likes").isJsonNull()) {
                likes = StringUtils.defaultIfBlank(jsonObject.get("likes").getAsString(), "0");
            }

            String totalViews = "0";
            if (jsonObject.has("total_views") && !jsonObject.get("total_views").isJsonNull()) {
                totalViews = StringUtils.defaultIfBlank(jsonObject.get("total_views").getAsString(), "0");
            }

            int comment_total = 0;
            if (jsonObject.has("comment_total") && !jsonObject.get("comment_total").isJsonNull()) {
                comment_total = jsonObject.get("comment_total").getAsInt();
            }

            List<String> authors = new ArrayList<>();
            if (jsonObject.has("author") && jsonObject.get("author").isJsonArray()) {
                for (JsonElement element : jsonObject.getAsJsonArray("author")) {
                    authors.add(element.getAsString());
                }
            }

            List<String> works = new ArrayList<>();
            if (jsonObject.has("works") && jsonObject.get("works").isJsonArray()) {
                for (JsonElement element : jsonObject.getAsJsonArray("works")) {
                    works.add(element.getAsString());
                }
            }

            List<String> actors = new ArrayList<>();
            if (jsonObject.has("actors") && jsonObject.get("actors").isJsonArray()) {
                for (JsonElement element : jsonObject.getAsJsonArray("actors")) {
                    actors.add(element.getAsString());
                }
            }

            List<String> tags = new ArrayList<>();
            if (jsonObject.has("tags") && jsonObject.get("tags").isJsonArray()) {
                for (JsonElement element : jsonObject.getAsJsonArray("tags")) {
                    tags.add(element.getAsString());
                }
            }

            JsonArray relatedListArray = jsonObject.has("related_list") && jsonObject.get("related_list").isJsonArray()
                    ? jsonObject.getAsJsonArray("related_list")
                    : new JsonArray();
            List<JmAlbumMeta> relatedAlbums = parseRelatedAlbums(relatedListArray);

            JsonArray seriesArray = jsonObject.has("series") && jsonObject.get("series").isJsonArray()
                    ? jsonObject.getAsJsonArray("series")
                    : new JsonArray();
            List<JmPhotoMeta> photoMetas = parsePhotoMetas(seriesArray, albumId, name);

            return new JmAlbum(
                    albumId,
                    name,
                    description, // description 可能为 null
                    "0", // API Album 响应中没有 scramble_id，使用一个默认值
                    "",  // API Album 响应中没有 pub_date
                    "",  // API Album 响应中没有 update_date
                    0,   // API Album 响应中没有 page_count
                    likes,
                    totalViews,
                    comment_total,
                    authors,
                    works,
                    actors,
                    tags,
                    relatedAlbums,
                    photoMetas
            );
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse album API JSON", e);
        }
    }

    private static List<JmAlbumMeta> parseRelatedAlbums(JsonArray relatedListArray) {
        if (relatedListArray == null || relatedListArray.isEmpty()) {
            return Collections.emptyList();
        }

        List<JmAlbumMeta> results = new ArrayList<>();
        for (JsonElement item : relatedListArray) {
            JsonObject node = item.getAsJsonObject();

            String id = null;
            if (node.has("id") && !node.get("id").isJsonNull()) {
                id = node.get("id").getAsString();
            }

            String name = null;
            if (node.has("name") && !node.get("name").isJsonNull()) {
                name = node.get("name").getAsString();
            }

            String author = null;
            if (node.has("author") && !node.get("author").isJsonNull()) {
                author = node.get("author").getAsString();
            }

            results.add(new JmAlbumMeta(
                    StringUtils.defaultIfBlank(id, ""),
                    StringUtils.defaultIfBlank(name, ""),
                    List.of(StringUtils.defaultIfBlank(author, "")),
                    Collections.emptyList()
            ));
        }
        return results;
    }

    /**
     * 从一个包含章节摘要信息的JSON数组节点中解析出 List<JmPhotoMeta>
     *
     * @param seriesArray 章节摘要信息数组
     * @param albumId     章节所属本子id
     * @param albumName   章节所属本子名字
     */
    private static List<JmPhotoMeta> parsePhotoMetas(JsonArray seriesArray, String albumId, String albumName) {
        if (seriesArray == null || seriesArray.isEmpty()) {
            // 单章本, series 列表为空
            return List.of(new JmPhotoMeta(albumId, albumName, 1));
        }

        List<JmPhotoMeta> photoMetas = new ArrayList<>();
        for (JsonElement item : seriesArray) {
            JsonObject node = item.getAsJsonObject();

            String id = "";
            if (node.has("id") && !node.get("id").isJsonNull()) {
                id = node.get("id").getAsString();
            }

            String name = "";
            if (node.has("name") && !node.get("name").isJsonNull()) {
                name = node.get("name").getAsString();
            }

            int sort = 0;
            if (node.has("sort") && !node.get("sort").isJsonNull()) {
                sort = node.get("sort").getAsInt();
            }

            photoMetas.add(new JmPhotoMeta(
                    StringUtils.defaultIfBlank(id, ""),
                    StringUtils.defaultIfBlank(name, ""),
                    sort
            ));
        }
        return photoMetas;
    }

    /**
     * 解析章节scramble_id
     *
     * @param html 章节的scramble id网页文本
     * @return 图片解密的scramble id
     */
    public static String parsePhotoScrambleId(String html) {
        Matcher matcher = Pattern.compile("var scramble_id = (\\d+);").matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 网页结构变化导致无法解析
        // throw new ParseResponseException("Failed to parse scramble_id");
        // 返回兜底值
        return String.valueOf(JmConstants.SCRAMBLE_220980);
    }

    /**
     * 解析章节详情页 (Photo Page) 的API JSON响应
     *
     * @param json       API返回的JSON字符串
     * @param scrambleId 从另一个接口获取到的 scrambleId，需要在此处传入
     * @return 一个 JmPhoto 对象
     */
    public static JmPhoto parsePhoto(String json, String scrambleId) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            String photoId = "";
            if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
                photoId = StringUtils.defaultIfBlank(jsonObject.get("id").getAsString(), "");
            }

            String name = "";
            if (jsonObject.has("name") && !jsonObject.get("name").isJsonNull()) {
                name = StringUtils.defaultIfBlank(jsonObject.get("name").getAsString(), "");
            }

            String seriesId = "";
            if (jsonObject.has("series_id") && !jsonObject.get("series_id").isJsonNull()) {
                seriesId = StringUtils.defaultIfBlank(jsonObject.get("series_id").getAsString(), "");
            }

            JsonArray series = jsonObject.has("series") && jsonObject.get("series").isJsonArray()
                    ? jsonObject.getAsJsonArray("series")
                    : null;
            boolean isSingleAlbum = false;
            if (series == null || series.isEmpty() || series.size() == 1) {
                // 该章节就是一个本子
                if (!photoId.equals(seriesId)) {
                    seriesId = photoId;
                    isSingleAlbum = true;
                }
            }

            // API 返回的 tags 是一个空格分隔的字符串
            String tagsString = null;
            if (jsonObject.has("tags") && !jsonObject.get("tags").isJsonNull()) {
                tagsString = jsonObject.get("tags").getAsString();
            }
            List<String> tags = StringUtils.isBlank(tagsString)
                    ? Collections.emptyList()
                    : List.of(tagsString.trim().split("\\s+"));

            JsonArray imagesArray = jsonObject.has("images") && jsonObject.get("images").isJsonArray()
                    ? jsonObject.getAsJsonArray("images")
                    : new JsonArray();

            List<JmImage> images = buildImageList(
                    photoId,
                    scrambleId,
                    // 从默认域名列表中随机选择一个
                    JmConstants.DEFAULT_IMAGE_DOMAINS.get(RANDOM.nextInt(JmConstants.DEFAULT_IMAGE_DOMAINS.size())),
                    imagesArray
            );

            return new JmPhoto(
                    photoId,
                    name,
                    seriesId,
                    scrambleId,
                    parsePhotoSortOrder(series, photoId),
                    "", // API photo 响应中没有 author
                    tags,
                    images,
                    isSingleAlbum
            );
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse photo API JSON", e);
        }
    }

    /**
     * 解析章节在本子内的排序
     *
     * @param seriesArray    序号和章节id组成的数组
     * @param currentPhotoId 章节id
     * @return 章节在本子内的序号
     */
    private static int parsePhotoSortOrder(JsonArray seriesArray, String currentPhotoId) {
        if (seriesArray == null || seriesArray.isEmpty()) {
            return 1; // 默认值为1
        }

        for (JsonElement item : seriesArray) {
            JsonObject node = item.getAsJsonObject();

            // 找到匹配的节点
            if (node.has("id") && !node.get("id").isJsonNull() && Objects.equals(node.get("id").getAsString(), currentPhotoId)) {
                // 获取 sort 值，如果不存在则返回 1
                if (node.has("sort") && !node.get("sort").isJsonNull()) {
                    return node.get("sort").getAsInt();
                }
                return 1;
            }
        }

        // 如果循环结束了还没找到，说明没有匹配的节点，返回默认值 1
        return 1;
    }

    /**
     * 构建图片信息列表
     *
     * @param photoId     章节id
     * @param scrambleId  图片解密的scramble id
     * @param imageDomain 图片域名
     * @param imagesArray 图片文件名数组
     * @return 图片信息列表
     */
    private static List<JmImage> buildImageList(String photoId, String scrambleId, String imageDomain, JsonArray imagesArray) {
        if (imagesArray == null || imagesArray.isEmpty()) {
            return Collections.emptyList();
        }

        List<JmImage> images = new ArrayList<>();
        int sortOrder = 1;
        for (JsonElement item : imagesArray) {
            String filename = item.getAsString();
            String url = String.format("%s%s/media/photos/%s/%s", JmConstants.PROTOCOL_HTTPS, imageDomain, photoId, filename);
            images.add(new JmImage(
                    photoId,
                    scrambleId,
                    filename,
                    url,
                    null, // API的图片URL通常没有查询参数
                    sortOrder++
            ));
        }
        return images;
    }

    /**
     * 解析搜索或分类列表页 (Search/Category Page) 的API JSON响应。
     *
     * @param jsonStr     API返回的JSON字符串。
     * @param currentPage 当前页码。
     * @return 一个 JmSearchPage 对象。
     */
    public static JmSearchPage parseSearchPage(String jsonStr, int currentPage) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();

            // API响应可能直接重定向到一个album，此时content为空
            String redirectAid = null;
            if (jsonObject.has("redirect_aid") && !jsonObject.get("redirect_aid").isJsonNull()) {
                redirectAid = jsonObject.get("redirect_aid").getAsString();
            }

            if (redirectAid != null && !redirectAid.isEmpty()) {
                // 这是一个特殊情况，表示搜索结果只有一个且直接匹配了ID。
                // 我们在这里无法获取完整的 Album 信息，所以返回一个只包含ID的列表，
                // 上层 Client 逻辑需要捕获这种情况并调用 getAlbum。
                JmAlbumMeta meta = new JmAlbumMeta(redirectAid, "", Collections.emptyList(), Collections.emptyList());
                return new JmSearchPage(1, 1, 1, List.of(meta));
            }

            int totalItems = 0;
            if (jsonObject.has("total") && !jsonObject.get("total").isJsonNull()) {
                totalItems = jsonObject.get("total").getAsInt();
            }

            int totalPages = (totalItems == 0)
                    ? 0
                    : (int) Math.ceil((double) totalItems / JmConstants.PAGE_SIZE_SEARCH);

            JsonArray contentArray = jsonObject.has("content") && jsonObject.get("content").isJsonArray()
                    ? jsonObject.getAsJsonArray("content")
                    : new JsonArray();
            List<JmAlbumMeta> content = parseAlbumMetaList(contentArray);

            return new JmSearchPage(currentPage, totalItems, totalPages, content);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse search API JSON", e);
        }
    }

    /**
     * 解析用户收藏夹页面 (Favorite Page) 的API JSON响应。
     *
     * @param jsonStr API返回的JSON字符串。
     * @param query   收藏夹参数
     * @return 一个 JmFavoritePage 对象。
     */
    public static JmFavoritePage parseFavoritePage(String jsonStr, FavoriteQuery query) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();

            int totalItems = 0;
            if (jsonObject.has("total") && !jsonObject.get("total").isJsonNull()) {
                totalItems = jsonObject.get("total").getAsInt();
            }
            final int pageSize = JmConstants.PAGE_SIZE_FAVORITE;
            int totalPages = (totalItems == 0)
                    ? 0
                    : (int) Math.ceil((double) totalItems / pageSize);

            JsonArray folderArray = jsonObject.has("folder_list") && jsonObject.get("folder_list").isJsonArray()
                    ? jsonObject.getAsJsonArray("folder_list")
                    : null;
            Map<String, String> folderList;
            if (folderArray == null || folderArray.isEmpty()) {
                folderList = new HashMap<>();
                folderList.putIfAbsent("0", "全部");
            } else {
                folderList = new HashMap<>();
                folderList.putIfAbsent("0", "全部");
                for (JsonElement item : folderArray) {
                    JsonObject node = item.getAsJsonObject();

                    String fid = "";
                    if (node.has("FID") && !node.get("FID").isJsonNull()) {
                        fid = StringUtils.defaultIfBlank(node.get("FID").getAsString(), "");
                    }

                    String name = "";
                    if (node.has("name") && !node.get("name").isJsonNull()) {
                        name = StringUtils.defaultIfBlank(node.get("name").getAsString(), "");
                    }

                    // 如果 fid 有效，则尝试放入 map
                    if (!fid.isEmpty()) {
                        folderList.putIfAbsent(fid, name);
                    }
                }
            }

            JsonArray listArray = jsonObject.has("list") && jsonObject.get("list").isJsonArray()
                    ? jsonObject.getAsJsonArray("list")
                    : new JsonArray();
            List<JmAlbumMeta> content = parseAlbumMetaList(listArray);

            return new JmFavoritePage(folderList.get(String.valueOf(query.getFolderId())), query.getFolderId(), query.getPage(), totalItems, totalPages, content, folderList);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse favorite API JSON", e);
        }
    }

    /**
     * 从一个包含本子摘要信息的JSON数组节点中解析出 List<JmAlbumMeta>
     *
     * @param array 本子摘要信息数组
     */
    private static List<JmAlbumMeta> parseAlbumMetaList(JsonArray array) {
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }

        List<JmAlbumMeta> resultList = new ArrayList<>();
        for (JsonElement item : array) {
            JsonObject node = item.getAsJsonObject();
            List<String> authors = new ArrayList<>();

            if (node.has("author") && !node.get("author").isJsonNull()) {
                JsonElement authorElement = node.get("author");
                // API返回的 author 可能是字符串或数组，这里做兼容处理
                if (authorElement.isJsonPrimitive() && authorElement.getAsJsonPrimitive().isString()) {
                    authors.add(authorElement.getAsString());
                } else if (authorElement.isJsonArray()) {
                    for (JsonElement authorItem : authorElement.getAsJsonArray()) {
                        authors.add(authorItem.getAsString());
                    }
                }
            }

            String id = "";
            if (node.has("id") && !node.get("id").isJsonNull()) {
                id = node.get("id").getAsString();
            }

            String name = "";
            if (node.has("name") && !node.get("name").isJsonNull()) {
                name = node.get("name").getAsString();
            }

            JmAlbumMeta meta = new JmAlbumMeta(
                    StringUtils.defaultIfBlank(id, ""),
                    StringUtils.defaultIfBlank(name, ""),
                    authors,
                    Collections.emptyList()
            );
            resultList.add(meta);
        }
        return resultList;
    }

    /**
     * 从域名服务器的响应JSON中解析出域名列表
     *
     * @param jsonStr 域名服务器返回的、解密后的JSON字符串
     * @return 域名列表
     */
    public static List<String> parseDomainsFromDomainServer(String jsonStr) {
        try {
            JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();
            JsonArray serverArray = root.has("Server") && root.get("Server").isJsonArray()
                    ? root.getAsJsonArray("Server")
                    : null;

            if (serverArray == null || serverArray.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> resultList = new ArrayList<>();
            for (JsonElement item : serverArray) {
                resultList.add(item.getAsString());
            }
            return resultList;

        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse domain server response JSON", e);
        }
    }
}
