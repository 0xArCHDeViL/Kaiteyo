import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.JavaExec
import java.net.URL
import java.util.zip.GZIPInputStream

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
    application
}

group = "ua.syt0r"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
    val ktor_version = "2.3.12"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("org.apache.commons:commons-csv:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("dev.esnault.wanakana:wanakana-core:1.1.1")
}

sqldelight {
    databases {
        create("KanjiDojoData") {
            packageName.set("export.db")
        }
    }
}

val TaskPropertyName = "task"
val appDataVersion = providers.gradleProperty("appDataVersion")
    .map(String::toInt)
    .orElse(15)
    .get()

tasks.register<JavaExec>("exportAppDatabase") {
    group = "database"
    description = "Export the internal Kanji Dojo SQLite database from the canonical source data."
    dependsOn(
        tasks.named("classes"),
        tasks.named("downloadJishoOpenFurigana"),
        tasks.named("downloadJMnedict")
    )
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("task.ExportDatabaseKt")
    workingDir(project.projectDir)
    systemProperty("appDataVersion", appDataVersion.toString())
    inputs.property("appDataVersion", appDataVersion)
    outputs.file(layout.projectDirectory.file("kanji-dojo-data-base-v$appDataVersion.sql"))
}

application {
    if (hasProperty(TaskPropertyName)) {
        val task = properties[TaskPropertyName]
        mainClass.set("task.${task}Kt")
    }
}

val radFileUrl = "http://ftp.edrdg.org/pub/Nihongo/radkfile.gz"
val jmdictFileUrl = "http://ftp.edrdg.org/pub/Nihongo/JMdict_e_examp.gz"
val jmnedictFileUrl = "http://ftp.edrdg.org/pub/Nihongo/JMnedict.xml.gz"
val leedsFreqUrl = "https://web.archive.org/web/20230924010025id_/http://corpus.leeds.ac.uk/frqc/internet-jp.num"
val jmdictFuriganaJsonUrl =
    "https://github.com/Doublevil/JmdictFurigana/releases/download/2.3.0%2B2023-08-25/JmdictFurigana.json"
val jishoOpenFuriganaUrl = "https://jisho.hlorenzi.com/furigana.txt"
val yomichanJlptVocabDecksBaseUrl =
    "https://raw.githubusercontent.com/stephenmk/yomitan-jlpt-vocab/refs/heads/main/original_data/"

val dataDir = File(projectDir, "parser_data")

task("downloadRadkFile") {
    doLast {
        downloadGz(
            url = radFileUrl,
            destination = File(dataDir, "radkfile")
        )
    }
}

task("downloadJMdict") {
    doLast {
        downloadGz(
            url = jmdictFileUrl,
            destination = File(dataDir, "JMdict_e_examp")
        )
    }
}

abstract class DownloadJMnedictTask : DefaultTask() {
    @get:Input
    abstract val sourceUrl: Property<String>

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    fun downloadIfMissing() {
        val output = destination.get().asFile
        if (output.isFile && output.length() > 0L) return
        output.parentFile.mkdirs()
        GZIPInputStream(URL(sourceUrl.get()).openStream()).use { input ->
            output.outputStream().use { outputStream -> input.copyTo(outputStream) }
        }
    }
}

tasks.register<DownloadJMnedictTask>("downloadJMnedict") {
    group = "database"
    description = "Download the official EDRDG JMnedict XML source."
    sourceUrl.set(jmnedictFileUrl)
    destination.set(layout.projectDirectory.file("parser_data/JMnedict.xml"))
}

task("downloadLeedsFrequencies") {
    doLast {
        dataDir.mkdirs()
        val file = File(dataDir, "internet-jp.num")
        downloadFile(leedsFreqUrl, file)
    }
}


task("downloadjmdictFuriganaJson") {
    doLast {
        val file = File(dataDir, "JmdictFurigana.json")
        downloadFile(jmdictFuriganaJsonUrl, file)
    }
}

abstract class DownloadJishoOpenFuriganaTask : DefaultTask() {
    @get:Input
    abstract val sourceUrl: Property<String>

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    fun downloadIfMissing() {
        val output = destination.get().asFile
        if (output.isFile && output.length() > 0L) return
        output.parentFile.mkdirs()
        URL(sourceUrl.get()).openStream().use { input ->
            output.outputStream().use { outputStream -> input.copyTo(outputStream) }
        }
    }
}

tasks.register<DownloadJishoOpenFuriganaTask>("downloadJishoOpenFurigana") {
    group = "database"
    description = "Download the derived furigana source used by the offline vocabulary export."
    sourceUrl.set(jishoOpenFuriganaUrl)
    destination.set(layout.projectDirectory.file("parser_data/furigana.txt"))
}

task("downloadYomichanJlptVocab") {
    doLast {
        val baseDir = File(dataDir, "yomichan-jlpt-vocab")
        val deckFileNames = (5 downTo 1).map { "n$it.csv" }
        deckFileNames.forEach { fileName ->
            val deckFile = File(baseDir, fileName)
            downloadFile(
                url = yomichanJlptVocabDecksBaseUrl + fileName,
                file = deckFile
            )
        }
    }
}

fun downloadFile(url: String, file: File) {
    file.parentFile.mkdirs()
    ant.invokeMethod("get", mapOf("src" to url, "dest" to file))
}

fun downloadGz(url: String, destination: File) {
    val dir = destination.parentFile
    dir.mkdirs()
    val archive = File(dir, "${destination.name}.gz")
    downloadFile(url, archive)
    val inputStream = resources.gzip(archive).read()
    destination.outputStream().apply {
        write(inputStream.readAllBytes())
        close()
    }
}
