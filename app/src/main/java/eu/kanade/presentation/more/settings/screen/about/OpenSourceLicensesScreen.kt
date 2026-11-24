package eu.kanade.presentation.more.settings.screen.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import eu.kanade.presentation.components.AppBar
import eu.kanade.tachiyomi.R
import mihon.core.navigation.LocalNavigator
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.HorizontalDivider
import tachiyomi.presentation.core.bokuhon.ListItem
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun OpenSourceLicensesScreen() {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = stringResource(MR.strings.licenses),
                navigateUp = navigator::pop,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        val libraries by produceLibraries(R.raw.aboutlibraries)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            items(
                items = libraries?.libraries.orEmpty(),
                key = { it.uniqueId },
            ) { library ->
                val licenseNames = library.licenses.mapNotNull { it.name }.joinToString(", ")
                val subtitle = listOfNotNull(
                    library.artifactVersion?.let { "v$it" },
                    licenseNames.takeIf { it.isNotBlank() },
                ).joinToString(" · ")

                ListItem(
                    modifier = Modifier.clickable(enabled = library.website != null) {
                        library.website?.let(uriHandler::openUri)
                    },
                    headlineContent = { Text(library.name) },
                    supportingContent = subtitle.takeIf { it.isNotBlank() }?.let {
                        { Text(it) }
                    },
                )
                HorizontalDivider()
            }
        }
    }
}
