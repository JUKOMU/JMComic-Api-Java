package io.github.jukomu.jmcomic.core.parser;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import io.github.jukomu.jmcomic.api.exception.ParseResponseException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            JSONObject jsonObject = JSONObject.parseObject(json);

            // API返回的 "author" 字段实际是作者列表
            String albumId = StringUtils.defaultIfBlank(jsonObject.getString("id"), "");
            String name = StringUtils.defaultIfBlank(jsonObject.getString("name"), "");
            String description = StringUtils.defaultIfBlank(jsonObject.getString("description"), "");
            String likes = StringUtils.defaultIfBlank(jsonObject.getString("likes"), "0");
            String totalViews = StringUtils.defaultIfBlank(jsonObject.getString("total_views"), "0");
            int comment_total = jsonObject.getIntValue("comment_total", 0);
            List<String> authors = jsonObject.getList("author", String.class);
            List<String> works = jsonObject.getList("works", String.class);
            List<String> actors = jsonObject.getList("actors", String.class);
            List<String> tags = jsonObject.getList("tags", String.class);
            List<JmAlbumMeta> relatedAlbums = parseRelatedAlbums(jsonObject.getJSONArray("related_list"));
            List<JmPhotoMeta> photoMetas = parsePhotoMetas(jsonObject.getJSONArray("series"), albumId, name);

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
        } catch (JSONException e) {
            throw new ParseResponseException("Failed to parse album API JSON", e);
        }
    }

    private static List<JmAlbumMeta> parseRelatedAlbums(JSONArray relatedListArray) {
        if (relatedListArray == null || relatedListArray.isEmpty()) {
            return Collections.emptyList();
        }

        return relatedListArray.stream()
                .map(item -> (JSONObject) item)
                .map(node -> new JmAlbumMeta(
                        StringUtils.defaultIfBlank(node.getString("id"), ""),
                        StringUtils.defaultIfBlank(node.getString("name"), ""),
                        List.of(StringUtils.defaultIfBlank(node.getString("author"), "")),
                        Collections.emptyList()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 从一个包含章节摘要信息的JSON数组节点中解析出 List<JmPhotoMeta>
     *
     * @param seriesArray 章节摘要信息数组
     * @param albumId     章节所属本子id
     * @param albumName   章节所属本子名字
     */
    private static List<JmPhotoMeta> parsePhotoMetas(JSONArray seriesArray, String albumId, String albumName) {
        if (seriesArray == null || seriesArray.isEmpty()) {
            // 单章本, series 列表为空
            return List.of(new JmPhotoMeta(albumId, albumName, 1));
        }

        return seriesArray.stream()
                .map(item -> (JSONObject) item)
                .map(node -> new JmPhotoMeta(
                        StringUtils.defaultIfBlank(node.getString("id"), ""),
                        StringUtils.defaultIfBlank(node.getString("name"), ""),
                        node.getIntValue("sort", 0)
                ))
                .collect(Collectors.toList());
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
            JSONObject jsonObject = JSONObject.parseObject(json);

            String photoId = StringUtils.defaultIfBlank(jsonObject.getString("id"), "");
            String name = StringUtils.defaultIfBlank(jsonObject.getString("name"), "");
            String seriesId = StringUtils.defaultIfBlank(jsonObject.getString("series_id"), "");
            JSONArray series = jsonObject.getJSONArray("series");
            boolean isSingleAlbum = false;
            if (series == null || series.isEmpty() || series.size() == 1) {
                // 该章节就是一个本子
                if (!photoId.equals(seriesId)) {
                    seriesId = photoId;
                    isSingleAlbum = true;
                }
            }

            // API 返回的 tags 是一个空格分隔的字符串
            String tagsString = jsonObject.getString("tags");
            List<String> tags = StringUtils.isBlank(tagsString)
                    ? Collections.emptyList()
                    : List.of(tagsString.trim().split("\\s+"));

            List<JmImage> images = buildImageList(
                    photoId,
                    scrambleId,
                    // 从默认域名列表中随机选择一个
                    JmConstants.DEFAULT_IMAGE_DOMAINS.get(RANDOM.nextInt(JmConstants.DEFAULT_IMAGE_DOMAINS.size())),
                    jsonObject.getJSONArray("images")
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
        } catch (JSONException e) {
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
    private static int parsePhotoSortOrder(JSONArray seriesArray, String currentPhotoId) {
        if (seriesArray == null || seriesArray.isEmpty()) {
            return 1; // 默认值为1
        }

        for (Object item : seriesArray) {
            JSONObject node = (JSONObject) item;

            // 找到匹配的节点
            if (Objects.equals(node.getString("id"), currentPhotoId)) {
                // 获取 sort 值，如果不存在则返回 1
                Integer sort = node.getInteger("sort");
                return sort == null ? 1 : sort;
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
    private static List<JmImage> buildImageList(String photoId, String scrambleId, String imageDomain, JSONArray imagesArray) {
        if (imagesArray == null || imagesArray.isEmpty()) {
            return Collections.emptyList();
        }

        List<JmImage> images = new ArrayList<>();
        int sortOrder = 1;
        for (Object item : imagesArray) {
            String filename = (String) item;
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
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);

            // API响应可能直接重定向到一个album，此时content为空
            String redirectAid = jsonObject.getString("redirect_aid");
            if (redirectAid != null && !redirectAid.isEmpty()) {
                // 这是一个特殊情况，表示搜索结果只有一个且直接匹配了ID。
                // 我们在这里无法获取完整的 Album 信息，所以返回一个只包含ID的列表，
                // 上层 Client 逻辑需要捕获这种情况并调用 getAlbum。
                JmAlbumMeta meta = new JmAlbumMeta(redirectAid, "", Collections.emptyList(), Collections.emptyList());
                return new JmSearchPage(1, 1, 1, List.of(meta));
            }

            int totalItems = jsonObject.getIntValue("total", 0);
            int totalPages = (totalItems == 0)
                    ? 0
                    : (int) Math.ceil((double) totalItems / JmConstants.PAGE_SIZE_SEARCH);

            List<JmAlbumMeta> content = parseAlbumMetaList(jsonObject.getJSONArray("content"));

            return new JmSearchPage(currentPage, totalItems, totalPages, content);
        } catch (JSONException e) {
            throw new ParseResponseException("Failed to parse search API JSON", e);
        }
    }

    /**
     * 解析用户收藏夹页面 (Favorite Page) 的API JSON响应。
     *
     * @param jsonStr     API返回的JSON字符串。
     * @param currentPage 当前页码。
     * @return 一个 JmFavoritePage 对象。
     */
    public static JmFavoritePage parseFavoritePage(String jsonStr, int currentPage) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);

            int totalItems = jsonObject.getIntValue("total", 0);
            final int pageSize = JmConstants.PAGE_SIZE_FAVORITE;
            int totalPages = (totalItems == 0)
                    ? 0
                    : (int) Math.ceil((double) totalItems / pageSize);

            JSONArray folderArray = jsonObject.getJSONArray("folder_list");
            Map<String, String> folderList;
            if (folderArray == null || folderArray.isEmpty()) {
                folderList = Collections.emptyMap();
            } else {
                folderList = new HashMap<>();
                for (Object item : folderArray) {
                    JSONObject node = (JSONObject) item;

                    String fid = StringUtils.defaultIfBlank(node.getString("FID"), "");
                    String name = StringUtils.defaultIfBlank(node.getString("name"), "");

                    // 如果 fid 有效，则尝试放入 map
                    if (!fid.isEmpty()) {
                        folderList.putIfAbsent(fid, name);
                    }
                }
            }

            List<JmAlbumMeta> content = parseAlbumMetaList(jsonObject.getJSONArray("list"));

            return new JmFavoritePage(currentPage, totalPages, content, folderList);
        } catch (JSONException e) {
            throw new ParseResponseException("Failed to parse favorite API JSON", e);
        }
    }

    /**
     * 从一个包含本子摘要信息的JSON数组节点中解析出 List<JmAlbumMeta>
     *
     * @param array 本子摘要信息数组
     */
    private static List<JmAlbumMeta> parseAlbumMetaList(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }

        List<JmAlbumMeta> resultList = new ArrayList<>();
        for (Object item : array) {
            JSONObject node = (JSONObject) item;
            List<String> authors = new ArrayList<>();
            Object authorObj = node.get("author");
            // API返回的 author 可能是字符串或数组，这里做兼容处理
            if (authorObj instanceof String) {
                authors.add((String) authorObj);
            } else if (authorObj instanceof JSONArray authorArray) {
                for (Object authorItem : authorArray) {
                    authors.add(authorItem.toString());
                }
            }

            JmAlbumMeta meta = new JmAlbumMeta(
                    StringUtils.defaultIfBlank(node.getString("id"), ""),
                    StringUtils.defaultIfBlank(node.getString("name"), ""),
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
            JSONObject root = JSONObject.parseObject(jsonStr);
            JSONArray serverArray = root.getJSONArray("Server");

            if (serverArray == null || serverArray.isEmpty()) {
                return Collections.emptyList();
            }

            return serverArray.toJavaList(String.class);

        } catch (JSONException e) {
            throw new ParseResponseException("Failed to parse domain server response JSON", e);
        }
    }
}
