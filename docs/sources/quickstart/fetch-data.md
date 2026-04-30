# 获取数据示例

## 获取本子详情

```java
JmAlbum album = client.getAlbum("540709");
System.out.println("标题: " + album.title());
System.out.println("作者: " + album.authors());
System.out.println("章节数: " + album.photoMetas().size());
```

## 搜索本子

```java
SearchQuery query = new SearchQuery.Builder()
        .text("NTR")
        .page(1)
        .build();

JmSearchPage result = client.search(query);
result.content().forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));
```

## 获取章节详情

```java
JmPhoto photo = client.getPhoto("1064001");
System.out.println("章节标题: " + photo.title());
System.out.println("图片数: " + photo.images().size());
System.out.println("第一张图片URL: " + photo.images().get(0).getDownloadUrl());
```

## 分类排行

```java
SearchQuery query = new SearchQuery.Builder()
        .time(TimeOption.MONTH)
        .page(1)
        .build();

JmSearchPage categories = client.getCategories(query);
categories.content().forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));
```

## 热门标签

```java
List<String> hotTags = client.getHotTags();
System.out.println("热门标签: " + hotTags);
```

## 最新上架

```java
JmSearchPage latest = client.getLatest(1);
latest.content().stream().limit(5).forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));
```

## 随机推荐

```java
List<JmAlbumMeta> random = client.getRandomRecommend();
random.forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));
```

---

更多数据获取方式见 [核心功能](../features/comic.md)。
