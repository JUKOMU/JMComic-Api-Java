# 下载任务系统

下载任务系统提供了比直接下载方法更强的控制能力：**暂停/恢复/取消**、**状态观察**、**字节级进度**。

## 任务树结构

下载任务按三层分级组织：

```
AlbumDownloadTask（本子）
  ├── PhotoDownloadTask（章节1）
  │     ├── ImageDownloadTask（图片1）
  │     └── ImageDownloadTask（图片2）
  └── PhotoDownloadTask（章节2）
        └── ImageDownloadTask（图片3）
```

- **本子任务**：聚合所有章节结果，父任务
- **章节任务**：聚合该章节所有图片结果，有父任务
- **图片任务**：叶节点，实际执行单张图片下载

## 创建与提交

```java
import io.github.jukomu.jmcomic.api.download.*;
import io.github.jukomu.jmcomic.api.download.task.*;
import io.github.jukomu.jmcomic.api.download.enums.*;

JmAlbum album = client.getAlbum("1064000");
BaseDownloadTask task = client.createDownloadTask(album, Path.of("downloads"));

client.downloadManager().submit(task);
```

## 注册观察者

通过 `TaskObserver` 接口监听任务事件：

```java
task.addObserver(new TaskObserver() {
    @Override
    public void onStateChanged(BaseDownloadTask task, TaskState newState) {
        System.out.println("状态变更: " + newState);
    }

    @Override
    public void onProgressUpdate(BaseDownloadTask task, DownloadProgress progress) {
        System.out.printf("进度: %d/%d 图片, %d bytes%n",
                progress.completedImages(), progress.totalImages(),
                progress.downloadedBytes());
    }

    @Override
    public void onFinished(BaseDownloadTask task, DownloadResult result) {
        System.out.println("完成! 成功: " + result.getSuccessfulFiles().size()
                + ", 失败: " + result.getFailedTasks().size());
    }

    @Override
    public void onError(BaseDownloadTask task, Exception e) {
        System.err.println("任务异常: " + e.getMessage());
    }
});
```

## 运行时控制

```java
IDownloadManager manager = client.downloadManager();

// 暂停指定任务
manager.pause(task.getTaskId());

// 恢复暂停的任务
manager.resume(task.getTaskId());

// 取消任务（不可恢复）
manager.cancel(task.getTaskId());
```

> 暂停会中断当前图片的网络请求（不支持断点续传），恢复后重新下载该图片。

## 状态机

任务从创建到结束经历严格的状态迁移：

```
PENDING → QUEUED → RUNNING → COMPLETED / FAILED / COMPLETED_WITH_ERRORS
                   ↘ PAUSED → QUEUED → ...
                   ↘ CANCELLING → CANCELLED
```

| 状态 | 可否转化 | 说明 |
|------|----------|------|
| `PENDING` | → QUEUED / CANCELLED / SKIPPED / FAILED | 初始状态 |
| `QUEUED` | → RUNNING / PAUSED / CANCELLING / CANCELLED | 已入队 |
| `RUNNING` | → PAUSED / CANCELLING / 终态 | 执行中 |
| `PAUSED` | → QUEUED / CANCELLING / CANCELLED | 暂停中 |
| `CANCELLING` | → CANCELLED | 取消中 |
| `CANCELLED` | 终态 | 已取消 |
| `COMPLETED` | 终态 | 全部成功 |
| `COMPLETED_WITH_ERRORS` | 终态 | 部分成功 |
| `FAILED` | 终态 | 全部失败 |
| `SKIPPED` | 终态 | 跳过（文件已存在） |

可通过 `TaskState.isTerminal()` 判断是否终态，`isActive()` 判断是否占用下载资源。

## 查询任务

```java
IDownloadManager manager = client.downloadManager();

// 按ID查任务
BaseDownloadTask task = manager.getTask(taskId);

// 活跃任务列表
List<BaseDownloadTask> active = manager.getActiveTasks();

// 全部注册过的任务
List<BaseDownloadTask> all = manager.getTaskRegistry();
```

## 获取任务结果

任务结束后可通过 `getCurrentDownloadResult()` 获取：

```java
DownloadResult result = task.getCurrentDownloadResult();

for (Path file : result.getSuccessfulFiles()) {
    System.out.println("成功: " + file);
}

result.getFailedTasks().forEach((image, error) ->
        System.err.println("失败: " + image.getTag() + " - " + error.getMessage()));
```

## 完整示例：带暂停/恢复的下载

```java
JmAlbum album = client.getAlbum("1064000");
BaseDownloadTask task = client.createDownloadTask(album, Path.of("downloads"));
IDownloadManager manager = client.downloadManager();

// 注册观察者接收完成通知
CountDownLatch done = new CountDownLatch(1);
task.addObserver(new TaskObserver() {
    @Override public void onStateChanged(BaseDownloadTask t, TaskState s) {
        System.out.println("→ " + s);
    }
    @Override public void onProgressUpdate(BaseDownloadTask t, DownloadProgress p) {
        System.out.printf("  图片 %d/%d, 章节 %d/%d, %d bytes%n",
                p.completedImages(), p.totalImages(),
                p.completedPhotos(), p.totalPhotos(),
                p.downloadedBytes());
    }
    @Override public void onFinished(BaseDownloadTask t, DownloadResult r) {
        System.out.println("下载完成: 成功" + r.getSuccessfulFiles().size() + "张");
        done.countDown();
    }
    @Override public void onError(BaseDownloadTask t, Exception e) {
        System.err.println("错误: " + e.getMessage());
        done.countDown();
    }
});

manager.submit(task);

// 模拟中途暂停再恢复
Thread.sleep(2000);
manager.pause(task.getTaskId());
System.out.println("已暂停");
Thread.sleep(1000);
manager.resume(task.getTaskId());
System.out.println("已恢复");

done.await();
```
