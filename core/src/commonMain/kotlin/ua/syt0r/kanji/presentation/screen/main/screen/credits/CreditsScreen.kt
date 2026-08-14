package ua.syt0r.kanji.presentation.screen.main.screen.credits

import ua.syt0r.kanji.presentation.common.ui.KaiteyoScaffold

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import org.koin.compose.koinInject
import ua.syt0r.kanji.presentation.common.AppListItem
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(
    state: MainNavigationState
) {
    KaiteyoScaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { state.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = {}
            )
        }
    ) { paddingValues ->

        val getCreditsUseCase = koinInject<GetCreditLibrariesUseCase>()
        val libsState = remember { mutableStateOf<Libs?>(null) }

        LaunchedEffect(Unit) {
            libsState.value = getCreditsUseCase()
        }

        var selectedLib by remember { mutableStateOf<Library?>(null) }
        selectedLib?.let {
            MultiplatformDialog(
                onDismissRequest = { selectedLib = null },
                title = {
                    Text("License")
                },
                content = {
                    Text(
                        text = it.licenses.firstOrNull()?.licenseContent
                            ?: "Can't find license text"
                    )
                },
                buttons = {
                    TextButton(
                        onClick = { selectedLib = null }
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        Crossfade(
            targetState = libsState.value,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when (it) {
                null -> CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Dictionary data and attribution",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "JMdict, JMnedict/ENAMDICT, KANJIDIC2, and RADKFILE are provided by the Electronic Dictionary Research and Development Group (EDRDG) under the CC BY-SA 4.0 licence. Furigana segmentation is derived from the JMdict data source.\n\nSources: https://www.edrdg.org/edrdg/licence.html\nhttps://www.edrdg.org/jmdict/\nhttps://www.edrdg.org/enamdict/enamdict_doc.html\nhttps://jisho.hlorenzi.com/furigana.txt",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        items(it.libraries) {
                            AppListItem(
                                onClick = { selectedLib = it },
                                headlineContent = { Text(it.name) },
                                supportingContent = {
                                    Text(
                                        text = it.licenses.joinToString { it.name }
                                    )
                                }
                            )
                        }
                    }
                }

            }
        }

    }

}
