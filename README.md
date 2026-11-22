# OPush / oplus-module

**面向 ColorOS / OxygenOS / OnePlus（O系）的小米推送接管 LSPosed 模块**

- **不需要全局伪装** 成 MIUI
- Hook 目标应用 MiPush SDK，强制走系统级 `com.xiaomi.xmsf`
- 禁用应用自带 `XMPushService`，减少双通道
- ColorOS 17：bindService 改走 startService
- 注册 15s 超时无回传会在 LSPosed 日志打出明确提示

## 安装

1. 编译或使用 Release APK 安装
2. LSPosed 中启用模块
3. 作用域至少勾选：
   - `android`（系统框架）
   - `com.xiaomi.xmsf`（若已安装推送服务）
   - 需要推送的目标应用（酷安、QQ 等）
4. 重启手机
5. 打开目标应用，在 LSPosed 日志过滤 `[OPush]`

## 日志关键字

| 日志 | 含义 |
|------|------|
| `register REQUESTED` | 应用发起了注册 |
| `REGISTER TIMEOUT` | 15s 内无结果，需检查 xmsf |
| `block XMPushService` | 已拦截应用自建长连接 |
| `bindService -> redirect startService` | C17 下已改走 startService |

## 依赖

仍需配合 **推送服务 APK**（`com.xiaomi.xmsf`，可来自 MiPushFramework 的 xmsf）。  
本模块负责让应用把注册/消息走系统通道，不替代 xmsf 本体。

## 构建

```bash
./gradlew :module:assembleDebug
```

## 状态

- [x] 模块入口 + O系检测
- [x] Hook registerPush + 注册超时
- [x] 拦截应用 XMPushService
- [x] bind -> startService（O系）
- [x] MIUI 检查绕过
- [ ] 与 xmsf 深度联调 / 强制注册结果回传
- [ ] 管理界面
