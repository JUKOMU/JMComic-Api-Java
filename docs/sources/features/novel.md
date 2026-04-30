# 小说子系统

## 获取小说列表

```java
JmNovelPage novels = client.getNovelList("mr", 1);
novels.getList().forEach(n ->
        System.out.printf("[%s] %s%n", n.id(), n.name()));
```

## 获取小说详情

```java
JmNovelDetail detail = client.getNovelDetail("novelId");
System.out.println("标题: " + detail.name());
System.out.println("作者: " + detail.author());
```

## 获取章节内容

```java
JmNovelChapter chapter = client.getNovelChapter("chapterId", "0");
System.out.println("章节正文: " + chapter.getContent());
```

## 搜索小说

```java
JmNovelPage result = client.searchNovels("关键词");
```

## 小说评论

```java
// 发表评论
client.postNovelComment("novelId", "精彩!", null);

// 回复评论
client.replyToNovelComment("novelId", "同感", parentCommentId, null);
```
