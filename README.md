# 移点（PinShift）

<p align="center">
  <img src="./docs/pinshift-app-icon-preview.png" height="112" alt="移点图标" />
</p>

<p align="center">
  Android 8.0+ 无需 Root 的位置模拟与定位诊断工具
</p>

<p align="center">
  <a href="./README.en.md">English</a> ·
  <a href="https://github.com/HawkonS/PinShift/releases">下载正式版</a>
</p>

[![Build Check](https://github.com/HawkonS/PinShift/actions/workflows/build-check.yml/badge.svg)](https://github.com/HawkonS/PinShift/actions/workflows/build-check.yml)
[![CodeQL](https://github.com/HawkonS/PinShift/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/HawkonS/PinShift/actions/workflows/codeql-analysis.yml)
[![License: GPL v3](https://img.shields.io/badge/license-GPL--3.0--only-blue.svg)](./LICENSE)

PinShift 面向开发、测试和学习场景，提供地图选点、位置搜索、坐标输入、模拟移动、历史记录与定位状态诊断。应用不提供公共地图密钥，地图访问凭证由用户自行申请并保存在本机。

## 功能

- 地图搜索、选点和经纬度输入
- Android 模拟位置与悬浮摇杆移动
- 位置记录和搜索历史
- 系统定位 Provider、权限及模拟位置状态诊断
- 应用内展示地图凭证申请所需信息
- 用户自主配置和清除地图访问凭证

## 快速开始

1. 在 Android 开发者选项中，将“选择模拟位置信息应用”设置为“移点”。
2. 打开应用的“地图配置”，复制页面显示的 PackageName、开发版 SHA1 和发布版 SHA1。
3. 在百度地图开放平台创建 Android SDK 应用，并使用上述信息申请 AK。
4. 返回应用保存 AK，按提示重新启动应用。

百度官方说明：[注册和获取密钥](https://lbs.baidu.com/faq/api?title=androidsdk/guide/create-project/ak)

## 构建

项目需要 JDK 17 和 Android SDK。

```bash
bash ./gradlew --no-daemon --no-configuration-cache testDebugUnitTest lintDebug assembleDebug
```

正式构建需要独立签名，签名文件、密码和用户地图凭证均不得提交到 Git。发布流程见 [RELEASING.md](./RELEASING.md)。

## 隐私与使用边界

- 项目维护者不运营应用业务服务器。
- 地图搜索、定位和逆地理编码由百度地图 SDK/API 提供，相关数据可能发送至百度服务器。
- 第三方应用可能识别或拒绝模拟位置，本项目不保证在任何第三方应用中生效。
- 请仅用于学习、开发及获得授权的测试环境，不得用于欺骗、作弊、侵犯隐私或规避安全机制。

## 开源与第三方组件

PinShift 是 [ZCShou/GoGoGo](https://github.com/ZCShou/GoGoGo) 的衍生项目。PinShift 自有代码及派生代码依据 [GNU GPL v3.0 only](./LICENSE) 发布，原项目版权和贡献者署名继续保留。

仓库为保证可构建性保留了百度地图与定位 SDK 二进制，正式 APK 也会集成这些组件。百度 SDK 不适用本仓库的 GPL 许可，其使用和分发受百度地图开放平台条款约束。详细说明见 [NOTICE.md](./NOTICE.md) 和 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。

## 参与维护

欢迎通过 Issue 和 Pull Request 报告问题或提交改进。
