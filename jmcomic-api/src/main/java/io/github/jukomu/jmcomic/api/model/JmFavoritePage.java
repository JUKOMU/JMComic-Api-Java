package io.github.jukomu.jmcomic.api.model;

import java.util.List;
import java.util.Map;

/**
 * @author JUKOMU
 * @Description: 代表用户收藏夹的一页结果
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmFavoritePage(
        /*
          当前页码
         */
        int currentPage,
        /*
          收藏夹总共的页数
         */
        int totalPages,
        /*
          当前页包含的收藏的本子摘要信息列表
         */
        List<JmAlbumMeta> content,
        /*
          用户的所有收藏夹文件夹列表
          Map的Key是文件夹ID (例如 "0" 代表 "全部")，Value是文件夹名称
         */
        Map<String, String> folderList
) {

    /**
     * 获取当前页码
     *
     * @return 当前页码
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * 获取收藏夹总共的页数
     *
     * @return 总页数
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * 获取当前页包含的收藏的本子摘要信息列表
     *
     * @return 本子摘要列表
     */
    public List<JmAlbumMeta> getContent() {
        return content;
    }

    /**
     * 获取用户的所有收藏夹文件夹列表
     *
     * @return 文件夹列表
     */
    public Map<String, String> getFolderList() {
        return folderList;
    }
}
