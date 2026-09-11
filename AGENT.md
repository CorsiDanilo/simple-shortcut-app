# AGENT.md

## 1. Executable Commands

- Dependency setup: Not configured as a standalone task; `./gradlew assembleDebug` resolves dependencies while building.
- Local Windows build: `.\gradlew.bat assembleDebug`
- Local POSIX build: `./gradlew assembleDebug`
- Development run: Not configured; no repository run or dev-server command exists.
- Linting and autofix: Not configured; no lint/formatter plugin or autofix task is present.
- Formatting: Not configured.
- Full test suite: Not configured; no `src/test`, `src/androidTest`, or test configuration is present.
- Targeted test: Not configured.
- Single-file validation: Not configured.
- Type checking: Not configured as a standalone task; Kotlin compilation is included in `./gradlew assembleDebug`.
- Local release package: `./gradlew assembleRelease` (requires a valid signing keystore and signing variables).
- CI release package: `./gradlew assembleRelease`, then `./gradlew assembleDebug` on JDK 17 after decoding the keystore.
- CI release trigger: push a tag matching `v*`; the workflow publishes signed and debug APKs plus SHA-256 files.

## 2. Operational Boundaries

### Always Do

- Inspect neighboring Kotlin, Compose, resource, and Gradle code before introducing a new pattern.
- Reuse existing abstractions and repository conventions.
- For a bug fix, reproduce the issue with the smallest available verification before changing implementation.
- Keep the diff limited to files required by the task.
- Run the narrowest relevant Gradle validation after changes.
- Preserve existing user data, local configuration, and generated outputs unless the task explicitly changes them.

### Ask First

- Adding, removing, or upgrading dependencies or changing the version catalog.
- Changing the Room schema, database version, migrations, or persistent data format.
- Changing public Android components, intent contracts, package identifiers, or update/release asset names.
- Modifying release signing, CI permissions, GitHub Actions, or updater security behavior.
- Renaming or deleting shared Kotlin components or resources.

### Never Do

- Never commit secrets, credentials, tokens, private keys, keystores, or `local.properties`.
- Never disable, delete, or weaken checks to make a build pass.
- Never bypass the Gradle wrapper with an unverified build tool or a different dependency-resolution path.
- Never perform broad refactors or mass formatting outside the task scope.
- Never manually replace generated icon data when the generator workflow is sufficient.

## 3. Repository-Specific Constraints

- Use the checked-in Gradle wrapper; the configured distribution is Gradle 9.0.0 and the project requires Java/Kotlin 17.
- The Android module targets SDK 35 and supports API 26+; keep variant behavior in `app/build.gradle.kts` consistent with these bounds.
- Release builds are minified and resource-shrunk, use `app/proguard-rules.pro`, and require `KEYSTORE_PATH`, `KEY_STORE_PASSWORD`, `ALIAS`, and `KEY_PASSWORD`.
- CI writes the signing keystore to `app/keystore.jks` and publishes `simple-shortcut-signed.apk` and `simple-shortcut-debug.apk`; preserve these updater-facing names.
- `paths.txt` contains generated icon-path output from `fetch_icons.py`; regenerate it intentionally and review the complete diff.
- The Room database is `shortcuts.db` with schema version 1 and `exportSchema = false`; treat persistence changes as migration-sensitive.
