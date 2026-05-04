# Changelog

All notable changes to Aagya are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.1] - 2026-05-05

### Fixed
- KLIB resolver "duplicated unique_name" error when consumers depend on both
  Aagya and Dhruva on iOS. Both libraries previously published modules named
  `:state` and `:data`, which produced colliding klib unique_names like
  `io.github.ksharma-xyz:data`. The Gradle modules are now renamed to
  `:aagya-state`, `:aagya-data`, `:aagya-store-datastore`,
  `:aagya-store-userdefaults`, `:aagya-di-koin` so each klib has a globally
  unique name.

### Changed
- `AndroidPermissionController` no longer ships a `resolveContextActivity`
  helper. `rememberPermissionController()` reads `LocalActivity.current`
  (`androidx.activity.compose` 1.10+) directly. Activity-compose was already
  a transitive dependency, so no change is required for consumers.

## [0.1.0] - 2026-05-04

### Added
- Initial library scaffolding.
- `AppPermission.Location.Fine` and `AppPermission.Location.Coarse`.
- `PermissionController` interface with Android (`ActivityResultContracts`) and iOS (`CLLocationManager`) implementations.
- `PermissionPolicy` for opt-in stricter request limits.
- `PermissionStore` interface with `InMemoryPermissionStore` default.
- `aagya-store-datastore` (Android DataStore backed `PermissionStore`).
- `aagya-store-userdefaults` (iOS `NSUserDefaults` backed `PermissionStore`).
- `aagya-di-koin` Koin module factory.
- Android and iOS sample apps demonstrating zero-config, persistent, and BYOS usage.
- MkDocs Material docs site with concepts, recipes, and publishing guide.
