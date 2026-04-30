# 发现与浏览

## 热门标签

```java
List<String> hotTags = client.getHotTags();
System.out.println("热门: " + hotTags);
```

## 最新上架

```java
JmSearchPage latest = client.getLatest(1);
latest.content().forEach(m ->
        System.out.printf("[%s] %s%n", m.id(), m.title()));
```

## 随机推荐

```java
List<JmAlbumMeta> random = client.getRandomRecommend();
```

## 每周必看

```java
// 获取期数列表
JmWeeklyPicksList picks = client.getWeeklyPicksList();
picks.getCategories().forEach(cat ->
        System.out.printf("%s (id=%s)%n", cat.getTitle(), cat.getId()));

// 获取某期详情
JmWeeklyPicksDetail detail = client.getWeeklyPicksDetail("categoryId");
```

## 首页推广

```java
Map promote = client.getPromote();
```

## 连载系列

```java
JmSearchPage serialization = client.getSerialization(1);
```
