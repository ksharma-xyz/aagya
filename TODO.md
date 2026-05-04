# TODO

## Done so far

- [x] Verified the `io.github.ksharma-xyz` namespace on Central Portal
- [x] Generated PGP signing key, registered with keyservers
- [x] Added 4 secrets to GitHub: `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`, `SIGNING_KEY`, `SIGNING_KEY_PASSWORD`
- [x] Released **v0.1.0** to Maven Central
- [x] Made the repo public
- [x] GitHub Pages docs site live at <https://ksharma-xyz.github.io/aagya/>

## How to cut the next release

1. Bump in `gradle.properties`: `VERSION_NAME=0.X.Y-SNAPSHOT` to `VERSION_NAME=0.X.Y`.
2. In `CHANGELOG.md`, move the `## [Unreleased]` entries under a new `## [0.X.Y] - YYYY-MM-DD` heading and leave a fresh empty `## [Unreleased]` block above.
3. Commit and tag:
   ```bash
   git commit -am "Release 0.X.Y"
   git tag v0.X.Y
   git push origin main --tags
   ```
4. The `publish-release.yml` workflow handles Maven Central upload, Central Portal promotion, and GitHub Release creation automatically.
5. Bump back to the next snapshot: `VERSION_NAME=0.(X+1).0-SNAPSHOT`, commit, push.

Maven Central indexing lag is ~10-20 min after the workflow finishes. The artifact is browsable at <https://central.sonatype.com/artifact/io.github.ksharma-xyz/aagya-state> immediately, but `repo1.maven.org` (what Gradle reads) takes a bit longer.

## v0.2.0 candidate work

- [ ] Replace `AndroidPermissionController.resolveContextActivity` with `LocalActivity.current` from `androidx.activity:activity-compose` 1.10+. Drops the `ContextWrapper` unwrap loop, simpler and idiomatic. Activity-compose is already a transitive dep, no new dep needed.
- [ ] Implement `AppPermission.Camera` and `AppPermission.Microphone`
- [ ] Implement `AppPermission.Notifications` (Android 13 / iOS UNUserNotificationCenter)
- [ ] Background-location opt-in (`AppPermission.Location.Background`)
- [ ] Robolectric SDK matrix tests for the Android controller (API 28, 30, 33, 36)
- [ ] Add screenshot-tested sample showing each `Denied(canAskAgain)` UX state
- [x] Migrate KRAIL onto `aagya-data` (replace `core/permission`) — done 2026-05-05, branch `feat/aagya-dhruva-migration`

## v1.0.0 release criteria

- [ ] At least one external project using Aagya (besides KRAIL)
- [ ] All planned permission families landed (`Camera`, `Microphone`, `Notifications`, `Photos`)
- [ ] No breaking API changes for two consecutive minor releases
- [ ] Public API frozen and documented at the doc site
- [ ] Detekt clean across all modules

## Optional polish

- [ ] Submit to [klibs.io](https://klibs.io) directory
- [ ] PR to [terrakok/kmp-awesome](https://github.com/terrakok/kmp-awesome)
- [ ] Tweet / Bluesky / Mastodon announcement
- [ ] Post to r/Kotlin and r/androiddev
- [ ] Kotlin Slack `#multiplatform` channel

---

Detailed publishing flow with troubleshooting lives in [docs/publishing.md](docs/publishing.md).
