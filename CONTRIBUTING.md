# Contributing

Thank you for considering a contribution to PinShift. Issues and focused pull requests are welcome.

## Development

You will need Git, JDK 17, and the Android SDK. Open the project in Android Studio or configure the SDK path for your environment. Do not commit `local.properties`.

Run the unit tests and static checks from the repository root before opening a pull request:

```bash
bash ./gradlew --no-daemon --no-configuration-cache testDebugUnitTest lintDebug
```

To build a debug APK, run:

```bash
bash ./gradlew --no-daemon --no-configuration-cache assembleDebug
```

## Issues

Before opening an issue, check whether it has already been reported. Include the app version, Android version and device, steps to reproduce, and the expected and actual results. Remove credentials, personal information, and unrelated log data before sharing logs. For security issues, use GitHub's private vulnerability reporting feature when available instead of posting exploit details publicly.

## Pull requests

- Keep each pull request focused on one problem or improvement and target the `main` branch.
- Explain the problem and the change, link a related issue when there is one, and complete the repository pull request template.
- Add or update tests when behavior changes. Include a screenshot for user-visible interface changes.
- Update user-facing documentation when behavior or configuration changes.
- Do not include map credentials, signing keys, passwords, `local.properties`, generated APKs, logs, or other private runtime data.
- Identify new dependencies and third-party code clearly, and make sure their licenses and distribution terms are compatible with the project.
- Run the checks listed under Development and report any that you could not run.

## Commit messages

Follow [Conventional Commits](https://www.conventionalcommits.org/) using the project rules below:

```text
type(scope): description
```

- Write the complete title in English. Keep the type, scope, and description lowercase; start the description with a short imperative verb such as `add`, `fix`, `remove`, `update`, or `document`.
- Always include a scope identifying the affected area. Prefer one of the project scopes listed below. Use another short, lowercase scope only when the change belongs to a distinct area not covered by them.
- Separate the type and scope with parentheses, then write a colon and one space before the description: `type(scope): description`.
- Keep the title on exactly one line, with no body, period, or emoji. Use a single `-m` when committing; do not add a second `-m` for a body.
- Aim to keep the complete title within 72 characters. Describe the actual change and its purpose; avoid vague descriptions such as `update code` or `fix bug`.
- Keep each commit focused on one logical change. Split unrelated fixes, features, tests, and refactors into separate commits.
- For an incompatible change, place `!` after the scope and before the colon, for example `feat(app)!: change location permission handling`.
- When using squash merges, format the pull request title as a commit title too. The pull request description should still explain the context, implementation, and verification.

Common types:

| Type | Use for |
| --- | --- |
| `feat` | A user-visible feature |
| `fix` | A defect or compatibility fix |
| `refactor` | Code restructuring without behavior changes |
| `perf` | A performance improvement |
| `docs` | Documentation changes |
| `test` | Test additions or updates |
| `build` | Dependencies, packaging, or build configuration |
| `ci` | CI workflows and automation |
| `chore` | Maintenance that does not fit another type |

Common scopes:

| Scope | Use for |
| --- | --- |
| `app` | Application behavior, UI, location services, and history |
| `android` | Android platform compatibility, manifest, and native SDK integration |
| `build` | Gradle and local build configuration |
| `ci` | GitHub Actions workflows |
| `docs` | README and documentation under `docs/` |
| `repo` | Repository-level configuration and collaboration guidelines |

Examples:

```text
feat(app): add manual coordinate entry
fix(android): update Baidu SDK for 16 KB page alignment
test(app): cover coordinate parsing edge cases
docs(repo): document open-source contribution workflow
```

## License

PinShift is distributed under the GNU General Public License v3.0 only. By submitting a contribution, you confirm that you have the right to contribute it and agree that it may be distributed under the same license. See [LICENSE](./LICENSE) and retain existing copyright, attribution, and license notices.
