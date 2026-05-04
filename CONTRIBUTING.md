# Contributing to Aagya

Thanks for considering a contribution.

## Before you start work

1. **Open an issue first.** For anything beyond a typo or doc tweak, please open an issue before writing code so we can agree on the shape of the change. The public API surface is intentionally small and we want to keep it that way.
2. Read the [architecture doc](docs/concepts/architecture.md). It explains the storage policy, the cross-platform state machine, and what the library refuses to do (and why).

## Local setup

```bash
git clone https://github.com/ksharma-xyz/aagya.git
cd aagya
./gradlew build
```

Sample apps:

```bash
# Android
./gradlew :sample-android:installDebug

# iOS
open sample-ios/iosApp.xcodeproj
# then run from Xcode against a simulator
```

## What goes where

- **`state/`**, pure Kotlin, no platform imports. If your change adds a runtime dep here, it is probably wrong.
- **`data/`**, the cross-platform `PermissionController` and its Android / iOS implementations. The contract that everything else depends on.
- **`store-*`**, optional storage adapters. New adapters live as separate modules so the core libs stay dependency-free.
- **`di-koin/`**, optional DI integration. New DI integrations (Hilt, Kodein, etc.) should be separate modules.
- **`sample-android/`, `sample-ios/`**, runnable apps that exercise every supported usage tier. New features must come with a sample.
- **`docs/`**, MkDocs Material source. Every public API change updates docs.

## Coding style

- Detekt is enforced in CI. Run `./gradlew detekt` locally.
- Public API: every new public symbol needs a KDoc explaining *what* it does and at least one example of *when* you would reach for it.
- No `Throwable`s across the public API. Use sealed result types.
- Compose APIs prefixed `remember*` must follow the standard rememberer rules (key on the right inputs, no side effects in composition).

## Testing

- `./gradlew check` runs unit tests + lint + detekt for every module.
- Platform-specific behavior is exercised via the sample apps. If your change can not be tested in a unit test, the sample app must demonstrate it.

## Pull requests

- Title: imperative mood, scoped (e.g. `data: short-circuit Denied(canAskAgain=false) on Android`).
- Description: link the issue, describe the user-visible change, call out any breaking changes.
- Keep PRs small. If your change is more than ~300 lines, split it.

## Releasing (maintainers only)

See [docs/publishing.md](docs/publishing.md) for the full release flow.

## Code of Conduct

Be kind. Assume good faith. Disagree about ideas, not people.
