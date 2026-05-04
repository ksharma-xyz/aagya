import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.publish)
    // Dokka is intentionally NOT applied to this Android-only module.
    // AGP 9.x + Dokka V1's javaDocReleaseGeneration task fails on JDK 21 with
    // "PermittedSubclasses requires ASM9" when reading the Kotlin stdlib's
    // sealed-class bytecode. We tell vanniktech below to use an empty javadoc
    // jar (which Maven Central accepts) and skip javadoc generation entirely.
}

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = false,
        ),
    )
}

android {
    namespace = "xyz.ksharma.aagya.permission.store.datastore"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

dependencies {
    api(projects.state)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.kotlinx.coroutines.core)
}

