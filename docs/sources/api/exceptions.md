# 异常类型

所有异常均继承自 `JmComicException`（`RuntimeException` 的子类）。

## 异常层次

```
RuntimeException
  └── JmComicException          基类
        ├── NetworkException     网络请求失败
        ├── ResponseException    服务器返回错误
        ├── ParseResponseException  解析响应失败
        └── ResourceNotFoundException  资源不存在
              ├── AlbumNotFoundException  本子不存在
              └── PhotoNotFoundException   章节不存在
```

## 异常说明

| 异常 | 触发场景 |
|------|----------|
| `JmComicException` | 通用 JM 相关异常基类 |
| `NetworkException` | 网络连接失败、超时等 |
| `ResponseException` | 服务端返回非预期状态码 |
| `ParseResponseException` | JSON/HTML 解析失败 |
| `ResourceNotFoundException` | 请求的资源 ID 不存在 |
| `AlbumNotFoundException` | 本子 ID 不存在 |
| `PhotoNotFoundException` | 章节 ID 不存在 |

## 处理建议

```java
try {
    JmAlbum album = client.getAlbum("123");
} catch (AlbumNotFoundException e) {
    System.err.println("本子不存在: " + e.getMessage());
} catch (NetworkException e) {
    System.err.println("网络错误: " + e.getMessage());
} catch (JmComicException e) {
    System.err.println("其他错误: " + e.getMessage());
}
```
