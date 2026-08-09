plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    kotlin("plugin.compose")
    id("com.mikepenz.aboutlibraries.plugin")
}

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
        minSdk = 26
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

    // The keystore is kept outside the repository for security.
    // Resolved from (in order): KEYSTORE_PATH env var, the user's ~/.kaiteyo directory,
    // or the repository root (where CI decodes it from the KEYSTORE_BASE64 secret).
    val keystoreFile = run {
        val fromEnv = System.getenv("KEYSTORE_PATH")?.let(::File)?.takeIf { it.exists() }
        val fromUserHome = File(System.getProperty("user.home"), ".kaiteyo/keystore.jks").takeIf { it.exists() }
        fromEnv ?: fromUserHome ?: rootProject.file("keystore.jks")
    }

    val signedBuildSigningConfig = signingConfigs.create("signedBuild") {
        storeFile = keystoreFile
        System.getenv("KEYSTORE_PASS")?.let { storePassword = it }
        System.getenv("SIGN_KEY")?.let { keyAlias = it }
        System.getenv("SIGN_PASS")?.let { keyPassword = it }
    }

    val debugSigningConfig = signingConfigs.getByName("debug")

    buildTypes.forEach {
        it.signingConfig = if (keystoreFile.exists()) {
            signedBuildSigningConfig
        } else {
            debugSigningConfig
        }
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


