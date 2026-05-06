package io.github.jukomu.jmcomic.api.download.task;

import io.github.jukomu.jmcomic.api.download.DownloadProgress;
import io.github.jukomu.jmcomic.api.download.DownloadResult;
import io.github.jukomu.jmcomic.api.download.enums.TaskState;
import io.github.jukomu.jmcomic.api.download.enums.TaskType;
import io.github.jukomu.jmcomic.api.model.JmImage;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author JUKOMU
 * @Description: 下载任务基类
 * @Project: JMComic-Api-Java
 * @Date: 2026/5/4
 */
public abstract class BaseDownloadTask implements Runnable {

    // 任务编号
    protected final String taskId;
    // 任务类型
    protected TaskType type;
    // 任务状态
    protected TaskState state = TaskState.PENDING;
    // 关联的父任务
    protected BaseDownloadTask parentTask = null;
    // 关联的子任务
    protected List<BaseDownloadTask> childTasks = new ArrayList<>();
    // 注册的观察者
    protected List<TaskObserver> observers = new CopyOnWriteArrayList<>();
    // 预计需要处理的总字节数，未知时为 -1，不保证等于最终文件大小
    protected long totalBytes = -1;
    // 已读取并处理的字节数，用于进度/速度统计
    protected long downloadedBytes = 0;
    // 任务创建时间
    protected final String createTimestamp;
    // 任务开始运行时间
    protected String startTimestamp;
    // 任务结束时间(处于任意终态)
    protected String endTimestamp;
    // 成功的任务数
    protected int completedCount = 0;
    // 失败的任务数
    protected int failedCount = 0;
    // 取消的任务数
    protected int cancelledCount = 0;
    // 跳过的任务数
    protected int skippedCount = 0;
    // 部分成功的任务数
    protected int completedWithErrorsCount = 0;
    // 下载结果
    protected List<Path> successfulFiles = new ArrayList<>();
    protected Map<JmImage, Exception> failedTasks = new HashMap<>();

    // 状态锁
    protected final Object stateLock = new Object();
    // 计数器锁
    protected final Object counterLock = new Object();
    // 进度锁
    protected final Object progressLock = new Object();
    // 结果锁
    protected final Object resultLock = new Object();

    public BaseDownloadTask() {
        this.createTimestamp = String.valueOf(System.currentTimeMillis());
        // 生成任务编号
        this.taskId = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
    }

    public String getTaskId() {
        return this.taskId;
    }

