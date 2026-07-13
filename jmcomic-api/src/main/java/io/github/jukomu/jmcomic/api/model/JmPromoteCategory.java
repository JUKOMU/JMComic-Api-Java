package io.github.jukomu.jmcomic.api.model;

import java.util.List;
import java.util.Map;

/**
 * @author JUKOMU
 * @Description: 首页推荐栏分类
 * @Project: jmcomic-api-java
 * @Date: 2026/07/13
 */
public record JmPromoteCategory(
        /*
          分类ID
         */
        String id,
        /*
          分类标题
         */
        String title,
        /*
          分类别名
         */
        String slug,
        /*
          分类类型（promote, not_in_category_id, category_id, library, novels）
         */
        String type,
        /*
          筛选值
         */
        String filterVal,
        /*
          内容列表（不同type的content结构不同，故使用Map）
         */
        List<Map> content
) {

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getType() {
        return type;
    }

    public String getFilterVal() {
        return filterVal;
    }

    public List<Map> getContent() {
        return content;
    }
}
