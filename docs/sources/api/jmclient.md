# JmClient 接口

`JmClient` 是核心客户端接口，定义了所有业务操作。通过 `JmComic.newApiClient()` 或 `JmComic.newHtmlClient()` 获取实例。

## 获取实例

```java
JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.API)
        .build();

AbstractJmClient client = JmComic.newApiClient(config);
```

## 漫画相关

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getAlbum(String albumId)` | `JmAlbum` | 获取本子详情 |
| `getComicRead(String comicId)` | `JmAlbum` | 获取阅读数据（含图片列表） |
| `getPhoto(String photoId)` | `JmPhoto` | 获取章节详情 |
| `search(SearchQuery query)` | `JmSearchPage` | 搜索本子 |
| `getCategories(SearchQuery query)` | `JmSearchPage` | 获取分类排行 |
| `getCategoriesList()` | `JmCategoryList` | 获取分类列表 |
| `fetchImageBytes(JmImage image)` | `byte[]` | 获取图片二进制数据 |
| `getAlbumDownloadInfo(String albumId)` | `JmAlbumDownloadInfo` | 获取本子下载信息 |

## 用户与会话

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `login(String, String)` | `JmUserInfo` | 登录 |
| `logout()` | `void` | 登出 |
| `getUserProfile(String uid)` | `JmUserProfile` | 获取用户资料 |
| `editUserProfile(String uid, Map)` | `JmUserProfile` | 编辑用户资料 |

## 评论

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getComments(ForumQuery)` | `JmCommentList` | 获取评论列表 |
| `postComment(String, String)` | `JmComment` | 发表本子评论 |
| `replyToComment(String, String, String)` | `JmComment` | 回复本子评论 |
| `postBlogComment(String, String, String)` | `JmComment` | 发表博客评论 |
| `replyToBlogComment(String, String, String, String)` | `JmComment` | 回复博客评论 |

## 收藏

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getFavorites(FavoriteQuery)` | `JmFavoritePage` | 获取收藏夹 |
| `toggleAlbumFavorite(String, String)` | `void` | 切换收藏状态 |
| `manageFavoriteFolder(...)` | `JmFavoriteFolderResult` | 管理收藏文件夹 |
| `getTagsFavorite()` | `List<JmTagFavorite>` | 获取收藏标签 |
| `addFavoriteTags(List)` | `void` | 添加收藏标签 |
| `removeFavoriteTags(List)` | `void` | 删除收藏标签 |

## 发现

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getHotTags()` | `List<String>` | 热门标签 |
| `getLatest(int page)` | `JmSearchPage` | 最新上架 |
| `getRandomRecommend()` | `List<JmAlbumMeta>` | 随机推荐 |
| `getPromote()` | `Map` | 首页推广 |
| `getWeeklyPicksList()` | `JmWeeklyPicksList` | 每周必看列表 |
| `getWeeklyPicksDetail(String)` | `JmWeeklyPicksDetail` | 每周必看详情 |
| `getSerialization(int page)` | `JmSearchPage` | 连载系列 |

## 通知与追踪

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getNotifications()` | `JmNotificationPage` | 通知列表 |
| `markNotification(String, int)` | `void` | 标记通知 |
| `getUnreadCount()` | `Map` | 未读数量 |
| `getAlbumTrackingList(int)` | `JmTrackingPage` | 追踪列表 |
| `getAlbumSertracking(String)` | `boolean` | 查询追踪状态 |
| `setAlbumSertracking(String)` | `void` | 设置追踪 |

## 签到

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getDailyCheckInStatus(String)` | `JmDailyCheckInStatus` | 签到状态 |
| `doDailyCheckin(String, String)` | `void` | 执行签到 |
| `getDailyCheckInOptions(String)` | `List` | 签到选项 |
| `filterDailyCheckInList(String)` | `List` | 签到历史 |

## 其他

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getWatchHistory(int)` | `List<JmAlbumMeta>` | 浏览历史 |
| `deleteWatchHistory(String)` | `void` | 删除历史 |
| `toggleAlbumLike(String)` | `void` | 切换点赞 |
| `getTasks(String, String)` | `JmTaskList` | 任务列表 |
