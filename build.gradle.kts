plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()
}

// Detekt was declared in the version catalog but never applied to any module, so
// `./gradlew detekt` failed with "Task 'detekt' not found" on every CI run. The
// step was marked continue-on-error, so that failure was reported and discarded
// for the lifetime of the workflow. Applying it here makes the task real.
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        // Every source set, not just the JVM ones: this is a KMP project and the
        // iOS/common code is where most of the logic lives.
        source.setFrom(files("src"))
        parallel = true
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "21"
        reports {
            html.required.set(true)
            xml.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}
