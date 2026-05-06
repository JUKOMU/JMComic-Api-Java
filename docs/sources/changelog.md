# 版本说明

## v1.1.0

### 新增下载任务系统

- 全新的任务管理框架，支持暂停/恢复/取消、状态机、观察者模式、任务管理器
    - `BaseDownloadTask` — 下载任务基类，支持父子任务树、状态迁移、进度聚合
    - `IDownloadManager` — 任务管理器接口（submit / pause / resume / cancel / 查询）
    - `TaskObserver` — 观察者接口（onStateChanged / onProgressUpdate / onFinished / onError）
    - `TaskState` — 10 个任务状态（PENDING / QUEUED / RUNNING / PAUSED / CANCELLING / CANCELLED / COMPLETED / COMPLETED_WITH_ERRORS / FAILED / SKIPPED）
    - `TaskType` — 任务类型（ALBUM / PHOTO / IMAGE）
    - `JmDownloadClient` 新增 `createDownloadTask` 和 `downloadManager` 方法

## v1.0.0

首个正式版本。

### 功能

- **漫画**: 本子详情、章节阅读、搜索、分类排行、分类列表
- **下载**: 并发下载、链式 API（withPath / withProgress / withExecutor）、三层粒度路径策略
- **评论**: 支持漫画/小说/博客/用户等多实体评论，发表/回复
- **收藏**: 收藏夹管理、标签管理、文件夹增删改移
- **用户**: 登录/登出、个人资料查看与编辑
- **签到**: 每日签到、签到历史
- **小说**: 列表、详情、章节、搜索、评论/收藏
- **创作者**: 作者列表、作品浏览、作品详情
- **发现**: 热门标签、最新上架、随机推荐、每周必看
- **通知**: 通知列表、已读标记
- **追踪**: 连载追踪列表、追踪状态管理
- **双客户端**: API 客户端（推荐）+ HTML 客户端
- **模块化**: api 模块零依赖，core 模块含完整实现
- **Android 支持**: jmcomic-android-support 模块
