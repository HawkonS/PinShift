# PinShift

<p align="center">
  <img src="./docs/pinshift-app-icon-preview.png" height="112" alt="PinShift icon" />
</p>

<p align="center">
  A root-free mock-location and diagnostics tool for Android 8.0+
</p>

<p align="center">
  <a href="./README.md">中文</a> ·
  English
</p>

<p align="center">
  <a href="https://github.com/HawkonS/PinShift/actions/workflows/build-check.yml"><img src="https://github.com/HawkonS/PinShift/actions/workflows/build-check.yml/badge.svg" alt="Build Check" /></a>
  <a href="https://github.com/HawkonS/PinShift/actions/workflows/codeql-analysis.yml"><img src="https://github.com/HawkonS/PinShift/actions/workflows/codeql-analysis.yml/badge.svg" alt="CodeQL" /></a>
  <a href="./LICENSE"><img src="https://img.shields.io/badge/license-GPL--3.0--only-blue.svg" alt="License: GPL-3.0-only" /></a>
</p>

## Introduction

PinShift is for development, testing, and learning. It provides map selection, place search, coordinate input, simulated movement, local history, and location diagnostics. The app does not provide a shared map credential; each user obtains a Baidu Maps AK and stores it locally.

## Features

- Search, select, or enter a location
- Send Android mock locations and simulate movement with a floating joystick
- Keep local location and search history
- Diagnose location providers, permissions, and mock-location state
- Show the information needed to request a map credential
- Configure or clear the credential in the app

## Quick Start

1. [Download the official APK](https://github.com/HawkonS/PinShift/releases/latest) and install it. The current release supports Android 8.0+ and `arm64-v8a`.
2. In Android developer options, select PinShift as the mock-location app.
3. Open Map Configuration and copy the displayed PackageName and release SHA1. Use them to create an Android SDK app and request an AK on Baidu Maps Platform.
4. Save the AK in PinShift and restart the app when prompted.

Baidu guide: [Register and obtain an API key](https://lbs.baidu.com/faq/api?title=androidsdk/guide/create-project/ak)

## Development

Development requires JDK 17 and the Android SDK. Run the unit tests and static checks with:

```bash
bash ./gradlew --no-daemon --no-configuration-cache testDebugUnitTest lintDebug
```

Report issues or suggest improvements through Issues and Pull Requests.

## Build

Run this command from the repository root to build a Debug APK:

```bash
bash ./gradlew --no-daemon --no-configuration-cache assembleDebug
```

Official releases require a separate signing identity. Never commit signing files, passwords, or user map credentials. See the [release guide](./docs/RELEASING.md) for signing configuration and release steps.

## Security

- The maintainers do not operate an application backend.
- Map search, positioning, and reverse geocoding are provided by Baidu SDKs and APIs and may transmit relevant data to Baidu.
- Protect your map AK. Never commit signing files, passwords, `local.properties`, or map credentials to Git.
- Third-party apps may detect or reject mock locations. Use PinShift only for learning, development, and authorized testing; do not use it to deceive, cheat, invade privacy, or bypass security controls.

## License

PinShift is an independently maintained derivative of [ZCShou/GoGoGo](https://github.com/ZCShou/GoGoGo), modified by HawkonS and subsequent contributors since September 26, 2026. Copyright © ZCShou and the original contributors for the original work and Git history; copyright © HawkonS and subsequent contributors for PinShift modifications. PinShift-authored and derived code is distributed under [GNU GPL v3.0 only](./LICENSE), with original copyright, attribution, and license notices retained. PinShift does not imply endorsement by the original author.

The repository retains Baidu Map and Location SDK binaries so it remains buildable, and release APKs incorporate those components. The Baidu SDKs are not covered by this repository's GPL license; their use and distribution remain subject to Baidu Maps Platform terms. See the [third-party notices](./docs/THIRD_PARTY_NOTICES.md).
