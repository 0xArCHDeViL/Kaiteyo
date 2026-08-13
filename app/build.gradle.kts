plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    kotlin("plugin.compose")
    id("com.mikepenz.aboutlibraries.plugin")
}

private val releaseKeystoreFile = run {
    val fromEnvironment = System.getenv("KEYSTORE_PATH")
        ?.takeIf { it.isNotBlank() }
        ?.let(::File)
        ?.takeIf { it.isFile }
    val fromUserHome = File(System.getProperty("user.home"), ".kaiteyo/keystore.jks")
        .takeIf { it.isFile }
    fromEnvironment ?: fromUserHome ?: rootProject.file("keystore.jks")
}

private val releaseSigningReady = releaseKeystoreFile.isFile && listOf(
    "KEYSTORE_PASS",
    "SIGN_KEY",
    "SIGN_PASS"
).all { !System.getenv(it).isNullOrBlank() }

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-nowarn"
        )
    }
}

android {

    namespace = "ua.syt0r.kanji"

    compileSdk = 35
    defaultConfig {
        applicationId = "ua.syt0r.kanji"
        minSdk = 31
        targetSdk = 35
        versionCode = AppVersion.versionCode
        versionName = AppVersion.versionName
        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".dev"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    buildFeatures {
        compose = true
    }

    // Release signing is intentionally separate from debug signing. The same release
    // keystore must be supplied in every release build so Android accepts in-place updates.
    val releaseSigningConfig = signingConfigs.create("release") {
        storeFile = releaseKeystoreFile
        System.getenv("KEYSTORE_PASS")?.let { storePassword = it }
        System.getenv("SIGN_KEY")?.let { keyAlias = it }
        System.getenv("SIGN_PASS")?.let { keyPassword = it }
    }

    buildTypes.named("debug") {
        signingConfig = signingConfigs.getByName("debug")
    }
    buildTypes.named("release") {
        signingConfig = releaseSigningConfig
    }

    dependenciesInfo {
        // Removes a signing block with encrypted data for reproducible F-Droid builds
        includeInApk = false
    }

    lint {
        // Disable lint: known incompatibility between AGP-bundled lint
        // and Kotlin 2.1.x Analysis API (WARNING: Missing analysis API method)
        checkReleaseBuilds = false
        abortOnError = false
    }

}

tasks.configureEach {
    if (name == "preReleaseBuild") {
        doFirst {
            check(releaseSigningReady) {
                "Release signing requires the original keystore plus KEYSTORE_PASS, SIGN_KEY, and SIGN_PASS. " +
                    "Refusing to produce an APK signed with a different identity."
            }
        }
    }
}

dependencies {
    implementation(project(":core"))
}

aboutLibraries {
    collect {
        configPath.set(project.rootProject.layout.projectDirectory.dir("core/credits"))
    }
    export {
        excludeFields.set(setOf("generated"))
    }
}

