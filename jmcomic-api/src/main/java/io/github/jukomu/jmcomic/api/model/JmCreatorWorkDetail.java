package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 创作者作品详情（包含作品页面图片URL列表）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCreatorWorkDetail(
        /* 作品ID */
        String id,
        /* 作品名称 */
        String name,
        /* 总页数 */
        int totalPage,
        /* 页面图片列表 */
        List<Image> images,
        /* 作品描述 */
        String content,
        /* 上传时间戳 */
        long addtime,
        /* 上传日期时间，如 "2026-03-31 22:39:28" */
        String adddt
) {

    /**
     * 获取作品ID
     *
     * @return 作品ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取作品名称
     *
     * @return 作品名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public int getTotalPage() {
        return totalPage;
    }

    /**
     * 获取页面图片列表
     *
     * @return 页面图片列表
     */
    public List<Image> getImages() {
        return images;
    }

    /**
     * 获取作品描述
     *
     * @return 作品描述
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取上传时间戳
     *
     * @return 上传时间戳
     */
    public long getAddtime() {
        return addtime;
    }

    /**
     * 获取上传日期时间
     *
     * @return 上传日期时间，如 "2026-03-31 22:39:28"
     */
    public String getAdddt() {
        return adddt;
    }

    /**
     * @author JUKOMU
     * @Description: 作品页面图片信息
     * @Project: jmcomic-api-java
     * @Date: 2026/4/27
     */
    public record Image(
            /* 页码（从1开始） */
            int page,
            /* 图片URL */
            String image
    ) {

        /**
         * 获取页码
         *
         * @return 页码
         */
        public int getPage() {
            return page;
        }

        /**
         * 获取图片URL
         *
         * @return 图片URL
         */
        public String getImage() {
            return image;
        }
    }
}
