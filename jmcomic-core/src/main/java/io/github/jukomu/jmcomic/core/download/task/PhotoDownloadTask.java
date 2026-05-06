package io.github.jukomu.jmcomic.core.download.task;

import io.github.jukomu.jmcomic.api.download.DownloadProgress;
import io.github.jukomu.jmcomic.api.download.DownloadResult;
import io.github.jukomu.jmcomic.api.download.enums.TaskState;
import io.github.jukomu.jmcomic.api.download.task.BaseDownloadTask;
import io.github.jukomu.jmcomic.api.download.task.TaskObserver;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.core.download.DownloadManager;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JUKOMU
 * @Description: 章节下载任务
 * @Project: JMComic-Api-Java
 * @Date: 2026/5/4
 */
public class PhotoDownloadTask extends BaseDownloadTask implements TaskObserver {

    private final JmPhoto photo;
    private final DownloadManager downloadManager;
    private final Set<String> countedTerminalTasks = ConcurrentHashMap.newKeySet();

    public PhotoDownloadTask(JmPhoto photo, DownloadManager downloadManager) {
        super();
        this.photo = photo;
        this.downloadManager = downloadManager;
    }

    @Override
    public void start() {
        if (transitState(TaskState.QUEUED, TaskState.RUNNING)) {
            recordStartTimestamp();
            notifyStateChanged(TaskState.RUNNING);
            for (BaseDownloadTask childTask : this.childTasks) {
                downloadManager.submit(childTask);
            }
            TaskState terminalState = aggregateTerminalState();
            if (terminalState != null) {
                doAggregateTerminalState(terminalState);
            }
        }
    }

    @Override
    public void pause() {
        if (transitState(TaskState.QUEUED, TaskState.PAUSED) ||
                transitState(TaskState.RUNNING, TaskState.PAUSED)) {
            notifyStateChanged(TaskState.PAUSED);
            for (BaseDownloadTask childTask : this.childTasks) {
                childTask.pause();
            }
        }
    }

    @Override
    public void resume() {
        if (isState(TaskState.PAUSED)) {
            downloadManager.submit(this);
        }
    }

    @Override
    public void cancel() {
        if (transitState(TaskState.RUNNING, TaskState.CANCELLING) ||
                transitState(TaskState.PENDING, TaskState.CANCELLED) ||
                transitState(TaskState.QUEUED, TaskState.CANCELLED) ||
                transitState(TaskState.PAUSED, TaskState.CANCELLED)) {
            notifyStateChanged(currentState());
            for (BaseDownloadTask childTask : this.childTasks) {
                childTask.cancel();
            }
        }
    }

    public JmPhoto getPhoto() {
        return this.photo;
    }

    @Override
    public void onStateChanged(BaseDownloadTask task, TaskState newState) {
        if (newState.isTerminal() && !countedTerminalTasks.add(task.getTaskId())) {
            // 已计数
            return;
        }
        if (newState.isTerminal()) {
            DownloadResult result = task.getCurrentDownloadResult();
            for (Path path : result.getSuccessfulFiles()) {
                addSuccessfulFile(path);
            }
            for (Map.Entry<JmImage, Exception> entry : result.getFailedTasks().entrySet()) {
                addFailedTask(entry.getKey(), entry.getValue());
            }
        }
        switch (newState) {
            case COMPLETED:
                synchronized (counterLock) {
                    this.completedCount++;
                }
                break;
            case FAILED:
                synchronized (counterLock) {
                    this.failedCount++;
                }
                break;
            case CANCELLED:
                synchronized (counterLock) {
                    this.cancelledCount++;
                }
                break;
            case SKIPPED:
                synchronized (counterLock) {
                    this.skippedCount++;
                }
                break;
            default:
                break;
        }

        TaskState terminalState = aggregateTerminalState();
        if (terminalState == null) {
            // 子任务没有全部结束
            return;
        }

        if (doAggregateTerminalState(terminalState)) {
            // 当前任务处于终态
            DownloadResult result = getCurrentDownloadResult();
            notifyFinish(result);
        }
    }

    @Override
    public void onProgressUpdate(BaseDownloadTask task, DownloadProgress progress) {
        synchronized (progressLock) {
            long totalDownloadedBytes = 0;
            long totalBytes = 0;
            for (BaseDownloadTask childTask : childTasks) {
                long bytes = childTask.getDownloadedBytes();
                long childTotalBytes = childTask.getTotalBytes();
                // 统计总字节数
                if (childTotalBytes == -1) {
                    childTotalBytes = bytes;
                }
                totalDownloadedBytes += bytes;
                totalBytes += childTotalBytes;
            }
            this.downloadedBytes = totalDownloadedBytes;
            this.totalBytes = totalBytes;
            DownloadProgress partialProgress = new DownloadProgress(
                    this.photo.getAlbumId(),
                    null,
                    this.photo.getId(),
                    this.photo.getTitle(),
                    this.completedCount + this.skippedCount,
                    this.failedCount + this.cancelledCount,
                    this.childTasks.size(),
                    0,
                    0,
                    0,
                    false,
                    totalDownloadedBytes,
                    String.valueOf(System.currentTimeMillis())
            );
            notifyProgressUpdate(partialProgress);
        }
    }

    @Override
    public void onFinished(BaseDownloadTask task, DownloadResult result) {
        for (Path path : result.getSuccessfulFiles()) {
            addSuccessfulFile(path);
        }
        for (Map.Entry<JmImage, Exception> entry : result.getFailedTasks().entrySet()) {
            addFailedTask(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void onError(BaseDownloadTask task, Exception e) {
        DownloadResult result = task.getCurrentDownloadResult();
        for (Map.Entry<JmImage, Exception> entry : result.getFailedTasks().entrySet()) {
            addFailedTask(entry.getKey(), entry.getValue());
        }
    }
}
