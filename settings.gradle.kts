pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            val version = "1.9.0"
            library("android-core-ktx", "androidx.core:core-ktx:$version")
        }
    }
}
rootProject.name = "actions_test"
include(":app")
