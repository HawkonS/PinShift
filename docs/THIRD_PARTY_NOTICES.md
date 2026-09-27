# Third-Party Notices

This file records the principal third-party components used by PinShift. It is informational and does not replace the complete license text or service terms supplied by each upstream provider.

## Runtime components

| Component | Version used by this project | Upstream terms |
| --- | --- | --- |
| AndroidX AppCompat, ConstraintLayout, Lifecycle and Preference | See app/build.gradle | Apache License 2.0 |
| Material Components for Android | 1.7.0 | Apache License 2.0 |
| OkHttp | 4.12.0 | Apache License 2.0 |
| XLog | 1.11.1 | Apache License 2.0 |
| Baidu Map and Location SDK binaries | Bundled map SDK 8.2.0 and location SDK 9.7.0 vendor binaries | Baidu Maps Platform terms, SDK privacy rules, and applicable service policies |

## Test-only components

JUnit, AndroidX Test, and Espresso are used for tests and are not application features. Their upstream license notices remain applicable.

## Baidu SDK notice

The repository intentionally contains a vendor JAR and arm64-v8a native libraries under `app/libs` so that the application remains directly buildable. Release APKs intentionally incorporate those components. These binaries are not PinShift source code, are not covered by GPL-3.0-only, and are not relicensed by this repository.

The binaries were inherited from the upstream project for build continuity. That provenance is attribution only: the upstream GoGoGo project and the PinShift maintainers cannot grant rights on Baidu's behalf. A distributor must independently confirm that the current Baidu Maps Platform agreement permits distribution of the raw SDK files and any APK incorporating them, and must assess compatibility with all source-code license obligations. If the required permission or compatibility cannot be confirmed, the distributor should not publish the APK or vendor binaries.

Applications using the SDK must also present accurate privacy disclosures and obtain any consent required before initializing the SDK. PinShift initializes the Baidu SDK only after the user has accepted the in-app privacy notice and supplied a map-platform credential.

Official references:

- [Baidu Maps Platform](https://lbsyun.baidu.com/)
- [Android SDK key registration guide](https://lbs.baidu.com/faq/api?title=androidsdk/guide/create-project/ak)
- [Baidu Maps Platform service terms](https://lbsyun.baidu.com/index.php?title=open/law)

## No additional grant

Inclusion of a component, binary, source link, or notice here does not grant trademark, patent, service-access, sublicensing, or binary-redistribution rights or imply endorsement by the upstream providers. This notice documents the project's composition; it is not legal advice or an additional license. Consult the current upstream terms and obtain any required permission before publishing a modified build.