    public TaskType getType() {
        return this.type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public BaseDownloadTask getParentTask() {
        return this.parentTask;
    }

    public void setParentTask(BaseDownloadTask parent) {
        this.parentTask = parent;
    }

    public List<BaseDownloadTask> getChildTasks() {
        return this.childTasks;
    }

    public void setChildTasks(List<BaseDownloadTask> childTasks) {
        this.childTasks = childTasks;
    }

    public List<TaskObserver> getObservers() {
        return this.observers;
    }

    public long getTotalBytes() {
        return this.totalBytes;
    }

    public long getDownloadedBytes() {
        return this.downloadedBytes;
    }

    public String getCreateTimestamp() {
        return this.createTimestamp;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    protected void recordStartTimestamp() {
        this.startTimestamp = String.valueOf(System.currentTimeMillis());
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public void recordEndTimestamp() {
        this.endTimestamp = String.valueOf(System.currentTimeMillis());
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(int cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getCompletedWithErrorsCount() {
        return completedWithErrorsCount;
    }

    public void setCompletedWithErrorsCount(int completedWithErrorsCount) {
        this.completedWithErrorsCount = completedWithErrorsCount;
    }

    public void addSuccessfulFile(Path path) {
        synchronized (resultLock) {
            if (!this.successfulFiles.contains(path)) {
                this.successfulFiles.add(path);
            }
        }
    }

    public void addFailedTask(JmImage jmImage, Exception e) {
        synchronized (resultLock) {
            if (!this.failedTasks.containsKey(jmImage)) {
                this.failedTasks.put(jmImage, e);
            }
        }
    }

    public DownloadResult getCurrentDownloadResult() {
        synchronized (resultLock) {
            List<Path> copyList;
            Map<JmImage, Exception> copyMap;
            if (this.successfulFiles.isEmpty()) {
                copyList = new ArrayList<>();
            } else {
                copyList = List.copyOf(this.successfulFiles);
            }
            if (this.failedTasks.isEmpty()) {
                copyMap = new HashMap<>();
            } else {
                copyMap = Map.copyOf(this.failedTasks);
            }
            return new DownloadResult(copyList, copyMap);
        }
    }

    public void addObserver(TaskObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    // 任务管理
    public abstract void start();

    public abstract void pause();

    public abstract void resume();

    public abstract void cancel();

    // 状态迁移
    protected boolean canTransit(TaskState from, TaskState to) {
        switch (from) {
            case PENDING:
                return to == TaskState.QUEUED
                        || to == TaskState.CANCELLED
                        || to == TaskState.SKIPPED
                        || to == TaskState.FAILED;

            case QUEUED:
                return to == TaskState.RUNNING
                        || to == TaskState.PAUSED
                        || to == TaskState.CANCELLING
                        || to == TaskState.CANCELLED
                        || to == TaskState.SKIPPED;

            case RUNNING:
                return to == TaskState.PAUSED
                        || to == TaskState.CANCELLING
                        || to == TaskState.COMPLETED
                        || to == TaskState.COMPLETED_WITH_ERRORS
                        || to == TaskState.FAILED
                        || to == TaskState.SKIPPED;

            case PAUSED:
                return to == TaskState.QUEUED
                        || to == TaskState.CANCELLING
                        || to == TaskState.CANCELLED;

            case CANCELLING:
                return to == TaskState.CANCELLED;

            case CANCELLED:
            case COMPLETED:
            case COMPLETED_WITH_ERRORS:
            case FAILED:
            case SKIPPED:
            default:
                return false;
        }
    }

    public boolean transitState(TaskState expected, TaskState target) {
        synchronized (stateLock) {
            if (this.state != expected) {
                return false;
            }
            if (!canTransit(expected, target)) {
                return false;
            }
            this.state = target;
            return true;
        }
    }

    public boolean isState(TaskState state) {
        synchronized (stateLock) {
            return this.state == state;
        }
    }

    public TaskState currentState() {
        synchronized (stateLock) {
            return this.state;
        }
    }

    // 聚合状态判断
    protected TaskState aggregateTerminalState() {
        synchronized (counterLock) {
            int total = childTasks.size();
            // 判断子任务是否全部终态
            if (completedCount + completedWithErrorsCount + failedCount + cancelledCount + skippedCount != total) {
                return null;
            }

            if (completedCount == total) {
                return TaskState.COMPLETED;
            } else if (completedWithErrorsCount == total) {
                return TaskState.COMPLETED_WITH_ERRORS;
            } else if (failedCount == total) {
                return TaskState.FAILED;
            } else if (cancelledCount == total) {
                return TaskState.CANCELLED;
            } else if (skippedCount == total) {
                return TaskState.SKIPPED;
            }
            return TaskState.COMPLETED_WITH_ERRORS;
        }
    }

    // 设置聚合状态
    protected boolean doAggregateTerminalState(TaskState terminalState) {
        if (terminalState == TaskState.COMPLETED) {
            // 全部成功
            if (transitState(TaskState.RUNNING, TaskState.COMPLETED)) {
                recordEndTimestamp();
                notifyStateChanged(TaskState.COMPLETED);
                return true;
            }
        } else if (terminalState == TaskState.CANCELLED) {
            // 全部取消
            if (transitState(TaskState.CANCELLING, TaskState.CANCELLED)) {
                recordEndTimestamp();
                notifyStateChanged(TaskState.CANCELLED);
                return true;
            }
        } else if (terminalState == TaskState.FAILED) {
            // 全部失败
            if (transitState(TaskState.RUNNING, TaskState.FAILED)) {
                recordEndTimestamp();
                notifyStateChanged(TaskState.FAILED);
                return true;
            }
        } else if (terminalState == TaskState.SKIPPED) {
            // 全部跳过
            if (transitState(TaskState.RUNNING, TaskState.SKIPPED)) {
                recordEndTimestamp();
                notifyStateChanged(TaskState.SKIPPED);
                return true;
            }
        } else if (terminalState == TaskState.COMPLETED_WITH_ERRORS) {
            // 部分成功
            if (transitState(TaskState.RUNNING, TaskState.COMPLETED_WITH_ERRORS)) {
                recordEndTimestamp();
                notifyStateChanged(TaskState.COMPLETED_WITH_ERRORS);
                return true;
            }
        }
        return false;
    }

    // 广播方法

    public void notifyStateChanged(TaskState newState) {
        for (TaskObserver observer : observers) {
            observer.onStateChanged(this, newState);
        }
    }

    public void notifyProgressUpdate(DownloadProgress progress) {
        for (TaskObserver observer : observers) {
            observer.onProgressUpdate(this, progress);
        }
    }

    public void notifyFinish(DownloadResult result) {
        for (TaskObserver observer : observers) {
            observer.onFinished(this, result);
        }
    }

    public void notifyError(Exception e) {
        for (TaskObserver observer : observers) {
            observer.onError(this, e);
        }
    }

    @Override
    public void run() {
        start();
    }
}
