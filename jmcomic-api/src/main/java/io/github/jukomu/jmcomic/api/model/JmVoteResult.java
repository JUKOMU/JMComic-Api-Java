package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 评论投票操作的响应结果
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 * @deprecated 该功能已被 JM 平台停用，服务端返回"评价已停用"
 */
@Deprecated
public record JmVoteResult(
        /*
          操作结果状态。
         */
        String status,
        /*
          操作结果消息。
         */
        String msg,
        /*
          操作后的点赞数。
         */
        int voteUp,
        /*
          操作后的点踩数。
         */
        int voteDown
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

    /**
     * 获取点赞数
     */
    public int getVoteUp() {
        return voteUp;
    }

    /**
     * 获取点踩数
     */
    public int getVoteDown() {
        return voteDown;
    }
}
