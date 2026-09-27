# PinShift

<p align="center">
  <img src="./docs/pinshift-app-icon-preview.png" height="112" alt="PinShift icon" />
</p>

<p align="center">
  Mock-location and diagnostics utility for Android 8.0+ without root
</p>

<p align="center">
  <a href="./README.md">简体中文</a> ·
  <a href="https://github.com/HawkonS/PinShift/releases">Download</a>
</p>

PinShift provides map selection, place search, coordinate input, simulated movement, local history, and location diagnostics for development, testing, and learning. It does not provide a shared map credential; each user supplies and stores their own credential locally.

## Features

- Search, select, or enter a location
- Send Android mock locations and simulate movement
- Store local location and search history
- Diagnose providers, permissions, and mock-location state
- Display the information required to request a map credential
- Configure or clear the credential in the app

## Getting started

1. Select PinShift as the mock-location app in Android developer options.
2. Open Map Configuration and copy the displayed PackageName and certificate fingerprints.
3. Create an Android SDK application on Baidu Maps Platform and request an AK.
4. Save the AK in PinShift and restart when prompted.

## Build

JDK 17 and the Android SDK are required.

```bash
bash ./gradlew --no-daemon --no-configuration-cache testDebugUnitTest lintDebug assembleDebug
```

Release builds require a private signing identity. Never commit signing files, passwords, or user map credentials. See [RELEASING.md](./RELEASING.md).

## Privacy and responsible use

The maintainers operate no application backend. Map search, positioning, and reverse geocoding are provided by Baidu SDKs and APIs and may transmit relevant data to Baidu. Third-party apps may detect or reject mock locations. Use PinShift only for learning, development, and authorized testing.

## License and third-party components

PinShift is derived from [ZCShou/GoGoGo](https://github.com/ZCShou/GoGoGo). PinShift-authored and derived code is distributed under [GNU GPL v3.0 only](./LICENSE), with original copyright and contributor attribution retained.

The repository retains Baidu Map and Location SDK binaries so the project remains buildable, and release APKs incorporate them. Those binaries are not covered by this repository's GPL license and remain subject to Baidu Maps Platform terms. See [NOTICE.md](./NOTICE.md) and [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md).
