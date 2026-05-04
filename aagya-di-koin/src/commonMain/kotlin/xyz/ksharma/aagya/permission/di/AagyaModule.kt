package xyz.ksharma.aagya.permission.di

import org.koin.core.module.Module
import org.koin.dsl.module
import xyz.ksharma.aagya.permission.Logger
import xyz.ksharma.aagya.permission.NoOpLogger
import xyz.ksharma.aagya.permission.PermissionPolicy
import xyz.ksharma.aagya.permission.store.InMemoryPermissionStore
import xyz.ksharma.aagya.permission.store.PermissionStore

/**
 * Koin module factory for Aagya.
 *
 * Wires:
 *   - [PermissionStore] (defaults to [InMemoryPermissionStore])
 *   - [PermissionPolicy] (defaults to [PermissionPolicy.Default])
 *   - [Logger] (defaults to [NoOpLogger])
 *
 * To get a `PermissionController`, use Aagya's `rememberPermissionController()` Composable
 * factory in your UI. Koin holds the policy and supporting types; the controller itself
 * needs platform context (`Activity` / `UIViewController`) so it stays Compose-bound.
 *
 * Example:
 * ```
 * startKoin {
 *     modules(
 *         aagyaModule(
 *             policy = PermissionPolicy(maxRequestsAndroid = 1, store = myStore),
 *             logger = MyKermitLogger(),
 *         ),
 *     )
 * }
 * ```
 */
public fun aagyaModule(
    policy: PermissionPolicy = PermissionPolicy.Default,
    logger: Logger = NoOpLogger,
    storeProvider: () -> PermissionStore = { InMemoryPermissionStore() },
): Module = module {
    single<PermissionStore> { storeProvider() }
    single<PermissionPolicy> { policy }
    single<Logger> { logger }
}
