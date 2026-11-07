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

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) for every commit message and pull request title:

```text
type(scope): summary
```

- **English is required** for the entire title, including the scope and summary.
- **A scope is required**. Start it with a lowercase English letter and use only lowercase English letters, digits, and hyphens.
- **The entire commit message must be a single title line**. Do not add a body, footers, trailers, or a second `-m` argument.
- **The complete title must be 72 characters or fewer**, including the type, scope, spaces, and punctuation.
- Keep the type and scope lowercase. Start the summary with a lowercase imperative verb, such as `add`, `fix`, `remove`, `update`, or `document`.
- Proper names and technical identifiers in the summary may retain their original capitalization, such as `Android`, `GitHub`, `SDK`, and `JAVA_HOME`.
- Do not end the title with a period or whitespace, and do not use emoji.
- Keep each commit focused on one logical change. Describe the actual change; avoid vague summaries such as `update code` or `fix bug`.
- For an incompatible change, put `!` after the scope and before the colon: `type(scope)!: summary`.
- Put context, issue references, validation details, and migration notes in the pull request description.
- Apply the same rules to pull request titles and final squash-merge commit messages. When squash-merging, keep only the title and remove any automatically generated body or trailers.

Use one of these types:

| Type | Use for |
| --- | --- |
| `feat` | New user-visible functionality |
| `fix` | Bug, regression, or compatibility fixes |
| `refactor` | Code restructuring without behavior changes |
| `ui` | Visual styling or interaction adjustments |
| `perf` | Performance improvements |
| `docs` | Documentation changes |
| `test` | Test additions or updates |
| `build` | Build configuration, dependencies, tooling, or packaging |
| `ci` | CI workflows and automation |
| `chore` | Maintenance that fits no other type |
| `revert` | Reverting an earlier commit |

Use `feat` for new functionality and `fix` for defects, including changes to the UI. Use `ui` for other visual or interaction adjustments.

Preferred scopes:

| Scope | Area |
| --- | --- |
| `app` | Application behavior, UI, location services, and history |
| `android` | Android compatibility, manifest, and native SDK integration |
| `build` | Build configuration, dependencies, tooling, and packaging |
| `ci` | CI workflows and automation |
| `docs` | README files and project documentation |
| `repo` | Repository-level configuration and contribution guidelines |

Prefer the scopes listed above. Use another concise scope only for a distinct area not covered by them, and keep it consistent. Tests should normally use the scope of the code they cover.

Examples:

```text
feat(app): add manual coordinate entry
fix(android): update Baidu SDK for 16 KB page alignment
test(app): cover coordinate parsing edge cases
docs(repo): document open-source contribution workflow
feat(app)!: change the location history format
```

## Pull requests

- Keep each pull request focused on one problem or improvement and target the `main` branch.
- Explain the problem and the change, link a related issue when there is one, and complete the repository pull request template.
- Add or update tests when behavior changes. Include a screenshot for user-visible interface changes.
- Update user-facing documentation when behavior or configuration changes.
- Do not include map credentials, signing keys, passwords, `local.properties`, generated APKs, logs, or other private runtime data.
- Identify new dependencies and third-party code clearly, and make sure their licenses and distribution terms are compatible with the project.
- Run the checks listed under Development and report any that you could not run.

## License

PinShift is distributed under the GNU General Public License v3.0 only. By submitting a contribution, you confirm that you have the right to contribute it and agree that it may be distributed under the same license. See [LICENSE](./LICENSE) and retain existing copyright, attribution, and license notices.
