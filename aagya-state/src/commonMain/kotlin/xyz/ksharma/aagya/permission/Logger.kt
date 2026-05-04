package xyz.ksharma.aagya.permission

/**
 * Minimal logger interface used by Aagya internals.
 *
 * The library never depends on a concrete logging framework. By default, [NoOpLogger]
 * is used and the library is silent. If you want to forward Aagya's diagnostics into
 * your own logs (Kermit, Timber, OSLog, etc.), provide your own implementation and pass
 * it when constructing the controller.
 */
public interface Logger {
    public fun debug(message: String)
    public fun info(message: String)
    public fun warn(message: String, error: Throwable? = null)
    public fun error(message: String, error: Throwable? = null)
}

/** Default no-op logger. Aagya is silent unless you swap this out. */
public object NoOpLogger : Logger {
    override fun debug(message: String) = Unit
    override fun info(message: String) = Unit
    override fun warn(message: String, error: Throwable?) = Unit
    override fun error(message: String, error: Throwable?) = Unit
}
