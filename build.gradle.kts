import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.android.navigation.safeargs) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.distribution) apply false
}

subprojects {
    tasks.withType<Test> {
        useJUnit()
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
            events("skipped", "failed", "passed")
        }
    }
}


dependencyAnalysis {
    structure {
        ignoreKtx(ignore = true)
    }
    issues {
        all {
            onUnusedDependencies {
                severity("warn")
            }
            onUsedTransitiveDependencies {
                severity("warn")
            }
            onIncorrectConfiguration {
                severity("warn")
            }
            onUnusedAnnotationProcessors {
                severity("warn")
            }
            onRedundantPlugins {
                severity("warn")
            }
        }
    }
}
