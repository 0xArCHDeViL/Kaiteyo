@file:OptIn(ExperimentalBuildToolsApi::class)

import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
    alias(libs.plugins.build.config)
    id("app.cash.sqldelight")
}

kotlin {

    androidTarget()

    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-nowarn"
        )
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.ui)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(libs.material3.window.size.clazz)
                api(compose.runtime)
                api(compose.materialIconsExtended)
                api(compose.components.resources)

                api(libs.koin.core)
                api(libs.koin.compose)
                api(libs.koin.compose.viewmodel)

                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.reflect)

                implementation(libs.datastore.preferences.core)
                implementation(libs.wanakana.core)

                api(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.auth)

                api(libs.aboutlibraries.core)

                api(libs.compose.reorderable)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android.driver)

                api(libs.lifecycle.viewmodel.ktx)
                api(libs.lifecycle.livedata.ktx)

                implementation(libs.work.runtime.ktx)

                api(libs.koin.android)
                api(libs.koin.androidx.compose)

                implementation(libs.navigation.compose)
                api(libs.activity.compose)
                api(libs.datastore.preferences)
                api(compose.uiTooling)

                api(libs.core.ktx)
                api(libs.appcompat)
                implementation(libs.media3.exoplayer)
            }
        }
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "ua.syt0r.kanji"
    publicResClass = true
}

val appDataSource = AppDataSource.parse(
    providers.gradleProperty("appDataSource").orElse(AppDataSource.RELEASE.name.lowercase()).get()
)
val appDataVersion = providers.gradleProperty("appDataVersion")
    .map(String::toInt)
    .orElse(AppAssets.DefaultAppDataDatabaseVersion)
    .get()
val appDataReleaseTag = providers.gradleProperty("appDataReleaseTag")
    .orElse(AppAssets.DefaultAppDataReleaseTag)
    .get()

registerPrepareAppAssetTasks(
    appDataSource = appDataSource,
    appDataVersion = appDataVersion,
    appDataReleaseTag = appDataReleaseTag
)

sqldelight {
    linkSqlite = true
    databases {
        create("AppDataDatabase") {
            packageName.set("ua.syt0r.kanji.core.app_data.db")
            srcDirs("src/commonMain/sqldelight_app_data")
        }
        create("UserDataDatabase") {
            packageName.set("ua.syt0r.kanji.core.user_data.db")
            srcDirs("src/commonMain/sqldelight_user_data")
        }
    }
}

android {
    namespace = "ua.syt0r.kanji.core"

    compileSdk = 35
    defaultConfig {
        minSdk = 31
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            consumerProguardFile("consumer-rules.pro")
        }
    }

}

buildConfig {

    packageName = "ua.syt0r.kanji"

    buildConfigField("versionCode", AppVersion.versionCode.toLong())
    buildConfigField("versionName", AppVersion.versionName)
    buildConfigField("appDataPackName", AppAssets.appDataPackFileName(appDataVersion))
    buildConfigField(
        "appDataPackUrl",
        AppAssets.appDataPackReleaseUrl(appDataVersion, appDataReleaseTag)
    )
    buildConfigField(
        "appDataPackChecksumUrl",
        AppAssets.appDataPackChecksumUrl(appDataVersion, appDataReleaseTag)
    )
    buildConfigField("appDataDatabaseVersion", appDataVersion)

    val kanaVoiceFieldName = "kanaVoiceAssetName"

    sourceSets.getByName("androidMain") {
        buildConfigField(
            name = kanaVoiceFieldName,
            value = AppAssets.kanaVoiceOpus.fileName
        )
    }

}

tasks.withType<Test> {
    useJUnitPlatform()
}
