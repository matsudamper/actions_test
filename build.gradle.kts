import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.3.0-alpha07" apply false
    id("com.android.library") version "7.3.0-alpha08" apply false
    id("org.jetbrains.kotlin.android") version "1.6.20" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1" apply false
}

repositories {
    google()
    mavenCentral()
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        version.set("0.45.2")
        verbose.set(false)
        android.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
        reporters {
            reporter(ReporterType.CHECKSTYLE)
        }
    }
}
