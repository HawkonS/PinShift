# Release Process

## Signing identity

PinShift releases must use the same long-term release keystore. Losing the keystore or either password prevents future APKs from upgrading an installed release with the same application ID.

Store the following items together in an encrypted, offline backup:

- The release JKS file
- The key alias
- The keystore password
- The key password
- The SHA-1 and SHA-256 certificate fingerprints

Never commit any of those secrets. The repository ignores keystore files, keystore.properties, and built APKs.

## Local release

Create a root-level `keystore.properties` file from [examples/keystore.properties.example](./examples/keystore.properties.example) and point `storeFile` to the private JKS file. Then run from the repository root:

    bash ./gradlew --no-daemon --no-configuration-cache clean testDebugUnitTest lintDebug assembleRelease

Verify the resulting APK with Android SDK apksigner before distribution.

## GitHub Actions secrets

The release workflow expects:

- SIGNING_KEY: base64 encoding of the complete JKS file
- KEY_STORE_PASSWORD: keystore password
- ALIAS: signing-key alias
- KEY_PASSWORD: signing-key password

The workflow restores those values only on the runner, builds a signed APK, verifies it with apksigner, and attaches it to a GitHub Release for tags matching v*.

## Release checklist

1. Confirm versionCode and versionName.
2. Run unit tests, lint, and both debug and release builds.
3. Verify the APK package, version, signature, and SHA-256 checksum.
4. Confirm that no map AK, keystore, password, local configuration, or built APK is staged in Git.
5. Review the current Baidu Maps Platform agreement and confirm that the intended source-repository and APK distribution are permitted; retain any written authorization or other evidence used for that conclusion.
6. Confirm that `app/libs` contains only the intended Baidu SDK files and that their versions and provenance are recorded in [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md).
7. Commit the complete corresponding GPL-covered source and build scripts.
8. Tag the exact release commit, for example v0.0.2.
9. Publish the APK with its checksum, signing-certificate fingerprints, source tag, GPL notice, third-party notice, privacy summary, and upgrade warning. Provide access to the corresponding PinShift source revision and its build scripts alongside the release artifacts.

Debug builds and official releases use different certificates. An installed debug build normally must be uninstalled before installing the official release; uninstalling can erase local settings, map credentials, and history.
