# 枚举类型

## ClientType

客户端类型：

| 值 | 说明 |
|----|------|
| `API` | API 客户端（推荐），调用移动端 API |
| `HTML` | HTML 客户端，解析网页 |

## Category

本子分类。取值为禁漫的分类常量。

## SubCategory

本子子分类。

## SearchMainTag

搜索主标签。

## TimeOption

搜索时的时间范围：

| 值 | 说明 |
|----|------|
| `ALL` | 所有时间 |
| `TODAY` | 今天 |
| `WEEK` | 本周 |
| `MONTH` | 本月 |

## OrderBy

搜索排序方式：

| 值 | 说明 |
|----|------|
| `LATEST` | 最新 |
| `MOST_VIEWED` | 最多观看 |
| `MOST_IMAGES` | 图片最多 |
| `MOST_LIKED` | 最多喜欢 |

## ForumMode

评论查阅模式：

| 值 | 说明 |
|----|------|
| `ALL` | 全部 |
| `MANHUA` | 漫画评论 |
| `CHAT` | 闲聊大厅 |

## FavoriteFolderType

收藏文件夹操作类型：

| 值 | 说明 |
|----|------|
| `ADD` | 添加文件夹 |
| `EDIT` | 重命名文件夹 |
| `MOVE` | 移动本子到文件夹 |
| `DELETE` | 删除文件夹 |

## SearchMainTag

搜索主标签/搜索类型：

| 值 | 说明 |
|----|------|
| `SITE_SEARCH` | 站内搜索（默认） |
| `WORK` | 作品 |
| `AUTHOR` | 作者 |
| `TAG` | 标签 |
| `ACTOR` | 登场人物 |

## SubCategory

副分类（网页端特有）：

| 值 | 说明 |
|----|------|
| `SUB_CHINESE` | 中文 |
| `SUB_JAPANESE` | 日语 |
| `SUB_ANOTHER_OTHER` | 其他 |
| `SUB_ANOTHER_3D` | 3D |
| `SUB_ANOTHER_COSPLAY` | cosplay |
| `SUB_DOUJIN_CG` | CG |
| `SUB_SINGLE_YOUTH` | 青年漫 |

## TaskState

下载任务状态（状态机）：

| 值 | 说明 |
|----|------|
| `PENDING` | 刚创建，尚未入队 |
| `QUEUED` | 已入队等待调度 |
| `RUNNING` | 正在下载 |
| `PAUSED` | 已暂停 |
| `CANCELLING` | 正在取消中 |
| `CANCELLED` | 已取消（终态，不可恢复） |
| `COMPLETED` | 全部成功（终态） |
| `COMPLETED_WITH_ERRORS` | 部分成功（终态） |
| `FAILED` | 全部失败（终态） |
| `SKIPPED` | 跳过（终态） |

终态（`COMPLETED`、`COMPLETED_WITH_ERRORS`、`FAILED`、`CANCELLED`、`SKIPPED`）不可再切换。可通过 `isTerminal()` 判断，`isActive()` 判断是否占用资源（`QUEUED` / `RUNNING`）。

## TaskType

下载任务类型：

| 值 | 说明 |
|----|------|
| `ALBUM` | 本子级下载任务 |
| `PHOTO` | 章节级下载任务 |
| `IMAGE` | 图片级下载任务 |

## CommentStatus

评论状态。

## VoteType

投票类型（点赞/点踩），该功能已被平台停用。
