package io.github.jukomu.jmcomic.core.download.task;

import io.github.jukomu.jmcomic.api.download.DownloadProgress;
import io.github.jukomu.jmcomic.api.download.DownloadResult;
import io.github.jukomu.jmcomic.api.download.enums.TaskState;
import io.github.jukomu.jmcomic.api.download.task.BaseDownloadTask;
import io.github.jukomu.jmcomic.api.download.task.TaskObserver;
import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.core.download.DownloadManager;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JUKOMU
 * @Description: 本子下载任务
 * @Project: JMComic-Api-Java
 * @Date: 2026/5/4
 */
public class AlbumDownloadTask extends BaseDownloadTask implements TaskObserver {

    private final JmAlbum album;
    private final DownloadManager downloadManager;
    private final Set<String> countedTerminalTasks = ConcurrentHashMap.newKeySet();

    public AlbumDownloadTask(JmAlbum album, DownloadManager downloadManager) {
        super();
        this.album = album;
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

    public JmAlbum getAlbum() {
        return this.album;
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
            case COMPLETED_WITH_ERRORS:
                synchronized (counterLock) {
                    this.completedWithErrorsCount++;
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
            int totalImageCompleted = 0;
            int totalImageFailed = 0;
            int totalImage = 0;
            for (BaseDownloadTask childTask : childTasks) {
                long bytes = childTask.getDownloadedBytes();
                long childTotalBytes = childTask.getTotalBytes();
                // 统计总字节数
                if (childTotalBytes == -1) {
                    childTotalBytes = bytes;
                }
                totalDownloadedBytes += bytes;
                totalBytes += childTotalBytes;

                // 统计图片
                int imageCompleted = childTask.getCompletedCount();
                int imageSkipped = childTask.getSkippedCount();
                int imageFailed = childTask.getFailedCount();
                int imageCancelled = childTask.getCancelledCount();
                int imageCompletedWithErrors = childTask.getCompletedWithErrorsCount();
                totalImageCompleted += imageCompleted + imageSkipped + imageCompletedWithErrors;
                totalImageFailed += imageFailed + imageCancelled;
                totalImage += childTask.getChildTasks().size();
            }
            this.downloadedBytes = totalDownloadedBytes;
            this.totalBytes = totalBytes;
            DownloadProgress partialProgress = new DownloadProgress(
                    this.album.getId(),
                    this.album.getTitle(),
                    null,
                    null,
                    totalImageCompleted,
                    totalImageFailed,
                    totalImage,
                    this.completedCount + this.completedWithErrorsCount + this.skippedCount,
                    this.failedCount + this.cancelledCount,
                    this.childTasks.size(),
                    true,
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
