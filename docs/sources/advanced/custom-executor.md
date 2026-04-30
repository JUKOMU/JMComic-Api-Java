# 自定义线程池

## 注入外部线程池

```java
ExecutorService myExecutor = Executors.newFixedThreadPool(16);

try (AbstractJmClient client = JmComic.newApiClient(config)) {
    DownloadResult result = client.download(album)
            .withExecutor(myExecutor)
            .withProgress(p -> System.out.printf("%d/%d%n",
                    p.completedImages(), p.totalImages()))
            .execute();
} finally {
    myExecutor.shutdown();
    myExecutor.awaitTermination(1, TimeUnit.MINUTES);
}
```

## 注意事项

- 外部传入的线程池**不会被 client.close() 关闭**，需要调用方自行管理生命周期
- 建议在 finally 块中确保线程池关闭，或使用 try-with-resources 包装
- 当注入外部线程池时，`downloadThreadPoolSize` 配置项自动失效（被设为 `-1`）

## 通过配置控制线程池大小

不注入外部线程池时，通过 `JmConfiguration` 控制：

```java
JmConfiguration config = new JmConfiguration.Builder()
        .downloadThreadPoolSize(12)   // 12 个下载线程
        .build();
```
