# 漫画

## 获取本子详情

```java
JmAlbum album = client.getAlbum("540709");
System.out.println("标题: " + album.title());
System.out.println("作者: " + album.authors());
System.out.println("章节数: " + album.photoMetas().size());

// 遍历章节
album.photoMetas().forEach(meta ->
        System.out.printf("  [%s] %s%n", meta.getId(), meta.getTitle()));
```

`JmAlbum` 包含本子的完整信息：标题、作者、标签、章节元数据列表等。

## 获取章节详情

```java
// 方式1: 通过章节ID直接获取
JmPhoto photo = client.getPhoto("1064001");

// 方式2: 从本子中获取某个章节
JmAlbum album = client.getAlbum("540709");
JmPhoto photo = client.getPhoto(album.getPhotoMeta(1).getId());

System.out.println("章节标题: " + photo.title());
System.out.println("图片数: " + photo.images().size());
```

`JmPhoto` 包含该章节所有图片的信息（包括下载 URL）。

## 搜索本子

```java
SearchQuery query = new SearchQuery.Builder()
        .text("关键词")
        .page(1)
        .build();

JmSearchPage result = client.search(query);
System.out.println("总页数: " + result.totalPages());
result.content().forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));
```

### 搜索参数

`SearchQuery.Builder` 支持的参数：

```java
new SearchQuery.Builder()
        .text("关键词")           // 搜索关键词
        .page(1)                  // 页码（从 1 开始）
        .time(TimeOption.MONTH)   // 时间范围: ALL, TODAY, WEEK, MONTH
        .orderBy(OrderBy.LATEST)  // 排序: LATEST, MOST_VIEWED, MOST_IMAGES, MOST_LIKED
        .category(Category.ALL)   // 分类（可选）
        .mainTag(SearchMainTag.SITE_SEARCH)     // 搜索类型
        .build();
```

## 分类排行

```java
SearchQuery query = new SearchQuery.Builder()
        .time(TimeOption.MONTH)
        .page(1)
        .build();

JmSearchPage result = client.getCategories(query);
```

## 分类列表

获取完整的分类树和标签块：

```java
JmCategoryList categories = client.getCategoriesList();

// 标签区块
for (JmCategoryBlock block : categories.blocks()) {
    System.out.println("区块: " + block.title());
    block.content().forEach(item ->
            System.out.println("  - " + item));
}

// 分类树
categories.categories().forEach(item ->
        System.out.printf("[%s] %s%n", item.id(), item.name()));
```
