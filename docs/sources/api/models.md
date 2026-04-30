# 数据模型一览

## 漫画领域

| 类 | 说明 |
|----|------|
| `JmAlbum` | 本子详情（标题、作者、标签、章节元数据列表） |
| `JmAlbumMeta` | 本子简要信息（ID、标题、封面） |
| `JmPhoto` | 章节详情（标题、图片列表） |
| `JmPhotoMeta` | 章节简要信息 |
| `JmImage` | 单张图片（URL、文件名、序号） |
| `JmAlbumDownloadInfo` | 本子下载信息 |

## 搜索与分类

| 类 | 说明 |
|----|------|
| `JmSearchPage` | 搜索结果分页（内容列表、总页数） |
| `SearchQuery` | 搜索条件构建器（Builder 模式） |
| `JmCategoryList` | 分类列表（分类树 + 标签块） |
| `JmCategoryBlock` | 标签区块 |
| `JmCategoryListItem` | 分类树节点 |
| `JmCategoryMeta` | 分类元数据 |

## 评论

| 类 | 说明 |
|----|------|
| `JmCommentList` | 评论列表分页 |
| `JmComment` | 单条评论 |
| `JmCommentExpInfo` | 评论扩展信息 |
| `ForumQuery` | 评论查询条件（静态工厂方法） |

## 收藏

| 类 | 说明 |
|----|------|
| `JmFavoritePage` | 收藏分页结果 |
| `FavoriteQuery` | 收藏查询条件（Builder） |
| `JmFavoriteFolderResult` | 文件夹操作结果 |
| `JmTagFavorite` | 收藏标签 |

## 用户与签到

| 类 | 说明 |
|----|------|
| `JmUserInfo` | 用户信息（登录返回） |
| `JmUserProfile` | 用户个人资料 |
| `JmDailyCheckInStatus` | 签到状态 |
| `JmDailyCheckInRecordItem` | 签到历史条目 |

## 小说

| 类 | 说明 |
|----|------|
| `JmNovelPage` | 小说列表分页 |
| `JmNovelMeta` | 小说简要信息 |
| `JmNovelDetail` | 小说详情 |
| `JmNovelChapter` | 小说章节内容 |
| `JmNovelChapterMeta` | 小说章节简要信息 |
| `JmNovelComment` | 小说评论 |
| `JmNovelFavoritesPage` | 小说收藏分页 |
| `JmRelatedNovel` | 关联小说 |

## 创作者

| 类 | 说明 |
|----|------|
| `JmCreatorPage` | 作者列表分页 |
| `JmCreatorMeta` | 作者简要信息 |
| `JmCreatorWorkPage` | 作品列表分页 |
| `JmCreatorWorkMeta` | 作品简要信息 |
| `JmCreatorWorkInfo` | 作品信息 |
| `JmCreatorWorkDetail` | 作品详情 |
| `JmCreatorAuthorWorksPage` | 指定作者作品分页 |
| `JmCreatorRelatedWork` | 关联作品 |
| `JmCreatorSponsor` | 赞助者信息 |

## 通知与追踪

| 类 | 说明 |
|----|------|
| `JmNotificationPage` | 通知列表分页 |
| `JmNotification` | 单条通知 |
| `JmTrackingPage` | 追踪列表分页 |
| `JmTrackingItem` | 追踪条目 |

## 发现

| 类 | 说明 |
|----|------|
| `JmWeeklyPicksList` | 每周必看期数列表 |
| `JmWeeklyPicksDetail` | 每周必看某期详情 |
| `JmWeeklyPicksType` | 每周必看类型 |
| `JmWeeklyPicksCategory` | 每周必看分类 |

## 任务

| 类 | 说明 |
|----|------|
| `JmTaskList` | 任务列表 |
| `JmTaskItem` | 任务条目 |

## 下载

| 类 | 说明 |
|----|------|
| `DownloadResult` | 下载结果（成功/失败文件统计） |
| `DownloadProgress` | 下载进度（图片数、章节数） |
| `DownloadRequest` | 链式下载请求对象 |

## 策略接口

| 接口 | 说明 |
|------|------|
| `IAlbumPathGenerator` | 本子目录路径生成 |
| `IPhotoPathGenerator` | 章节目录路径生成 |
| `IDownloadPathGenerator` | 完整三层路径生成 |
