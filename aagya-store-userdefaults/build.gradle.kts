plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

kotlin {
    applyDefaultHierarchyTemplate()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.aagyaState)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

