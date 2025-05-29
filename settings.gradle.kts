import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        maven("https://maven.waltid.dev/releases")
        maven("https://maven.waltid.dev/snapshots")
        google()
        mavenCentral()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent { snapshotsOnly() }
        }
    }
}
//enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "EE Wallet"
include(":app")

// include eu-digital-identity-wallet libs sources into project if exist
val properties = Properties().apply {
    val localProperties = file("local.properties")
    if (localProperties.exists()) {
        localProperties.inputStream().use {
            load(it)
        }
    }
}

// TODO: remove this later
val euDigitalIdentityWalletPath = properties["eu-digital-identity-wallet-path"] as? String
if (euDigitalIdentityWalletPath != null && File(euDigitalIdentityWalletPath).exists()) {
    includeIfExists("$euDigitalIdentityWalletPath/eudi-lib-jvm-sdjwt-kt")
    includeIfExists("$euDigitalIdentityWalletPath/eudi-lib-jvm-siop-openid4vp-kt")
    includeIfExists("$euDigitalIdentityWalletPath/eudi-lib-jvm-openid4vci-kt")
    includeIfExists("$euDigitalIdentityWalletPath/eudi-lib-jvm-presentation-exchange-kt")
}

fun includeIfExists(path: String) {
    if (Files.exists(Path.of(path))) {
        println("including build $path")
        includeBuild(path)
    } else {
        println("skipping $path - not found")
    }
}
