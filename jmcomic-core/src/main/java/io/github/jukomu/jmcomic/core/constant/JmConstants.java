package io.github.jukomu.jmcomic.core.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JUKOMU
 * @Description: 内部常量类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmConstants {

    private JmConstants() {
        // 防止实例化
    }

    // == 分页大小 ==
    public static final int PAGE_SIZE_SEARCH = 80;
    public static final int PAGE_SIZE_FAVORITE = 20;

    // == 网络与协议 ==
    public static final String PROTOCOL_HTTPS = "https://";
    public static final String PLACEHOLDER_HOST = "jm-placeholder.domain.com";

    // == 移动端 API 密钥与版本 ==
    public static final String APP_HEADER_TOKEN = "token";
    public static final String APP_HEADER_TOKEN_PARAM = "tokenparam";
    public static final String APP_TOKEN_SECRET = "18comicAPP";
    public static final String APP_TOKEN_SECRET_2 = "18comicAPPContent"; // 用于特殊接口
    public static final String APP_DATA_SECRET = "185Hcomic3PAPP7R";
    public static final String API_DOMAIN_SERVER_SECRET = "diosfjckwpqpdfjkvnqQjsik";
    public static String APP_VERSION = "2.0.20";

    // == 基础 API 端点路径 ==
    public static final String API_SETTING = "setting";
    public static final String API_ALBUM = "album";
    public static final String API_CHAPTER = "chapter";
    public static final String API_CHAPTER_VIEW_TEMPLATE = "chapter_view_template";
    public static final String API_SEARCH = "search";
    public static final String API_FAVORITE = "favorite";
    public static final String API_FILTER = "filter";
    public static final String API_LOGIN = "login";
    public static final String API_COMMENT = "comment";

    // == API 端点路径 (核心交互) ==
    public static final String API_FORUM = "forum";
    // @deprecated 评价投票功能已停用，服务端返回"评价已停用"
    public static final String API_COMMENT_VOTE = "comment_vote";
    public static final String API_LIKE = "like";
    public static final String API_HOT_TAGS = "hot_tags";
    public static final String API_LATEST = "latest";
    public static final String API_COMIC_READ = "comic_read";
    public static final String API_FAVORITE_FOLDER = "favorite_folder";
    public static final String API_ALBUM_DOWNLOAD = "album_download_2";

    // == API 端点路径 (用户功能) ==
    public static final String API_MEMBER_REGISTER = "register";
    public static final String API_MEMBER_LOGOUT = "logout";
    public static final String API_MEMBER_FORGOT = "forgot";
    public static final String API_USEREDIT = "useredit";
    public static final String API_HISTORY_LIST = "watch_list";
    public static final String API_COMIC_RANDOM_RECOMMEND = "random_recommend";
    public static final String API_COMIC_PROMOTE = "promote";
    public static final String API_COMIC_PROMOTE_LIST = "promote_list";
    public static final String API_COMIC_SER_MORE_LIST = "serialization";
    public static final String API_TAGS_FAVORITE = "tags_favorite";
    public static final String API_TAGS_FAVORITE_UPDATE = "tags_favorite_update";
    public static final String API_CATEGORIES_LIST = "categories";

    // == 小说 API 端点路径 ==
    public static final String API_NOVEL_LIST = "novels";
    public static final String API_NOVEL_DETAIL = "novel";
    public static final String API_NOVEL_CHAPTERS = "novelchapters";
    public static final String API_NOVEL_SEARCH = "search_novels";
    public static final String API_NOVEL_FAVORITES = "novel_favorites";
    public static final String API_EDIT_NOVEL_FAVORITES = "novel_favorites_folder";
    public static final String API_NOVEL_COIN_BUY = "coin_buy_nc";

    // == 创作者 API 端点路径 ==
    public static final String API_CREATOR_AUTHOR = "creator_author";
    public static final String API_CREATOR_WORK = "creator_work";
    public static final String API_CREATOR_WORK_DETAIL = "creator_author_work";
    public static final String API_CREATOR_WORK_INFO = "creator_work_info";
    public static final String API_CREATOR_WORK_INFO_DETAIL = "creator_work_info_detail";

    // == 通知 API 端点路径 ==
    public static final String API_NOTIFICATIONS = "notifications";
    public static final String API_NOTIFICATIONS_UNREAD = "notifications/unreadCount";
    public static final String API_NOTIFICATIONS_SERTRACK = "album_sertracking";
    public static final String API_NOTIFICATIONS_TRACK_LIST = "album_tracking";

    // == 任务/硬币 API 端点路径 ==
    public static final String API_TASKS_LIST = "tasks";
    public static final String API_TASKS_BUY_LIST = "coin";
    public static final String API_COIN_BUY_COMICS = "coin_buy_comics";
    public static final String API_COIN_BUY_CHARGE = "coin_buy_charge";
    public static final String API_AD_FREE = "ad_free";

    // == 签到 API 端点路径 ==
    public static final String API_DAILY = "daily";
    public static final String API_DAILY_CHECK = "daily_chk";
    public static final String API_DAILY_LIST = "daily_list";
    public static final String API_DAILY_LIST_FILTER = "daily_list/filter";

    // == 每周必看 API 端点路径 ==
    public static final String API_WEEK = "week";
    public static final String API_WEEK_FILTER_LIST = "week/filter";

    // == 分页大小 ==
    public static final int PAGE_SIZE_FORUM = 10;

    // == 图片分割算法关键版本号 ==
    public static final int SCRAMBLE_220980 = 220980;
    public static final int SCRAMBLE_268850 = 268850;
    public static final int SCRAMBLE_421926 = 421926;

    // == 域名 ==
    public static List<String> DEFAULT_API_DOMAINS = Collections.unmodifiableList(List.of(
            "www.cdnaspa.vip",
            "www.cdnaspa.club",
            "www.cdnplaystation6.org",
            "www.cdnplaystation6.vip",
            "www.cdnplaystation6.cc"
    ));

    public static List<String> DEFAULT_IMAGE_DOMAINS = Collections.unmodifiableList(List.of(
            "cdn-msp.jmapiproxy1.cc",
            "cdn-msp.jmapiproxy2.cc",
            "cdn-msp2.jmapiproxy2.cc",
            "cdn-msp3.jmapiproxy2.cc",
            "cdn-msp.jmapinodeudzn.net",
            "cdn-msp3.jmapinodeudzn.net"
    ));

    public static final List<String> API_URL_DOMAIN_SERVER_LIST = Collections.unmodifiableList(List.of(
            "https://rup4a04-c01.tos-ap-southeast-1.bytepluses.com/newsvr-2025.txt",
            "https://rup4a04-c02.tos-cn-hongkong.bytepluses.com/newsvr-2025.txt"
    ));

    public static final String JM_REDIRECT_URL = PROTOCOL_HTTPS + "jm365.work/3YeBdF";
    public static final String JM_PUB_URL = PROTOCOL_HTTPS + "jmcomicgo.org";

    // == 默认 User-Agent ==
    public static final String DEFAULT_USER_AGENT_API = "Mozilla/5.0 (Linux; Android 9; V1938CT Build/PQ3A.190705.11211812; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.114 Safari/537.36";
    public static final String DEFAULT_USER_AGENT_HTML = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    // == 默认 Headers 模板 ==
    public static final Map<String, String> DEFAULT_API_HEADERS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("Accept-Encoding", "gzip, deflate");
        put("user-agent", "Mozilla/5.0 (Linux; Android 9; V1938CT Build/PQ3A.190705.11211812; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.114 Safari/537.36");
    }});

    public static final Map<String, String> DEFAULT_HTML_HEADERS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        put("accept-language", "zh-CN,zh;q=0.9");
        put("cache-control", "no-cache");
        put("dnt", "1");
        put("pragma", "no-cache");
        put("priority", "u=0, i");
        // Referer will be dynamically set, default is https://18comic.vip/
        put("sec-ch-ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"");
        put("sec-ch-ua-mobile", "?0");
        put("sec-ch-ua-platform", "\"Windows\"");
        put("sec-fetch-dest", "document");
        put("sec-fetch-mode", "navigate");
        put("sec-fetch-site", "none");
        put("sec-fetch-user", "?1");
        put("upgrade-insecure-requests", "1");
        put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
    }});
}
