# 移点（PinShift）

<p align="center">
  <img src="./docs/pinshift-app-icon-preview.png" height="112" alt="移点图标" />
</p>

<p align="center">
  Android 8.0+ 无需 Root 的模拟位置与定位诊断工具
</p>

<p align="center">
  中文 ·
  <a href="./README_EN.md">English</a>
</p>

<p align="center">
  <a href="https://github.com/HawkonS/PinShift/actions/workflows/build-check.yml"><img src="https://github.com/HawkonS/PinShift/actions/workflows/build-check.yml/badge.svg" alt="Build Check" /></a>
  <a href="https://github.com/HawkonS/PinShift/actions/workflows/codeql-analysis.yml"><img src="https://github.com/HawkonS/PinShift/actions/workflows/codeql-analysis.yml/badge.svg" alt="CodeQL" /></a>
  <a href="./LICENSE"><img src="https://img.shields.io/badge/license-GPL--3.0--only-blue.svg" alt="License: GPL-3.0-only" /></a>
</p>

## 简介

移点（PinShift）面向开发、测试和学习场景，提供地图选点、位置搜索、坐标输入、模拟移动、历史记录与定位状态诊断。应用不提供公共地图密钥，用户自行申请百度地图 AK 并保存在本机。

## 功能

- 地图搜索、选点和经纬度输入
- Android 模拟位置与悬浮摇杆移动
- 位置记录和搜索历史
- 系统定位 Provider、权限及模拟位置状态诊断
- 应用内展示地图凭证申请所需信息
- 用户自主配置和清除地图访问凭证

## 快速开始

1. [下载正式版 APK](https://github.com/HawkonS/PinShift/releases/latest) 并安装。当前正式版支持 Android 8.0+ 和 `arm64-v8a`。
2. 在 Android 开发者选项中，将“选择模拟位置信息应用”设置为“移点”。
3. 打开应用的“地图配置”，复制页面显示的 PackageName 和发布版 SHA1，在百度地图开放平台创建 Android SDK 应用并申请 AK。
4. 返回应用保存 AK，并按提示重新启动。

百度官方说明：[注册和获取密钥](https://lbs.baidu.com/faq/api?title=androidsdk/guide/create-project/ak)

## 开发

开发环境需要 JDK 17 和 Android SDK。运行单元测试和静态检查：

```bash
bash ./gradlew --no-daemon --no-configuration-cache testDebugUnitTest lintDebug
```

欢迎通过 Issue 和 Pull Request 报告问题或提交改进。

## 构建

在仓库根目录运行以下命令构建 Debug APK：

```bash
bash ./gradlew --no-daemon --no-configuration-cache assembleDebug
```

正式版构建需要独立签名。签名文件、密码和用户地图凭证不得提交到 Git。签名配置和发布步骤见[发布指南](./docs/RELEASING.md)。

## 安全说明

- 项目维护者不运营应用业务服务器。
- 地图搜索、定位和逆地理编码由百度地图 SDK/API 提供，相关数据可能发送至百度服务器。
- 请妥善保管自己的地图 AK；签名文件、密码、`local.properties` 和地图凭证不得提交到 Git。
- 第三方应用可能识别或拒绝模拟位置；请仅在学习、开发和获得授权的测试环境中使用，不得用于欺骗、作弊、侵犯隐私或规避安全机制。

## License

PinShift 是 [ZCShou/GoGoGo](https://github.com/ZCShou/GoGoGo) 的独立衍生项目，自 2026 年 9 月 26 日起由 HawkonS 及后续贡献者修改和维护，不代表原作者背书。原始作品及 Git 历史版权 © ZCShou 和原贡献者；PinShift 修改部分版权 © HawkonS 及后续贡献者。PinShift 自有代码及派生代码依据 [GNU GPL v3.0 only](./LICENSE) 发布，原有版权、署名和许可声明继续保留。

仓库保留了百度地图与定位 SDK 二进制以保证项目可构建，正式 APK 也会集成这些组件。百度 SDK 不适用本仓库的 GPL 许可，其使用和分发受百度地图开放平台条款约束。详见[第三方组件声明](./docs/THIRD_PARTY_NOTICES.md)。
