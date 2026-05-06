package io.github.jukomu.jmcomic.api.download.task;

import io.github.jukomu.jmcomic.api.download.DownloadProgress;
import io.github.jukomu.jmcomic.api.download.DownloadResult;
import io.github.jukomu.jmcomic.api.download.enums.TaskState;

/**
 * @author JUKOMU
 * @Description: 任务观察者接口
 * @Project: JMComic-Api-Java
 * @Date: 2026/5/5
 */
public interface TaskObserver {
    void onStateChanged(BaseDownloadTask task, TaskState newState);

    void onProgressUpdate(BaseDownloadTask task, DownloadProgress progress);

    void onFinished(BaseDownloadTask task, DownloadResult result);

    void onError(BaseDownloadTask task, Exception e);
}
