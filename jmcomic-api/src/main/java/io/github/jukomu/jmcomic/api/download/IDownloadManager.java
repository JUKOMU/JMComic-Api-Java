package io.github.jukomu.jmcomic.api.download;

import io.github.jukomu.jmcomic.api.download.task.BaseDownloadTask;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 下载任务管理器接口
 * @Project: JMComic-Api-Java
 * @Date: 2026/5/4
 */
public interface IDownloadManager {

    BaseDownloadTask getTask(String taskId);

    List<BaseDownloadTask> getActiveTasks();

    List<BaseDownloadTask> getTaskRegistry();

    void submit(BaseDownloadTask task);

    void pause(String taskId);

    void resume(String taskId);

    void cancel(String taskId);

    void close();
}
