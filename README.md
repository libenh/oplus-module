# OPush / oplus-module

**面向 ColorOS / OxygenOS / OnePlus（O系）的小米推送接管模块**

- 不依赖全局设备伪装
- 通过 LSPosed 强制接管目标应用的小米推送注册与消息通道
- 针对 ColorOS 17 注册无回传、消息转发失败问题做专门处理
- 系统应用安装为可选增强项

## 目标

1. 解决 ColorOS 17 上「大部分应用注册不上 / 注册后一点反应都没有」
2. 消息可靠转发（优先 startService 路径）
3. 注册过程有明确日志与超时反馈

## 架构

```
module/                 # LSPosed 模块主体
  hooks/                # Hook 目标应用 MiPush SDK
  coloros/              # C17 特殊兼容
```

## 状态

初始骨架已就绪，核心 Hook 与注册逻辑进行中。

## 环境要求

- Root（Magisk / KernelSU / APatch）
- LSPosed（或兼容框架）
- ColorOS 15+（优先适配 17）
