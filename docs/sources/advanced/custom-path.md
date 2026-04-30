# 自定义下载路径

库提供了三种粒度的路径策略接口，你可以选择最适合的方式控制文件保存位置。

## 方式1：本子级路径生成器

控制本子目录的命名：

```java
IAlbumPathGenerator albumPath = album ->
        Path.of("downloads", album.getId());
```

## 方式2：章节级路径生成器

控制每个章节子目录的命名：

```java
IPhotoPathGenerator photoPath = photo ->
        Path.of(String.format("%03d", photo.getSortOrder()));
```

## 方式3：完整路径生成器

一次性控制 album / photo / image 三层完整路径：

```java
IDownloadPathGenerator totalPath = (album, photo, image) ->
        Path.of(album.getId(),
                String.format("%03d", photo.getSortOrder()),
                image.getFilename());
```

## 策略组合

通过 `andThen` 组合多个路径转换：

```java
IAlbumPathGenerator generator = new DefaultAlbumPathGenerator()
        .andThen(path -> path.resolve("_backup"))
        .andThen(path -> Path.of("/downloads").resolve(path));
```

默认路径格式为 `{author}/{title}/{albumId}`，`andThen` 可在此基础上追加更多层级。

## 使用方式

```java
// 本子级生成器
client.downloadAlbum(album, albumPath);

// 章节级生成器
client.downloadPhoto(photo, photoPath);

// 完整路径生成器
client.downloadAlbum(album, totalPath, executor);
```

## 自定义实现示例

```java
// 按日期归档
String today = LocalDate.now().toString();
IAlbumPathGenerator dateBased = album ->
        Path.of("downloads", today, album.getId());

// 自定义文件名格式
IDownloadPathGenerator custom = (album, photo, image) ->
        Path.of(album.getId(),
                photo.getTitle(),
                String.format("%03d_%s", image.getSortOrder(), image.getFilename()));
```
