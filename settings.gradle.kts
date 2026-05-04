rootProject.name = "aagya"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":aagya-state")
include(":aagya-data")
include(":aagya-store-datastore")
include(":aagya-store-userdefaults")
include(":aagya-di-koin")
include(":sample-android")
