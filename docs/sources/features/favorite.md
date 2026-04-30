# 收藏管理

## 获取收藏夹

```java
JmFavoritePage favPage = client.getFavorites(
        new FavoriteQuery.Builder()
                .folderId(0)   // 文件夹ID
                .page(1)       // 页码
                .build());

System.out.println("总数: " + favPage.getTotalItems());
favPage.getContent().forEach(meta ->
        System.out.printf("[%s] %s%n", meta.id(), meta.title()));
```

## 切换收藏状态

```java
client.toggleAlbumFavorite("1064000", "0");
```

## 管理收藏文件夹

```java
// 添加文件夹
client.manageFavoriteFolder(
        FavoriteFolderType.ADD, "0", "新文件夹名", null);

// 重命名文件夹
client.manageFavoriteFolder(
        FavoriteFolderType.EDIT, "folderId", "新名字", null);

// 删除文件夹
client.manageFavoriteFolder(
        FavoriteFolderType.DELETE, "folderId", null, null);

// 移动本子到指定文件夹
client.manageFavoriteFolder(
        FavoriteFolderType.MOVE, "targetFolderId", null, "albumId");
```

## 收藏标签

```java
// 获取标签列表
List<JmTagFavorite> tags = client.getTagsFavorite();

// 添加标签
client.addFavoriteTags(List.of("标签1", "标签2"));

// 删除标签
client.removeFavoriteTags(List.of("标签1"));
```
