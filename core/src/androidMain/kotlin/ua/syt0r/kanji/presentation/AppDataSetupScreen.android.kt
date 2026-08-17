package ua.syt0r.kanji.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.app_data_setup_download
import ua.syt0r.kanji.app_data_setup_error
import ua.syt0r.kanji.app_data_setup_import
import ua.syt0r.kanji.app_data_setup_import_description
import ua.syt0r.kanji.app_data_setup_title
import ua.syt0r.kanji.app_data_setup_download_description
import ua.syt0r.kanji.app_data_setup_downloading
import ua.syt0r.kanji.app_data_setup_importing
import ua.syt0r.kanji.app_data_setup_retry
import ua.syt0r.kanji.core.app_data.AppDataSetupController
import ua.syt0r.kanji.core.app_data.AppDataSetupState

@Composable
actual fun AppDataSetupScreen(controller: AppDataSetupController) {
    val state by controller.state.collectAsState()
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { controller.chooseImport(it.toString()) }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.app_data_setup_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = when (val current = state) {
                        AppDataSetupState.Downloading -> stringResource(Res.string.app_data_setup_downloading)
                        AppDataSetupState.Importing -> stringResource(Res.string.app_data_setup_importing)
                        is AppDataSetupState.Error -> current.message ?: stringResource(Res.string.app_data_setup_error)
                        else -> stringResource(Res.string.app_data_setup_download_description)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )

                when (state) {
                    AppDataSetupState.Checking,
                    AppDataSetupState.Downloading,
                    AppDataSetupState.Importing -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    AppDataSetupState.ChoiceRequired,
                    is AppDataSetupState.Error -> {
                        Button(
                            onClick = controller::chooseDownload,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.app_data_setup_download))
                        }
                        OutlinedButton(
                            onClick = {
                                filePicker.launch(
                                    arrayOf(
                                        "application/gzip",
                                        "application/x-gzip",
                                        "application/octet-stream",
                                        "application/vnd.sqlite3",
                                        "*/*"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.app_data_setup_import))
                        }
                        Text(
                            text = stringResource(Res.string.app_data_setup_import_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (state is AppDataSetupState.Error) {
                            OutlinedButton(
                                onClick = controller::retry,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(Res.string.app_data_setup_retry))
                            }
                        }
                    }

                    AppDataSetupState.Ready -> Unit
                }
            }
        }
    }
}
