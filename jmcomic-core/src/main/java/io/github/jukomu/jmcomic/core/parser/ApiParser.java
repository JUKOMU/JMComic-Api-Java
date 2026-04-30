package io.github.jukomu.jmcomic.core.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jukomu.jmcomic.api.exception.ParseResponseException;
import io.github.jukomu.jmcomic.api.exception.ResponseException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

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

            String addTime = "";
            if (jsonObject.has("addtime") && !jsonObject.get("addtime").isJsonNull()) {
                addTime = StringUtils.defaultIfBlank(jsonObject.get("addtime").getAsString(), "");
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
            List<JmAlbumMeta> relatedAlbums = parseAlbumMetaList(relatedListArray);

            JsonArray seriesArray = jsonObject.has("series") && jsonObject.get("series").isJsonArray()
                    ? jsonObject.getAsJsonArray("series")
                    : new JsonArray();
            List<JmPhotoMeta> photoMetas = parsePhotoMetas(seriesArray, albumId, name);

            // series_id: "0" 表示单行本，非 "0" 表示多章节本子
            String seriesId = StringUtils.defaultIfBlank(getString(jsonObject, "series_id"), "0");

            // is_favorite: 当前用户是否已收藏
            boolean isFavorite = jsonObject.has("is_favorite")
                    && !jsonObject.get("is_favorite").isJsonNull()
                    && jsonObject.get("is_favorite").getAsBoolean();

            // liked: 当前用户是否已点赞
            boolean liked = jsonObject.has("liked")
                    && !jsonObject.get("liked").isJsonNull()
                    && jsonObject.get("liked").getAsBoolean();

            // is_aids: 是否为成人内容标识
            boolean isAids = jsonObject.has("is_aids")
                    && !jsonObject.get("is_aids").isJsonNull()
                    && jsonObject.get("is_aids").getAsBoolean();

            // price: 价格，通常为空字符串
            String price = StringUtils.defaultIfBlank(getString(jsonObject, "price"), "");

            // purchased: 是否已购买，通常为空字符串
            String purchased = StringUtils.defaultIfBlank(getString(jsonObject, "purchased"), "");

            String image = StringUtils.defaultIfBlank(getString(jsonObject, "image"), "");

            return new JmAlbum(
                    albumId,
                    name,
                    description,
                    "0", // API Album 响应中没有 scramble_id，使用默认值
                    addTime,
                    0,   // API Album 响应中没有 page_count
                    likes,
                    totalViews,
                    comment_total,
                    image,
                    parseCategoryMeta(jsonObject, "category"),
                    parseCategoryMeta(jsonObject, "category_sub"),
                    authors,
                    works,
                    actors,
                    tags,
                    relatedAlbums,
                    photoMetas,
                    seriesId,
                    isFavorite,
                    liked,
                    isAids,
                    Collections.emptyList(), // images — 仅 getComicRead 填充
                    price,
                    purchased
            );
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse album API JSON", e);
        }
    }

    /**
     * 解析漫画阅读页 (Comic Read Page) 的 API JSON 响应。
     * 从 /comic_read 端点获取数据，包含图片列表和 scramble_id。
     *
     * @param json API 返回的 JSON 字符串
     * @return 一个 JmAlbum 对象，已填充 images 和 scrambleId
     */
    public static JmAlbum parseComicRead(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            String albumId = StringUtils.defaultIfBlank(getString(jsonObject, "id"), "");
            String name = StringUtils.defaultIfBlank(getString(jsonObject, "name"), "");

            // addtime: Unix 时间戳字符串
            String addTime = StringUtils.defaultIfBlank(getString(jsonObject, "addtime"), "");

            // series_id: "0" 表示单行本
            String seriesId = StringUtils.defaultIfBlank(getString(jsonObject, "series_id"), "0");

            // scramble_id: 图片解密的 scramble ID
            String scrambleId = getString(jsonObject, "scramble_id");
            if (scrambleId == null || scrambleId.isBlank()) {
                scrambleId = String.valueOf(JmConstants.SCRAMBLE_220980);
            }

            // total_page: 该阅读页的总页数
            int pageCount = 0;
            if (jsonObject.has("total_page") && !jsonObject.get("total_page").isJsonNull()) {
                pageCount = jsonObject.get("total_page").getAsInt();
            }

            // is_favorite
            boolean isFavorite = jsonObject.has("is_favorite")
                    && !jsonObject.get("is_favorite").isJsonNull()
                    && jsonObject.get("is_favorite").getAsBoolean();

            // liked
            boolean liked = jsonObject.has("liked")
                    && !jsonObject.get("liked").isJsonNull()
                    && jsonObject.get("liked").getAsBoolean();

            // 解析 images 数组 → List<JmImage>
            JsonArray imagesArray = jsonObject.has("images") && jsonObject.get("images").isJsonArray()
                    ? jsonObject.getAsJsonArray("images")
                    : new JsonArray();
            String imageDomain = JmConstants.DEFAULT_IMAGE_DOMAINS.get(
                    RANDOM.nextInt(JmConstants.DEFAULT_IMAGE_DOMAINS.size()));
            List<JmImage> images = buildImageList(albumId, scrambleId, imageDomain, imagesArray);

            // 构建 photoMetas: comic_read 的 series 数组格式与 album 相同
            List<JmPhotoMeta> photoMetas;
            if (jsonObject.has("series") && jsonObject.get("series").isJsonArray()) {
                JsonArray seriesArray = jsonObject.getAsJsonArray("series");
                if (!seriesArray.isEmpty()) {
                    photoMetas = parsePhotoMetas(seriesArray, albumId, name);
                } else {
                    photoMetas = List.of(new JmPhotoMeta(albumId, name, 1));
                }
            } else {
                photoMetas = List.of(new JmPhotoMeta(albumId, name, 1));
            }

            return new JmAlbum(
                    albumId,
                    name,
                    "",              // description — comic_read 不提供
                    scrambleId,
                    addTime,
                    pageCount,
                    "0",             // likes — comic_read 不提供
                    "0",             // views — comic_read 不提供
                    0,               // commentCount — comic_read 不提供
                    "",              // image — comic_read 不提供
                    null,            // category — comic_read 不提供
                    null,            // subCategory — comic_read 不提供
                    Collections.emptyList(), // authors — comic_read 返回格式不同
                    Collections.emptyList(), // works
                    Collections.emptyList(), // actors
                    Collections.emptyList(), // tags — 格式不同
                    Collections.emptyList(), // relatedAlbums
                    photoMetas,
                    seriesId,
                    isFavorite,
                    liked,
                    false,           // isAids — comic_read 不提供
                    images,
                    "",              // price — comic_read 不提供
                    ""               // purchased — comic_read 不提供
            );
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse comic_read API JSON", e);
        }
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

            // 检测 API 返回的错误响应（如 {"error": "参数错误"}）
            if (jsonObject.has("error") && !jsonObject.get("error").isJsonNull()) {
                String errorMsg = jsonObject.get("error").getAsString();
                throw new ParseResponseException("API returned error: " + errorMsg);
            }

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
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse search API JSON", e);
        }
    }

    /**
     * 解析最新上架/推广列表 (Latest/Promote List) 的API JSON响应。
     * 这些 API 返回的是裸 JSON 数组 [{...}, {...}]，而非包装对象。
     *
     * @param jsonStr     API返回的JSON字符串（裸数组）。
     * @param currentPage 当前页码。
     * @return 一个 JmSearchPage 对象。
     */
    public static JmSearchPage parseLatestOrPromoteList(String jsonStr, int currentPage) {
        try {
            JsonArray array = JsonParser.parseString(jsonStr).getAsJsonArray();
            List<JmAlbumMeta> list = parseAlbumMetaList(array);
            int totalItems = list.size();
            int totalPages = totalItems == 0
                    ? 0
                    : (int) Math.ceil((double) totalItems / JmConstants.PAGE_SIZE_SEARCH);
            // 当返回数据量等于页大小时，至少多给一页，让调用方知道可能还有更多
            if (totalItems >= JmConstants.PAGE_SIZE_SEARCH) {
                totalPages = Math.max(totalPages, currentPage + 1);
            }
            return new JmSearchPage(currentPage, totalItems, totalPages, list);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse latest/promote list API JSON", e);
        }
    }

    /**
     * 解析观看历史 (Watch History) 的API JSON响应。
     * <p>
     * 该接口返回的数据结构为 {@code {"list": [...], "total": N}}，
     * 每条记录与 JmAlbumMeta 结构一致。
     *
     * @param jsonStr API 返回的 JSON 字符串
     * @return 观看历史列表（List of JmAlbumMeta）
     */
    public static List<JmAlbumMeta> parseHistoryList(String jsonStr) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();

            if (jsonObject.has("error") && !jsonObject.get("error").isJsonNull()) {
                String errorMsg = jsonObject.get("error").getAsString();
                throw new ParseResponseException("API returned error: " + errorMsg);
            }

            JsonArray listArray = jsonObject.has("list") && jsonObject.get("list").isJsonArray()
                    ? jsonObject.getAsJsonArray("list")
                    : new JsonArray();
            return parseAlbumMetaList(listArray);
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse history API JSON", e);
        }
    }

    /**
     * 解析随机推荐列表 (Random Recommend) 的API JSON响应。
     * 该接口返回的是裸 JSON 数组 [{...}, {...}]，每条记录与 JmAlbumMeta 结构一致。
     *
     * @param jsonStr API返回的JSON字符串（裸数组）。
     * @return 一个 JmAlbumMeta 列表。
     */
    public static List<JmAlbumMeta> parseRandomRecommendList(String jsonStr) {
        try {
            JsonArray array = JsonParser.parseString(jsonStr).getAsJsonArray();
            return parseAlbumMetaList(array);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse random recommend API JSON", e);
        }
    }

    /**
     * 解析收藏标签列表的API JSON响应。
     * 该接口返回结构为 {"list": [{"tag": "...", "updated_at": "..."}, ...]}。
     *
     * @param jsonStr API返回的JSON字符串。
     * @return 一个 JmTagFavorite 列表。
     */
    public static List<JmTagFavorite> parseTagFavoriteList(String jsonStr) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();

            JsonArray listArray = jsonObject.has("list") && jsonObject.get("list").isJsonArray()
                    ? jsonObject.getAsJsonArray("list")
                    : new JsonArray();

            List<JmTagFavorite> result = new ArrayList<>();
            for (JsonElement item : listArray) {
                JsonObject node = item.getAsJsonObject();
                String tag = getString(node, "tag");
                String updatedAt = getString(node, "updated_at");
                result.add(new JmTagFavorite(
                        StringUtils.defaultIfBlank(tag, ""),
                        StringUtils.defaultIfBlank(updatedAt, "")
                ));
            }
            return result;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse tag favorite list API JSON", e);
        }
    }

    /**
     * 解析分类列表的API JSON响应。
     * 该接口返回结构为 {"categories": [...], "blocks": [...]}。
     *
     * @param jsonStr API返回的JSON字符串。
     * @return 一个 JmCategoryList 对象。
     */
    public static JmCategoryList parseCategoryList(String jsonStr) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();

            // 解析 categories 数组
            JsonArray categoriesArray = jsonObject.has("categories") && jsonObject.get("categories").isJsonArray()
                    ? jsonObject.getAsJsonArray("categories")
                    : new JsonArray();
            List<JmCategoryListItem> categories = new ArrayList<>();
            for (JsonElement item : categoriesArray) {
                JsonObject node = item.getAsJsonObject();
                String id = getJsonFieldAsString(node, "id");
                String name = getString(node, "name");
                String slug = getString(node, "slug");
                String type = getString(node, "type");
                String totalAlbums = getJsonFieldAsString(node, "total_albums");

                // 解析 sub_categories 数组
                List<JmSubCategoryItem> subCategories = new ArrayList<>();
                if (node.has("sub_categories") && node.get("sub_categories").isJsonArray()) {
                    for (JsonElement subItem : node.getAsJsonArray("sub_categories")) {
                        JsonObject subNode = subItem.getAsJsonObject();
                        subCategories.add(new JmSubCategoryItem(
                                getJsonFieldAsString(subNode, "CID"),
                                StringUtils.defaultIfBlank(getString(subNode, "name"), ""),
                                StringUtils.defaultIfBlank(getString(subNode, "slug"), "")
                        ));
                    }
                }

                categories.add(new JmCategoryListItem(
                        StringUtils.defaultIfBlank(id, ""),
                        StringUtils.defaultIfBlank(name, ""),
                        StringUtils.defaultIfBlank(slug, ""),
                        StringUtils.defaultIfBlank(type, null),
                        StringUtils.defaultIfBlank(totalAlbums, "0"),
                        subCategories
                ));
            }

            // 解析 blocks 数组
            JsonArray blocksArray = jsonObject.has("blocks") && jsonObject.get("blocks").isJsonArray()
                    ? jsonObject.getAsJsonArray("blocks")
                    : new JsonArray();
            List<JmCategoryBlock> blocks = new ArrayList<>();
            for (JsonElement item : blocksArray) {
                JsonObject node = item.getAsJsonObject();
                String title = StringUtils.defaultIfBlank(getString(node, "title"), "");

                List<String> content = new ArrayList<>();
                if (node.has("content") && node.get("content").isJsonArray()) {
                    for (JsonElement contentItem : node.getAsJsonArray("content")) {
                        content.add(contentItem.getAsString());
                    }
                }

                blocks.add(new JmCategoryBlock(title, content));
            }

            return new JmCategoryList(categories, blocks);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse category list API JSON", e);
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

            JmAlbumMeta meta = new JmAlbumMeta(
                    StringUtils.defaultIfBlank(getString(node, "id"), ""),
                    StringUtils.defaultIfBlank(getString(node, "name"), ""),
                    authors,
                    Collections.emptyList(),
                    StringUtils.defaultIfBlank(getString(node, "description"), ""),
                    StringUtils.defaultIfBlank(getString(node, "image"), ""),
                    parseCategoryMeta(node, "category"),
                    parseCategoryMeta(node, "category_sub")
            );
            resultList.add(meta);
        }
        return resultList;
    }

    /**
     * 解析本子下载信息的API JSON响应
     *
     * @param json API返回的JSON字符串
     * @return 一个 JmAlbumDownloadInfo 对象
     */
    public static JmAlbumDownloadInfo parseAlbumDownloadInfo(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return new JmAlbumDownloadInfo(
                    getString(jsonObject, "title"),
                    getString(jsonObject, "fileSize"),
                    getString(jsonObject, "download_url"),
                    getString(jsonObject, "img_url")
            );
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse album download info API JSON", e);
        }
    }

    private static String getString(JsonObject node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isJsonNull()) {
            return null;
        }
        return node.get(fieldName).getAsString();
    }

    private static JmCategoryMeta parseCategoryMeta(JsonObject node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isJsonNull()) {
            return null;
        }

        JsonObject categoryNode = node.getAsJsonObject(fieldName);
        return new JmCategoryMeta(
                getString(categoryNode, "id"),
                getString(categoryNode, "title")
        );
    }

    // == 论坛/评论/点赞/收藏夹扩展解析 ==

    /**
     * 解析评论列表的API JSON响应
     *
     * @param json API返回的JSON字符串，格式: {"list": [...], "total": N}
     * @return 一个 JmCommentList 对象
     */
    public static JmCommentList parseCommentList(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            int total = 0;
            if (jsonObject.has("total") && !jsonObject.get("total").isJsonNull()) {
                total = jsonObject.get("total").getAsInt();
            }

            JsonArray listArray = jsonObject.has("list") && jsonObject.get("list").isJsonArray()
                    ? jsonObject.getAsJsonArray("list")
                    : new JsonArray();
            List<JmComment> comments = new ArrayList<>();
            for (JsonElement item : listArray) {
                comments.add(parseForumComment(item.getAsJsonObject()));
            }

            return new JmCommentList(total, comments);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse comment list API JSON", e);
        }
    }

    /**
     * 解析论坛评论列表中的单条评论
     */
    private static JmComment parseForumComment(JsonObject node) {
        String cid = getJsonFieldAsString(node, "CID");
        String userId = getJsonFieldAsString(node, "UID");
        String nickname = getJsonFieldAsString(node, "nickname");
        String username = StringUtils.defaultIfBlank(
                getJsonFieldAsString(node, "username"),
                nickname
        );
        String contentHtml = getJsonFieldAsString(node, "content");
        String content = extractCommentContent(contentHtml);
        String addtime = getJsonFieldAsString(node, "addtime");
        String photo = getJsonFieldAsString(node, "photo");
        String expinfo = getJsonFieldAsString(node, "expinfo");
        JmCommentExpInfo expinfoData = parseCommentExpinfo(node.get("expinfo"));
        String aid = getJsonFieldAsString(node, "AID");
        String bid = getJsonFieldAsString(node, "BID");
        String nid = getJsonFieldAsString(node, "NID");
        String ncid = getJsonFieldAsString(node, "NCID");
        String name = getJsonFieldAsString(node, "name");
        String gender = getJsonFieldAsString(node, "gender");
        String updateAt = getJsonFieldAsString(node, "update_at");
        String parentCommentId = getJsonFieldAsString(node, "parent_CID");
        String spoiler = getJsonFieldAsString(node, "spoiler");

        // 构建完整的用户头像URL: https://{imageDomain}/media/users/{photo}
        String avatarUrl = StringUtils.isNotBlank(photo)
                ? JmConstants.PROTOCOL_HTTPS
                  + JmConstants.DEFAULT_IMAGE_DOMAINS.get(RANDOM.nextInt(JmConstants.DEFAULT_IMAGE_DOMAINS.size()))
                  + "/media/users/" + photo
                : "";

        int likes = 0;
        if (node.has("likes") && !node.get("likes").isJsonNull()) {
            likes = safeParseInt(node.get("likes").getAsString());
        }

        int voteUp = 0;
        if (node.has("vote_up") && !node.get("vote_up").isJsonNull()) {
            voteUp = node.get("vote_up").getAsInt();
        }

        int voteDown = 0;
        if (node.has("vote_down") && !node.get("vote_down").isJsonNull()) {
            voteDown = node.get("vote_down").getAsInt();
        }

        // 解析子评论/回复列表
        List<JmComment> replys = new ArrayList<>();
        if (node.has("replys") && node.get("replys").isJsonArray()) {
            for (JsonElement replyItem : node.getAsJsonArray("replys")) {
                replys.add(parseForumComment(replyItem.getAsJsonObject()));
            }
        }

        return new JmComment(
                cid,
                userId,
                username,
                nickname,
                content,
                contentHtml,
                addtime,
                avatarUrl,
                expinfo,
                expinfoData,
                aid,
                bid,
                nid,
                ncid,
                name,
                likes,
                gender,
                updateAt,
                parentCommentId,
                spoiler,
                replys,
                voteUp,
                voteDown
        );
    }

    private static String extractCommentContent(String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }
        return StringUtils.defaultIfBlank(Jsoup.parseBodyFragment(html).text(), "");
    }

    private static JmCommentExpInfo parseCommentExpinfo(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return JmCommentExpInfo.empty("");
        }

        if (!element.isJsonObject()) {
            return JmCommentExpInfo.empty(getJsonElementAsString(element));
        }

        JsonObject expinfo = element.getAsJsonObject();
        List<String> badges = new ArrayList<>();
        if (expinfo.has("badges") && expinfo.get("badges").isJsonArray()) {
            for (JsonElement badge : expinfo.getAsJsonArray("badges")) {
                badges.add(getJsonElementAsString(badge));
            }
        }

        return new JmCommentExpInfo(
                expinfo.toString(),
                getJsonFieldAsString(expinfo, "level_name"),
                getJsonFieldAsInteger(expinfo, "level"),
                getJsonFieldAsInteger(expinfo, "nextLevelExp"),
                getJsonFieldAsString(expinfo, "exp"),
                getJsonFieldAsDouble(expinfo, "expPercent"),
                getJsonFieldAsString(expinfo, "uid"),
                badges
        );
    }

    /**
     * 兼容 API 字段在字符串、数字、布尔、对象、数组之间切换的情况。
     * 对于对象/数组，保留其 JSON 字符串，避免解析阶段直接抛异常。
     */
    private static String getJsonFieldAsString(JsonObject node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return "";
        }

        JsonElement element = node.get(fieldName);
        if (element == null || element.isJsonNull()) {
            return "";
        }

        if (element.isJsonPrimitive()) {
            return StringUtils.defaultIfBlank(element.getAsString(), "");
        }

        return element.toString();
    }

    private static String getJsonElementAsString(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }

        if (element.isJsonPrimitive()) {
            return StringUtils.defaultIfBlank(element.getAsString(), "");
        }

        return element.toString();
    }

    private static Integer getJsonFieldAsInteger(JsonObject node, String fieldName) {
        String value = getJsonFieldAsString(node, fieldName);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return safeParseInt(value);
    }

    private static Double getJsonFieldAsDouble(JsonObject node, String fieldName) {
        String value = getJsonFieldAsString(node, fieldName);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int safeParseInt(String value) {
        try {
            return Integer.parseInt(StringUtils.defaultIfBlank(value, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 解析收藏夹管理操作的API JSON响应
     *
     * @param json API返回的JSON字符串，格式: {"status": "ok", "msg": "..."}
     * @return 一个 JmFavoriteFolderResult 对象
     */
    public static JmFavoriteFolderResult parseFavoriteFolderResult(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            String status = "";
            if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
                status = StringUtils.defaultIfBlank(jsonObject.get("status").getAsString(), "");
            }

            String msg = "";
            if (jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()) {
                msg = StringUtils.defaultIfBlank(jsonObject.get("msg").getAsString(), "");
            }

            return new JmFavoriteFolderResult(status, msg);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse favorite folder result API JSON", e);
        }
    }

    /**
     * 解析评论投票操作的API JSON响应
     *
     * @param json API返回的JSON字符串
     * @return 一个 JmVoteResult 对象
     * @deprecated 该功能已被 JM 平台停用，服务端返回"评价已停用"
     */
    @Deprecated
    public static JmVoteResult parseVoteResult(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            String status = "";
            if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
                status = StringUtils.defaultIfBlank(jsonObject.get("status").getAsString(), "");
            }

            String msg = "";
            if (jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()) {
                msg = StringUtils.defaultIfBlank(jsonObject.get("msg").getAsString(), "");
            }

            int voteUp = 0;
            if (jsonObject.has("vote_up") && !jsonObject.get("vote_up").isJsonNull()) {
                voteUp = jsonObject.get("vote_up").getAsInt();
            }

            int voteDown = 0;
            if (jsonObject.has("vote_down") && !jsonObject.get("vote_down").isJsonNull()) {
                voteDown = jsonObject.get("vote_down").getAsInt();
            }

            return new JmVoteResult(status, msg, voteUp, voteDown);
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse vote result API JSON", e);
        }
    }

    /**
     * 解析通知列表的API JSON响应。
     * 支持两种格式：
     * 1. 裸数组：[{...}, {...}]（当前API返回格式）
     * 2. 包装对象：{"total": N, "list": [...]}
     */
    public static JmNotificationPage parseNotificationPage(String json) {
        try {
            JsonElement root = JsonParser.parseString(json);
            JsonArray listArray;
            int total;

            if (root.isJsonArray()) {
                listArray = root.getAsJsonArray();
                total = listArray.size();
            } else if (root.isJsonObject()) {
                JsonObject jsonObject = root.getAsJsonObject();
                total = 0;
                if (jsonObject.has("total") && !jsonObject.get("total").isJsonNull()) {
                    total = jsonObject.get("total").getAsInt();
                }
                listArray = jsonObject.has("list") && jsonObject.get("list").isJsonArray()
                        ? jsonObject.getAsJsonArray("list")
                        : new JsonArray();
            } else {
                throw new ParseResponseException("Unexpected notification API response format");
            }

            List<JmNotification> list = new ArrayList<>();
            for (JsonElement item : listArray) {
                JsonObject node = item.getAsJsonObject();

                String id = optString(node, "id", "");

                String title = optString(node, "title", "");

                // API 使用 "date" 字段表示时间
                String time = optString(node, "date", "");
                if (time.isEmpty()) {
                    time = optString(node, "time", "");
                }

                // read 字段可能是 boolean 或 string
                boolean read = false;
                if (node.has("read") && !node.get("read").isJsonNull()) {
                    JsonElement readEl = node.get("read");
                    if (readEl.isJsonPrimitive() && readEl.getAsJsonPrimitive().isBoolean()) {
                        read = readEl.getAsBoolean();
                    } else {
                        read = Boolean.parseBoolean(readEl.getAsString());
                    }
                }

                String type = optString(node, "type", "");

                // content 可能是 string（站点通知）或 array（漫画追更）
                String content = "";
                if (node.has("content") && !node.get("content").isJsonNull()) {
                    JsonElement contentEl = node.get("content");
                    if (contentEl.isJsonArray()) {
                        content = contentEl.getAsJsonArray().toString();
                    } else if (contentEl.isJsonPrimitive()) {
                        content = contentEl.getAsString();
                    } else {
                        content = contentEl.toString();
                    }
                }

                list.add(new JmNotification(id, title, content, time, read, type));
            }

            return new JmNotificationPage(total, list);
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse notification page API JSON", e);
        }
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

    // == 辅助方法 ==

    /**
     * 安全地从 JsonObject 中读取字符串，不存在或为 null 时返回默认值
     */
    /**
     * 解析连载跟踪列表的API JSON响应。
     * <p>
     * 响应格式：
     * <pre>
     * {
     *   "item": [
     *     {"id": "1107113", "name": "...", "image": "...", "update_at": "1777274161"}
     *   ],
     *   "totalCnt": "2"
     * }
     * </pre>
     *
     * @param json 原始JSON字符串
     * @return JmTrackingPage 对象
     */
    public static JmTrackingPage parseTrackingPage(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            String totalCnt = optString(root, "totalCnt", "0");

            List<JmTrackingItem> items = new ArrayList<>();
            if (root.has("item") && root.get("item").isJsonArray()) {
                JsonArray itemArray = root.getAsJsonArray("item");
                for (JsonElement elem : itemArray) {
                    JsonObject node = elem.getAsJsonObject();
                    String id = optString(node, "id", "");
                    String name = optString(node, "name", "");
                    String image = optString(node, "image", "");
                    String updateAt = optString(node, "update_at", "");
                    items.add(new JmTrackingItem(id, name, image, updateAt));
                }
            }

            return new JmTrackingPage(totalCnt, items);
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse tracking list API JSON", e);
        }
    }

    /**
     * 解析任务列表的API JSON响应。
     * <p>
     * 响应格式：
     * <pre>
     * {
     *   "msg": "",
     *   "status": "ok",
     *   "list": [
     *     {
     *       "id": "68", "name": "等级0", "type": "title",
     *       "content": "静默的石头", "coin": "0",
     *       "rule": "{...}",
     *       "begin_time": "2021-01-01 00:00:00",
     *       "end_time": "2080-01-01 00:00:00",
     *       "created_at": "2021-02-22 12:13:45",
     *       "updated_at": null,
     *       "done": true
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param json 原始JSON字符串
     * @return JmTaskList 对象
     */
    public static JmTaskList parseTaskList(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            String msg = optString(root, "msg", "");
            String status = optString(root, "status", "");

            List<JmTaskItem> items = new ArrayList<>();
            if (root.has("list") && root.get("list").isJsonArray()) {
                JsonArray listArray = root.getAsJsonArray("list");
                for (JsonElement elem : listArray) {
                    JsonObject node = elem.getAsJsonObject();
                    items.add(new JmTaskItem(
                            optString(node, "id", ""),
                            optString(node, "name", ""),
                            optString(node, "type", ""),
                            optString(node, "content", ""),
                            optString(node, "coin", ""),
                            optString(node, "rule", ""),
                            optString(node, "begin_time", ""),
                            optString(node, "end_time", ""),
                            optString(node, "created_at", ""),
                            optString(node, "updated_at", ""),
                            !node.has("done") || node.get("done").isJsonNull() ? false : node.get("done").getAsBoolean()
                    ));
                }
            }

            return new JmTaskList(msg, status, items);
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse task list API JSON", e);
        }
    }

    /**
     * 解析每日签到状态的API JSON响应。
     * <p>
     * 响应格式：
     * <pre>
     * {
     *   "daily_id": 67,
     *   "three_days_coin": "150",
     *   "three_days_exp": "150",
     *   "seven_days_coin": "350",
     *   "seven_days_exp": "350",
     *   "event_name": "可以色色",
     *   "background_pc": "/media/logo/pc/2PC.jpg",
     *   "background_phone": "/media/logo/phone/2PH.jpg",
     *   "currentProgress": "0%",
     *   "record": [
     *     [{"date": "01", "signed": false, "bonus": false}, ...],
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param json 原始JSON字符串
     * @return JmDailyCheckInStatus 对象
     */
    public static JmDailyCheckInStatus parseDailyStatus(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            int dailyId = root.has("daily_id") && !root.get("daily_id").isJsonNull()
                    ? root.get("daily_id").getAsInt() : 0;

            String threeDaysCoin = optString(root, "three_days_coin", "0");
            String threeDaysExp = optString(root, "three_days_exp", "0");
            String sevenDaysCoin = optString(root, "seven_days_coin", "0");
            String sevenDaysExp = optString(root, "seven_days_exp", "0");
            String eventName = optString(root, "event_name", "");
            String backgroundPc = optString(root, "background_pc", "");
            String backgroundPhone = optString(root, "background_phone", "");
            String currentProgress = optString(root, "currentProgress", "");

            List<List<JmDailyCheckInRecordItem>> record = new ArrayList<>();
            if (root.has("record") && root.get("record").isJsonArray()) {
                JsonArray weeksArray = root.getAsJsonArray("record");
                for (JsonElement weekElem : weeksArray) {
                    List<JmDailyCheckInRecordItem> week = new ArrayList<>();
                    if (weekElem.isJsonArray()) {
                        JsonArray daysArray = weekElem.getAsJsonArray();
                        for (JsonElement dayElem : daysArray) {
                            JsonObject dayObj = dayElem.getAsJsonObject();
                            String date = optString(dayObj, "date", "");
                            Boolean signed = dayObj.has("signed") && !dayObj.get("signed").isJsonNull()
                                    ? dayObj.get("signed").getAsBoolean() : null;
                            boolean bonus = dayObj.has("bonus") && !dayObj.get("bonus").isJsonNull()
                                    && dayObj.get("bonus").getAsBoolean();
                            week.add(new JmDailyCheckInRecordItem(date, signed, bonus));
                        }
                    }
                    record.add(week);
                }
            }

            return new JmDailyCheckInStatus(dailyId, threeDaysCoin, threeDaysExp,
                    sevenDaysCoin, sevenDaysExp, eventName,
                    backgroundPc, backgroundPhone, currentProgress, record);
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse daily status API JSON", e);
        }
    }

    /**
     * 解析每周必看列表的API JSON响应。
     * <p>
     * 响应格式：
     * <pre>
     * {
     *   "categories": [
     *     {"id": "237", "title": "", "time": "2026第236期04.24 - 04.17"},
     *     ...
     *   ],
     *   "type": [
     *     {"id": "hanman", "title": "韩漫"},
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param json 原始JSON字符串
     * @return JmWeeklyPicksList 对象
     */
    public static JmWeeklyPicksList parseWeeklyList(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            List<JmWeeklyPicksCategory> categories = new ArrayList<>();
            if (root.has("categories") && root.get("categories").isJsonArray()) {
                JsonArray catArray = root.getAsJsonArray("categories");
                for (JsonElement elem : catArray) {
                    JsonObject node = elem.getAsJsonObject();
                    categories.add(new JmWeeklyPicksCategory(
                            optString(node, "id", ""),
                            optString(node, "title", ""),
                            optString(node, "time", "")
                    ));
                }
            }

            List<JmWeeklyPicksType> type = new ArrayList<>();
            if (root.has("type") && root.get("type").isJsonArray()) {
                JsonArray typeArray = root.getAsJsonArray("type");
                for (JsonElement elem : typeArray) {
                    JsonObject node = elem.getAsJsonObject();
                    type.add(new JmWeeklyPicksType(
                            optString(node, "id", ""),
                            optString(node, "title", "")
                    ));
                }
            }

            return new JmWeeklyPicksList(categories, type);
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse weekly list API JSON", e);
        }
    }

    /**
     * 解析每周必看筛选API JSON响应（{total, list}）
     *
     * @param json 原始JSON字符串
     * @return JmWeeklyPicksDetail 对象
     */
    public static JmWeeklyPicksDetail parseWeeklyDetail(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            int total = root.has("total") && !root.get("total").isJsonNull()
                    ? root.get("total").getAsInt()
                    : 0;

            JsonArray listArray = root.has("list") && root.get("list").isJsonArray()
                    ? root.getAsJsonArray("list")
                    : new JsonArray();
            List<JmAlbumMeta> list = parseAlbumMetaList(listArray);

            return new JmWeeklyPicksDetail(total, list);
        } catch (ParseResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse weekly detail API JSON", e);
        }
    }

    /**
     * 解析提交评论/回复的API JSON响应
     *
     * @param json        API返回的JSON字符串
     * @param entityId    评论所属的实体ID
     * @param commentText 评论内容
     * @param username    当前用户名
     * @return 一个 JmComment 对象
     */
    public static JmComment parseCommentSubmitResult(String json, String entityId, String commentText, String username) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            String msg = "";
            if (jsonObject.has("msg") && !jsonObject.get("msg").isJsonNull()) {
                msg = StringUtils.defaultIfBlank(jsonObject.get("msg").getAsString(), "");
            }

            String status = "";
            if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
                status = StringUtils.defaultIfBlank(jsonObject.get("status").getAsString(), "");
            }

            if (!"ok".equalsIgnoreCase(status)) {
                throw new ResponseException("Failed to submit comment: " + msg);
            }

            return new JmComment("", "", username, commentText, "", "", "", entityId, "", List.of(), 0, 0);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse comment submit result API JSON", e);
        }
    }

    /**
     * 解析用户个人资料的API JSON响应
     *
     * @param json API返回的JSON字符串（已解密的 response.data）
     * @return 一个 JmUserProfile 对象
     */
    public static JmUserProfile parseUserProfile(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            return new JmUserProfile(
                    optString(obj, "username", ""),
                    optString(obj, "email", ""),
                    optString(obj, "nickName", ""),
                    optString(obj, "lastName", ""),
                    optString(obj, "firstName", ""),
                    optString(obj, "birthday", ""),
                    optString(obj, "relations", ""),
                    optString(obj, "sexuality", ""),
                    optString(obj, "website", ""),
                    optString(obj, "birthPlace", ""),
                    optString(obj, "city", ""),
                    optString(obj, "country", ""),
                    optString(obj, "occupation", ""),
                    optString(obj, "company", ""),
                    optString(obj, "school", ""),
                    optString(obj, "aboutMe", ""),
                    optString(obj, "infoHere", ""),
                    optString(obj, "collections", ""),
                    optString(obj, "ideal", ""),
                    optString(obj, "erogenic", ""),
                    optString(obj, "favorite", ""),
                    optString(obj, "hate", "")
            );
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse user profile API JSON", e);
        }
    }

    private static String optString(JsonObject obj, String key, String defaultValue) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        JsonElement el = obj.get(key);
        if (el.isJsonPrimitive()) {
            return el.getAsString();
        }
        return el.toString();
    }
}
