package io.github.jukomu.jmcomic.api.model;

import java.util.List;
import java.util.Map;

/**
 * @author JUKOMU
 * @Description: 小说收藏夹分页结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmNovelFavoritesPage(
        /* 当前页小说列表 */
        List<JmNovelMeta> list,
        /* 收藏夹目录列表，每项含 folder_id 和 folder_name */
        List<Map<String, String>> folderList,
        /* 当前收藏夹内小说数量 */
        int count,
        /* 总收藏夹数量 */
        int total
) {

    /**
     * 获取当前页小说列表
     *
     * @return 小说列表
     */
    public List<JmNovelMeta> getList() {
        return list;
    }

    /**
     * 获取收藏夹目录列表
     *
     * @return 目录列表，每项含 folder_id 和 folder_name
     */
    public List<Map<String, String>> getFolderList() {
        return folderList;
    }

    /**
     * 获取当前收藏夹内小说数量
     *
     * @return 小说数量
     */
    public int getCount() {
        return count;
    }

    /**
     * 获取总收藏夹数量
     *
     * @return 总收藏夹数量
     */
    public int getTotal() {
        return total;
    }
}
