package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 收藏夹管理操作的响应结果（增删改移）
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmFavoriteFolderResult(
        /*
          操作结果状态 ("ok" 表示成功)。
         */
        String status,
        /*
          操作结果消息。
         */
        String msg
) {

    /**
     * 判断操作是否成功
     *
     * @return 是否成功
     */
    public boolean isOk() {
        return "ok".equalsIgnoreCase(status);
    }

    /**
     * 获取操作结果状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取操作结果消息
     */
    public String getMsg() {
        return msg;
    }
}
