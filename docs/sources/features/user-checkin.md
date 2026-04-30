# 用户与签到

## 登录

```java
JmUserInfo user = client.login("username", "password");
System.out.println("用户ID: " + user.getUid());
```

## 登出

```java
client.logout();
```

## 获取用户资料

```java
JmUserProfile profile = client.getUserProfile(user.getUid());
```

## 编辑用户资料

```java
client.editUserProfile(user.getUid(),
        Map.of("nickname", "新昵称"));
```

## 每日签到

```java
// 获取签到状态
JmDailyCheckInStatus status = client.getDailyCheckInStatus(user.getUid());

// 执行签到
client.doDailyCheckin(user.getUid(), status.dailyId());
```

## 签到历史

```java
// 获取可筛选的年份选项
List options = client.getDailyCheckInOptions(user.getUid());

// 按年份筛选签到记录
List records = client.filterDailyCheckInList("2025");
```
