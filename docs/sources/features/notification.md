# 通知与追踪

## 通知列表

```java
JmNotificationPage notifications = client.getNotifications();
```

## 标记通知

```java
// 标记为已读
client.markNotification("notificationId", 1);

// 标记为未读
client.markNotification("notificationId", 0);
```

## 未读通知数量

```java
Map unreadCount = client.getUnreadCount();
```

## 连载追踪

```java
// 获取追踪列表
JmTrackingPage tracking = client.getAlbumTrackingList(1);
tracking.getItem().forEach(item ->
        System.out.printf("[%s] %s (更新于 %s)%n",
                item.getId(), item.getName(), item.getUpdateAt()));

// 设置追踪
client.setAlbumSertracking("albumId");

// 查询追踪状态
boolean isTracking = client.getAlbumSertracking("albumId");
```

## 浏览历史

```java
// 获取历史
List<JmAlbumMeta> history = client.getWatchHistory(1);

// 删除某条历史
client.deleteWatchHistory("albumId");
```

## 任务系统

```java
JmTaskList tasks = client.getTasks("type", "filter");
```
