# JmNovelClient 接口

`JmNovelClient` 提供小说相关的操作。

## 方法列表

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getNovelList(String, int)` | `JmNovelPage` | 获取小说列表 |
| `getNovelDetail(String)` | `JmNovelDetail` | 获取小说详情 |
| `getNovelChapter(String, String)` | `JmNovelChapter` | 获取章节内容 |
| `searchNovels(String)` | `JmNovelPage` | 搜索小说 |
| `postNovelComment(String, String, String)` | `JmComment` | 发表小说评论 |
| `replyToNovelComment(String, String, String, String)` | `JmComment` | 回复小说评论 |

## 使用示例

```java
// 获取小说列表
JmNovelPage novels = client.getNovelList("mr", 1);

// 获取小说详情
JmNovelDetail detail = client.getNovelDetail("novelId");

// 获取章节内容
JmNovelChapter chapter = client.getNovelChapter("chapterId", "0");

// 搜索小说
JmNovelPage result = client.searchNovels("关键词");

// 评论
client.postNovelComment("novelId", "精彩!", null);
```
