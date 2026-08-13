data class Asset(
    val fileName: String,
    val url: String?
)

data class AssetLocation(
    val expectedAssets: List<Asset>
)

object AppAssets {

    const val DefaultAppDataDatabaseVersion = 15
    const val DefaultAppDataReleaseTag = "data-v15"
    const val AppDataReleaseRepository = "0xArCHDeViL/Kaiteyo"

    fun appDataAssetFileName(version: Int): String = "kanji-dojo-data-base-v$version.sql"

    fun appDataReleaseUrl(version: Int, releaseTag: String): String =
        "https://github.com/$AppDataReleaseRepository/releases/download/$releaseTag/${appDataAssetFileName(version)}"

    val kanaVoiceOpus = Asset(
        fileName = "ja-JP-Neural2-B.opus",
        url = "https://github.com/syt0r/Kanji-Dojo-Data/releases/download/voice-v1/ja-JP-Neural2-B.opus"
    )

    val kanaVoiceWav = Asset(
        fileName = "ja-JP-Neural2-B.wav",
        url = "https://github.com/syt0r/Kanji-Dojo-Data/releases/download/voice-v1/ja-JP-Neural2-B.wav"
    )

    fun commonAssetsLocation(
        appDataVersion: Int = DefaultAppDataDatabaseVersion,
        appDataReleaseTag: String = DefaultAppDataReleaseTag
    ) = AssetLocation(
        expectedAssets = listOf(
            Asset(
                fileName = appDataAssetFileName(appDataVersion),
                url = appDataReleaseUrl(appDataVersion, appDataReleaseTag)
            ),
            Asset(
                fileName = "text_analysis_preview.json",
                url = null
            ),
            Asset(
                fileName = "bunpou_data.json",
                url = null
            )
        )
    )

    val AndroidAssetsLocation = AssetLocation(
        expectedAssets = listOf(kanaVoiceOpus)
    )

    val DesktopAssetsLocation = AssetLocation(
        expectedAssets = listOf(kanaVoiceWav)
    )

    val IosAssetsLocation = AssetLocation(
        expectedAssets = listOf(kanaVoiceWav)
    )

}

enum class AppDataSource {
    RELEASE,
    SOURCE;

    companion object {
        fun parse(value: String): AppDataSource = when (value.lowercase()) {
            "release" -> RELEASE
            "source" -> SOURCE
            else -> error("Unsupported app data source '$value'. Expected 'release' or 'source'.")
        }
    }
}
