# OPush / oplus-module

**面向 ColorOS / OxygenOS / OnePlus（O系）的小米推送接管 LSPosed 模块**

- **不需要全局伪装** 成 MIUI
- Hook 目标应用 MiPush SDK，强制走系统级 `com.xiaomi.xmsf`
- 禁用应用自带 `XMPushService`
- ColorOS 17：bindService 偏向 startService
- 注册 15s 超时在 LSPosed 日志提示

## 下载 APK（GitHub Actions）

1. 打开 [Actions](https://github.com/libenh/oplus-module/actions)
2. 选最新一次 **Build** 成功记录
3. 底部 **Artifacts** 下载 `OPush-APKs`
4. 解压安装 `module-debug.apk` 或 release

也可在 Actions 页点 **Run workflow** 手动触发编译。

## 安装使用

1. 安装 APK
2. LSPosed 启用模块
3. 作用域勾选：`android`、`com.xiaomi.xmsf`（若有）、目标 App
4. 重启 → 打开目标 App
5. LSPosed 日志过滤 `[OPush]`

需配合 **xmsf 推送服务**（MiPushFramework 的 com.xiaomi.xmsf）。

## 本地编译

```bash
# 需 JDK 17 +已安装 Gradle 8.9，或用 Android Studio 打开
gradle :module:assembleDebug
```

## 日志

| 关键字 | 含义 |
|--------|------|
| register REQUESTED | 发起注册 |
| REGISTER TIMEOUT | 15s 无回传 |
| block XMPushService | 已拦截自建服务 |
| bindService -> redirect | O系改 startService |
