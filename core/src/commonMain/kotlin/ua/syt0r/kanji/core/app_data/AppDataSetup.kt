package ua.syt0r.kanji.core.app_data

import kotlinx.coroutines.flow.StateFlow

sealed interface AppDataSetupState {
    data object Checking : AppDataSetupState
    data object ChoiceRequired : AppDataSetupState
    data object Downloading : AppDataSetupState
    data object Importing : AppDataSetupState
    data object Ready : AppDataSetupState
    data class Error(val message: String?) : AppDataSetupState
}

interface AppDataSetupController {
    val state: StateFlow<AppDataSetupState>

    fun initialize()

    fun chooseDownload()

    fun chooseImport(uri: String)

    fun retry()
}
