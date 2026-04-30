# 评论系统

## 获取评论列表

支持多种实体类型的评论：本子、小说、小说章节、博客、用户等。

```java
// 获取本子评论
JmCommentList comments = client.getComments(
        ForumQuery.album("1173049")
                .mode(ForumMode.ALL)
                .page(1)
                .build());

System.out.println("总评论数: " + comments.getTotal());
comments.getList().forEach(c ->
        System.out.println(c.getUsername() + ": " + c.getContent()));
```

`ForumQuery` 静态工厂方法：

```java
ForumQuery.album("albumId")              // 本子评论
ForumQuery.novel("novelId")              // 小说评论
ForumQuery.novelChapter("novelId", "ch") // 小说章节评论
ForumQuery.blog("blogId")               // 博客评论
ForumQuery.user("userId")                // 用户评论
```

## 发表评论

```java
// 发表本子评论（需先登录）
JmComment comment = client.postComment("1173049", "好看!");
```

## 回复评论

```java
JmComment reply = client.replyToComment(
        "1173049",       // 本子ID
        "+1",            // 评论内容
        parentCommentId  // 被回复的评论ID
);
```

## 博客评论

```java
// 发表博客评论
client.postBlogComment("albumId", "blogId", "评论内容");

// 回复博客下的评论
client.replyToBlogComment("albumId", "blogId", "内容", parentCommentId);
```
