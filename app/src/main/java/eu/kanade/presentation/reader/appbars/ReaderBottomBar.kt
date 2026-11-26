package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.CropOff
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Crop
import mihon.icons.materialsymbols.rounded.LightOff
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.roundedfilled.Lightbulb
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.Icon
import tachiyomi.presentation.core.bokuhon.IconButton
import tachiyomi.presentation.core.bokuhon.IconButtonDefaults
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderBottomBar(
    readingMode: ReadingMode,
    onClickReadingMode: () -> Unit,
    orientation: ReaderOrientation,
    onClickOrientation: () -> Unit,
    cropEnabled: Boolean,
    onClickCropBorder: () -> Unit,
    /** Null where the device has no front light the app can reach, which is everywhere but BOOX. */
    frontLightOn: Boolean?,
    onClickFrontLight: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .pointerInput(Unit) {},
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClickReadingMode) {
            Icon(
                imageVector = readingMode.icon,
                contentDescription = stringResource(MR.strings.viewer),
            )
        }

        IconButton(onClick = onClickOrientation) {
            Icon(
                imageVector = orientation.icon,
                contentDescription = stringResource(MR.strings.rotation_type),
            )
        }

        IconButton(onClick = onClickCropBorder) {
            Icon(
                imageVector = if (cropEnabled) MaterialSymbols.Rounded.Crop else CustomMaterialSymbols.Rounded.CropOff,
                contentDescription = stringResource(MR.strings.pref_crop_borders),
            )
        }

        if (frontLightOn != null) {
            // Its state is carried by the square itself: lit is ink with a white bulb, unlit is paper
            // with a crossed black one, the same way the rest of the app says "on".
            val colors = LocalBokuhonColors.current
            Box(
                modifier = Modifier
                    .size(IconButtonDefaults.Size)
                    .background(if (frontLightOn) colors.primary else colors.surface)
                    .clickable(role = Role.Button, onClick = onClickFrontLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (frontLightOn) {
                        MaterialSymbols.RoundedFilled.Lightbulb
                    } else {
                        MaterialSymbols.Rounded.LightOff
                    },
                    contentDescription = stringResource(MR.strings.pref_front_light),
                    tint = if (frontLightOn) colors.onPrimary else colors.onSurface,
                )
            }
        }

        IconButton(onClick = onClickSettings) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Settings,
                contentDescription = stringResource(MR.strings.action_settings),
            )
        }
    }
}
