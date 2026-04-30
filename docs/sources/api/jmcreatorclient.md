# JmCreatorClient 接口

`JmCreatorClient` 提供创作者相关的操作。

## 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getCreatorAuthors(int, String)` | `JmCreatorPage` | 获取作者列表 |
| `getCreatorWorks(int, String, String, String)` | `JmCreatorWorkPage` | 获取作品列表 |
| `getCreatorAuthorWorks(String, String, String, int)` | `JmCreatorAuthorWorksPage` | 获取指定作者的作品 |
| `getCreatorWorkInfo(String)` | `JmCreatorWorkInfo` | 获取作品信息 |
| `getCreatorWorkDetail(String)` | `JmCreatorWorkDetail` | 获取作品详情 |

## 使用示例

```java
// 获取作者列表
JmCreatorPage authors = client.getCreatorAuthors(1, "");

// 获取作品列表
JmCreatorWorkPage works = client.getCreatorWorks(1, "", "", "");

// 获取指定作者的作品
JmCreatorAuthorWorksPage authorWorks =
        client.getCreatorAuthorWorks("creatorId", "", "", 1);

// 作品详情
JmCreatorWorkInfo info = client.getCreatorWorkInfo("workId");
JmCreatorWorkDetail detail = client.getCreatorWorkDetail("workId");
```
