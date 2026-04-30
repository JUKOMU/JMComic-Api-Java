# Android 集成

## 添加依赖

```groovy
// build.gradle (app)
dependencies {
    implementation 'io.github.jukomu:jmcomic-core:1.0.0'
    implementation 'io.github.jukomu:jmcomic-android-support:1.0.0'
}
```

## 使用方式

Android 平台下使用方式与标准 Java 一致：

```java
JmConfiguration config = new JmConfiguration.Builder()
        .clientType(ClientType.API)
        .build();

try (AbstractJmClient client = JmComic.newApiClient(config)) {
    JmAlbum album = client.getAlbum("1064000");
    // ...
}
```

## 注意事项

- 网络请求需在**子线程**执行（Android 主线程禁止网络操作）
- `jmcomic-core` 依赖 OkHttp，Android 平台自带，无需额外配置
- `jmcomic-android-support` 提供 Android 平台特定的适配（如图片处理）
- 下载文件需要 `WRITE_EXTERNAL_STORAGE` 或应用私有目录权限
