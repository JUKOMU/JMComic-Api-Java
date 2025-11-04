package io.github.jukomu.jmcomic.core.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.jukomu.jmcomic.api.exception.ApiResponseException;
import io.github.jukomu.jmcomic.api.exception.ParseResponseException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author JUKOMU
 * @Description: 内部工具类，负责将JMComic的HTML页面解析为Java数据模型
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class HtmlParser {

    private HtmlParser() {
    }

    private static final Pattern PATTERN_B64_DECODE = Pattern.compile("const html = base64DecodeUtf8\\(\"(.+?)\"\\)");
    private static final Pattern PATTERN_SCRIPT_VAR = Pattern.compile("var\\s+%s\\s*=\\s*['\"]?(.+?)['\"]?;");
    private static final Pattern PATTERN_HTML_JM_PUB_DOMAIN = Pattern.compile("[\\w-]+\\.\\w+/?\\w+");
    private static final Pattern PATTERN_JM_DOMAIN = Pattern.compile("^(?:https?://)?(?:[^@\\n]+@)?(?:www\\.)?([^:/\\n?]+)");
    public static final Pattern PATTERN_HTML_ALBUM_VIEWS = Pattern.compile("<span>(.*?)</span>\\n *<span>(次觀看|观看次数|次观看次数|次觀看次數|觀看次數|views)</span>");
    public static final Pattern PATTERN_HTML_SEARCH_TOTAL = Pattern.compile("class=\"text-white\">(\\d+)</span> A漫.");

    /**
     * 解析本子详情页 (Album Page)。
     *
     * @param html 完整的HTML页面内容。
     * @return 一个 JmAlbum 对象。
     */
    public static JmAlbum parseAlbum(String html) {
        String decodedHtml = decodeBase64Html(html);
        Document doc = Jsoup.parse(decodedHtml);

        String id = parseAlbumId(doc);

        return new JmAlbum(
                id,
                ParseHelper.selectFirstText(doc, "h1#book-name", "album title"),
                ParseHelper.selectFirstText(doc, "h2:contains(叙述：), h2:contains(敘述：)", "album description").replace("叙述：", "").trim(),
                extractVarFromScript(doc, "scramble_id"),
                // 日期
                extractDate(doc, "上架日期"),
                extractDate(doc, "更新日期"),
                // 页数
                parsePageCount(doc),
                // 喜欢数
                ParseHelper.selectFirstText(doc, "#albim_likes_" + id, "likes count"),
                // 观看次数
                extractViewsCount(doc),
                // 评论数
                ParseHelper.parseIntOrDefault(ParseHelper.selectFirstText(doc, "#total_video_comments", "comment count"), 0),
                // 作者/作品/演员/标签
                parseTagLikeList(doc, "author", "author"),
                parseTagLikeList(doc, "works", "works"),
                parseTagLikeList(doc, "actor", "actors"),
                parseTagLikeList(doc, "tags", "tags"),
                // 相关作品
                parseRelatedAlbums(doc),
                // 章节列表
                parsePhotoMetas(doc, id)
        );
    }


    /**
     * 有些页面内容是Base64编码的，需要先解码。
     */
    private static String decodeBase64Html(String html) {
        try {
            String b64Content = ParseHelper.extractByRegex(html, PATTERN_B64_DECODE, "base64 content");
            return new String(Base64.getDecoder().decode(b64Content));
        } catch (ParseResponseException e) {
            // 如果没找到Base64内容，说明是普通HTML，直接返回
            return html;
        }
    }

    private static String parseAlbumId(Document doc) {
        // 优先从PC布局的 h2 标签提取
        Element h2Element = doc.selectFirst("div.col-lg-7 h2:contains(禁漫车：), div.col-lg-7 h2:contains(禁漫車：)");
        if (h2Element != null && h2Element.parent() != null) {
            Element parentDiv = h2Element.parent();
            String fullText = parentDiv.text();
            String id = fullText.replaceAll("[^0-9]", "");
            if (!id.isEmpty()) {
                return id;
            }
        }
        // 备用，从移动端布局的 span.number 提取
        Element mobileIdElement = doc.selectFirst("span.number:contains(禁漫车：), pan.number:contains(禁漫車：)");
        if (mobileIdElement != null) {
            return mobileIdElement.text().replaceAll("[^0-9]", "");
        }
        throw new ParseResponseException("Could not parse album id.");
    }

    private static String extractDate(Document doc, String key) {
        // 优先尝试PC端结构
        Element pcDateSpan = doc.selectFirst(String.format("div.col-lg-7 span:contains(%s)", key));
        if (pcDateSpan != null) {
            String[] parts = pcDateSpan.text().split(":");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }

        // 备用移动端结构
        Element mobileDateP = doc.selectFirst(String.format("div.hidden-lg p:contains(%s)", key));
        if (mobileDateP != null) {
            String[] parts = mobileDateP.text().split(":");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }

        // 如果找不到，返回空字符串或抛出异常
        return "";
    }

    private static int parsePageCount(Document doc) {
        // 优先移动端 .pagecount
        Element pageCountSpan = doc.selectFirst("span.pagecount");
        if (pageCountSpan != null) {
            return ParseHelper.parseIntOrDefault(pageCountSpan.text(), 0);
        }

        // 备用PC端，从文本中提取
        Element pcTextContainer = doc.selectFirst("div.col-lg-7 div.p-t-5.p-b-5:contains(页数：), div.col-lg-7 div.p-t-5.p-b-5:contains(頁數：)");
        if (pcTextContainer != null) {
            Matcher matcher = Pattern.compile("页数：(\\d+)").matcher(pcTextContainer.text());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            matcher = Pattern.compile("頁數：(\\d+)").matcher(pcTextContainer.text());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }


    private static String extractViewsCount(Document doc) {
        // 优先PC端结构
        if (PATTERN_HTML_ALBUM_VIEWS.matcher(doc.text()).find()) {
            return PATTERN_HTML_ALBUM_VIEWS.matcher(doc.text()).group(0);
        }
        Element pcContainer = doc.selectFirst("div.col-lg-7 span:has(span:matches(次觀看|观看次数|次观看次数|次觀看次數|觀看次數|views))");
        if (pcContainer != null) {
            // 在这个容器内，第一个 span 就是我们想要的数字
            Element valueElement = pcContainer.selectFirst("span");
            if (valueElement != null && StringUtils.isNotBlank(valueElement.text())) {
                if (PATTERN_HTML_ALBUM_VIEWS.matcher(valueElement.text()).find()) {
                    return PATTERN_HTML_ALBUM_VIEWS.matcher(valueElement.text()).group(0);
                }
            }
        }

        // 备用移动端结构
        Element mobileIconElement = doc.selectFirst("div.hidden-lg i.fa-eye");
        if (mobileIconElement != null && mobileIconElement.nextElementSibling() != null) {
            Element valueElement = mobileIconElement.nextElementSibling();
            if (valueElement != null) {
                return valueElement.text();
            }
        }

        // 如果都找不到，返回 "0"
        return "0";
    }


    private static List<String> parseTagLikeList(Element root, String type, String fieldName) {
        // 这个选择器对PC和移动端都有效
        return ParseHelper.selectAllText(root, String.format("span[data-type='%s'] a", type));
    }

    private static List<JmAlbumMeta> parseRelatedAlbums(Element root) {
        // 选择所有 "相关A漫" 列表下的卡片
        Elements items = root.select("#related_comics div.p-b-15");
        return items.stream()
                .map(item -> {
                    Element link = item.selectFirst("a");
                    Element titleElement = item.selectFirst("h3");
                    Element authorElement = item.selectFirst("div.title-truncate > a");

                    if (link == null || titleElement == null) {
                        return null;
                    }

                    String id = extractIdFromUrl(link.attr("href"));
                    String title = titleElement.text();
                    List<String> authors = (authorElement != null) ? List.of(authorElement.text()) : List.of();

                    // 提取相关作品卡片上的tags
                    List<String> tags = item.select("div.tags a.tag").stream()
                            .map(Element::text)
                            .collect(Collectors.toList());

                    return new JmAlbumMeta(id, title, authors, tags);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<JmPhotoMeta> parsePhotoMetas(Element root, String albumId) {
        Elements items = root.select("div.episode-list-box > ul.episode-ul > li > a");
        if (items.isEmpty()) {
            // 对于单章本，创建一个虚拟的章节指向自身
            String title = ParseHelper.selectFirstText(root, "#book-name", "album title");
            return List.of(new JmPhotoMeta(albumId, title, 1));
        }

        List<JmPhotoMeta> metas = new ArrayList<>();
        for (Element item : items) {
            String title = item.text().replaceAll("第[\\d\\s]+[话話]", "").trim();
            if (title.isEmpty()) {
                // 兼容只有序号没有标题的情况
                title = item.text().trim();
            }
            metas.add(new JmPhotoMeta(
                    item.attr("data-album"),
                    title,
                    ParseHelper.parseIntOrDefault(item.selectFirst("span").text(), metas.size() + 1)
            ));
        }
        // 章节列表可能是倒序的，需要反转
        java.util.Collections.reverse(metas);
        return metas;
    }

    private static String extractVarFromScript(Document doc, String varName) {
        Pattern pattern = Pattern.compile(String.format(PATTERN_SCRIPT_VAR.pattern(), varName));
        return doc.select("script:not([src])")
                .stream()
                .map(Element::html)
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .findFirst()
                .orElse("");
    }

    private static String extractIdFromUrl(String url) {
        // 从 "/album/12345" 或 "/photo/12345" 中提取ID
        Pattern pattern = Pattern.compile("/(?:album|photo)/(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 解析章节详情页 (Photo Page)。
     *
     * @param html 完整的HTML页面内容。
     * @return 一个 JmPhoto 对象。
     */
    public static JmPhoto parsePhoto(String html) {
        Document doc = Jsoup.parse(html);

        // 从 <script> 标签中提取JS变量
        String photoId = extractVarFromScript(doc, "aid");
        String scrambleId = extractVarFromScript(doc, "scramble_id");
        String seriesId = extractVarFromScript(doc, "series_id");
        int sortOrder = ParseHelper.parseIntOrDefault(extractVarFromScript(doc, "sort"), 1);
        String pageArrJson = extractVarFromScript(doc, "page_arr");
        boolean isSingleAlbum = false;
        if ("0".equals(seriesId)) {
            seriesId = photoId;
            isSingleAlbum = true;
        }

        // 从 <meta> 和 <title> 标签中提取信息
        String title = parsePhotoTitle(doc);
        List<String> tags = parsePhotoTags(doc);

        // 从图片标签中提取图片域名和查询参数
        String imageUrlTemplate = parseImageUrlTemplate(doc, photoId);
        String queryParams = extractQueryParamsFromUrl(
                doc.selectFirst("img#album_photo_0, img[data-page='0']").attr("data-original")
        );

        List<JmImage> images = buildImageList(photoId, scrambleId, imageUrlTemplate, queryParams, pageArrJson);

        return new JmPhoto(
                photoId,
                title,
                seriesId,
                scrambleId,
                sortOrder,
                "",
                tags,
                images,
                isSingleAlbum
        );
    }

    private static String parsePhotoTitle(Document doc) {
        String fullTitle = doc.title().trim();
        int separatorIndex = fullTitle.indexOf('|');
        return (separatorIndex != -1) ? fullTitle.substring(0, separatorIndex).trim() : fullTitle;
    }

    private static List<String> parsePhotoTags(Document doc) {
        String keywords = doc.selectFirst("meta[name=keywords]").attr("content");
        return List.of(keywords.split(","));
    }

    private static String parseImageUrlTemplate(Document doc, String photoId) {
        // 尝试从一个特殊的 "blank.jpg" 图片的 src 中提取图片域名前缀
        Element blankImg = doc.selectFirst("img[src*='/media/albums/blank.jpg']");
        if (blankImg != null) {
            String src = blankImg.attr("src");
            int mediaIndex = src.indexOf("/media/");
            if (mediaIndex != -1) {
                return JmConstants.PROTOCOL_HTTPS + HtmlParser.parseUrlDomain(src) + "/media/photos/%s/%s";
            }
        }
        // 如果上述方法失败，使用备用逻辑
        throw new ParseResponseException("Could not determine the image URL template from photo page.");
    }

    private static String extractQueryParamsFromUrl(String url) {
        if (url == null) return null;
        int queryIndex = url.indexOf('?');
        return (queryIndex != -1) ? url.substring(queryIndex + 1) : null;
    }

    private static List<JmImage> buildImageList(String photoId, String scrambleId, String urlTemplate, String queryParams, String pageArrJson) {
        try {
            JsonArray jsonArray = JsonParser.parseString(pageArrJson).getAsJsonArray();
            List<String> filenames = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                filenames.add(element.getAsString());
            }

            List<JmImage> images = new ArrayList<>();
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                images.add(new JmImage(
                        photoId,
                        scrambleId,
                        filename,
                        String.format(urlTemplate, photoId, filename),
                        queryParams,
                        i + 1
                ));
            }
            return images;
        } catch (Exception e) {
            throw new ParseResponseException("Failed to parse page_arr JSON for photo: " + photoId, e);
        }
    }

    /**
     * 解析搜索结果页或分类列表页。
     *
     * @param html        完整的HTML页面内容。
     * @param currentPage 当前页码。
     * @return 一个 JmSearchPage 对象。
     */
    public static JmSearchPage parseSearchPage(String html, int currentPage) {
        Document doc = Jsoup.parse(html);

        Element errorElement = doc.selectFirst("div.alert-danger");
        if (errorElement != null) {
            throw new ApiResponseException(errorElement.text());
        }

        // 总结果数
        int totalItems = parseTotalItems(html);
        // 计算总页数
        int totalPages = (totalItems == 0) ? 0 : (int) Math.ceil((double) totalItems / JmConstants.PAGE_SIZE_SEARCH);

        // 选择所有本子卡片
        Elements albumCards = doc.select("div.row > div.col-lg-3.col-md-4.col-sm-6.thumb-overlay-albums");

        List<JmAlbumMeta> content = albumCards.stream()
                .map(HtmlParser::parseAlbumCard)
                .collect(Collectors.toList());

        return new JmSearchPage(currentPage, totalItems, totalPages, content);
    }

    private static JmAlbumMeta parseAlbumCard(Element card) {
        Element link = card.selectFirst("a");
        String id = extractIdFromUrl(link.attr("href"));
        String title = link.attr("title");
        Element authorElement = card.selectFirst("div.title-truncate");
        List<String> authors = (authorElement != null) ? List.of(authorElement.text()) : List.of();
        List<String> tags = ParseHelper.selectAllText(card, "div.tags a");

        return new JmAlbumMeta(id, title, authors, tags);
    }

    /**
     * 从页面中提取搜索结果的总项目数。
     *
     * @param html html
     * @return 搜索结果的总数量。
     */
    private static int parseTotalItems(String html) {
        Matcher matcher = PATTERN_HTML_SEARCH_TOTAL.matcher(html);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    /**
     * 解析用户收藏夹页面。
     *
     * @param html        完整的HTML页面内容。
     * @param currentPage 当前页码。
     * @return 一个 JmFavoritePage 对象。
     */
    public static JmFavoritePage parseFavoritePage(String html, int currentPage) {
        Document doc = Jsoup.parse(html);

        // 总页数逻辑与搜索页类似，但不完全相同
        int totalItems = parseFavoriteTotalItems(doc);
        int totalPages = (totalItems == 0) ? 0 : (int) Math.ceil((double) totalItems / JmConstants.PAGE_SIZE_FAVORITE);

        // 解析收藏夹文件夹列表
        Map<String, String> folderList = doc.select("select.user-select[name=movefolder-fid] > option")
                .stream()
                .collect(Collectors.toMap(
                        option -> option.attr("value"),
                        Element::text,
                        (oldValue, newValue) -> oldValue // in case of duplicate keys
                ));

        // 解析当前页的收藏内容
        List<JmAlbumMeta> content = doc.select("div.col-lg-3.col-md-4.col-sm-6.thumb-overlay-albums")
                .stream()
                .map(HtmlParser::parseFavoriteCard)
                .collect(Collectors.toList());

        return new JmFavoritePage(currentPage, totalPages, content, folderList);
    }

    private static int parseFavoriteTotalItems(Document doc) {
        Element totalElement = doc.selectFirst("h5:contains(共)");
        if (totalElement != null) {
            Matcher matcher = Pattern.compile("共\\s*(\\d+)\\s*本").matcher(totalElement.text());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }

    private static JmAlbumMeta parseFavoriteCard(Element card) {
        Element link = card.selectFirst("a");
        String id = extractIdFromUrl(link.attr("href"));
        String title = card.selectFirst("div.video-title").text();
        // 收藏夹卡片中没有直接显示作者和tag，返回空列表
        return new JmAlbumMeta(id, title, List.of(), List.of());
    }

    /**
     * 从HTML中分析禁漫的公共域名。
     *
     * @param html HTML内容。
     * @return 匹配到的域名列表。
     */
    public static List<String> parseJmPubHtml(String html) {
        List<String> domainList = new ArrayList<>();
        Matcher matcher = PATTERN_HTML_JM_PUB_DOMAIN.matcher(html);
        while (matcher.find()) {
            String domain = matcher.group(0);
            boolean containsKeyword = Stream.of("jm", "comic").anyMatch(domain::contains);
            if (containsKeyword) {
                domainList.add(domain);
            }
        }
        return domainList.stream().distinct().collect(Collectors.toList());
    }

    public static String parseUrlDomain(String url) {
        Matcher matcher = PATTERN_JM_DOMAIN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
