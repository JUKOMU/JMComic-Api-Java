package io.github.jukomu.jmcomic.core.download;

import io.github.jukomu.jmcomic.api.download.DownloadProgress;
import io.github.jukomu.jmcomic.api.download.DownloadResult;
import io.github.jukomu.jmcomic.api.download.IDownloadManager;
import io.github.jukomu.jmcomic.api.download.enums.TaskState;
import io.github.jukomu.jmcomic.api.download.task.BaseDownloadTask;
import io.github.jukomu.jmcomic.api.download.task.TaskObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author JUKOMU
 * @Description: 下载任务管理器实现
 * @Project: JMComic-Api-Java
 * @Date: 2026/5/5
 */
public class DownloadManager implements IDownloadManager, TaskObserver {

    private final Logger logger = LoggerFactory.getLogger(DownloadManager.class);
    private final ConcurrentMap<String, BaseDownloadTask> taskRegistry = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BaseDownloadTask> activeTasks = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final long closeTimeoutMs;

    public DownloadManager(ExecutorService executor, long closeTimeoutMs) {
        this.executor = executor;
        this.closeTimeoutMs = closeTimeoutMs;
    }

    @Override
    public BaseDownloadTask getTask(String taskId) {
        return taskRegistry.get(taskId);
    }

    @Override
    public List<BaseDownloadTask> getActiveTasks() {
        return new ArrayList<>(activeTasks.values());
    }

    @Override
    public List<BaseDownloadTask> getTaskRegistry() {
        return new ArrayList<>(taskRegistry.values());
    }

    @Override
    public void submit(BaseDownloadTask task) {
        if (task.transitState(TaskState.PENDING, TaskState.QUEUED) ||
                task.transitState(TaskState.PAUSED, TaskState.QUEUED)) {
            taskRegistry.put(task.getTaskId(), task);
            activeTasks.put(task.getTaskId(), task);
            task.addObserver(this);
            task.notifyStateChanged(TaskState.QUEUED);
            try {
                executor.submit(task);
            } catch (Exception e) {
                activeTasks.remove(task.getTaskId());
                taskRegistry.remove(task.getTaskId());

                if (task.transitState(TaskState.QUEUED, TaskState.FAILED)) {
                    task.recordEndTimestamp();
                    task.notifyError(e);
                    task.notifyStateChanged(TaskState.FAILED);
                }

                throw e;
            }
        }
    }

    @Override
    public void pause(String taskId) {
        BaseDownloadTask task = taskRegistry.get(taskId);
        if (task == null) {
            return;
        }
        task.pause();
    }

    @Override
    public void resume(String taskId) {
        BaseDownloadTask task = taskRegistry.get(taskId);
        if (task == null) {
            return;
        }
        task.resume();
    }

    @Override
    public void cancel(String taskId) {
        BaseDownloadTask task = taskRegistry.get(taskId);
        if (task == null) {
            return;
        }
        task.cancel();
    }

    @Override
    public void close() {
        for (BaseDownloadTask task : taskRegistry.values()) {
            task.cancel();
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(closeTimeoutMs, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        taskRegistry.clear();
        activeTasks.clear();
    }

    @Override
    public void onStateChanged(BaseDownloadTask task, TaskState newState) {
        // 移除终态任务
        if (newState.isTerminal()) {
            activeTasks.remove(task.getTaskId());
        }
    }

    @Override
    public void onProgressUpdate(BaseDownloadTask task, DownloadProgress progress) {
        if (task.getParentTask() != null) {
            return;
        }
        logger.info("Task: {}, progress with downloaded bytes: {}", task.getTaskId(), progress.downloadedBytes());
    }

    @Override
    public void onFinished(BaseDownloadTask task, DownloadResult result) {
        if (task.getParentTask() != null) {
            return;
        }
        logger.info("Task: {}, finished with result: {}", task.getTaskId(), result);
    }

    @Override
    public void onError(BaseDownloadTask task, Exception e) {
        logger.error("Task: {}, error with exception: {}", task.getTaskId(), e.getMessage(), e);
    }
}
