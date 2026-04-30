# 创作者子系统

## 获取作者列表

```java
JmCreatorPage authors = client.getCreatorAuthors(1, "");
authors.getList().forEach(a ->
        System.out.printf("[%s] %s%n", a.id(), a.name()));
```

## 获取作品列表

```java
JmCreatorWorkPage works = client.getCreatorWorks(1, "", "", "");
```

## 获取指定作者的作品

```java
JmCreatorAuthorWorksPage authorWorks =
        client.getCreatorAuthorWorks("creatorId", "", "", 1);
```

## 获取作品信息与详情

```java
// 作品基本信息
JmCreatorWorkInfo info = client.getCreatorWorkInfo("workId");

// 作品详情（含更多字段）
JmCreatorWorkDetail detail = client.getCreatorWorkDetail("workId");
```
