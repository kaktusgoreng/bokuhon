package tachiyomi.presentation.core.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import tachiyomi.presentation.core.bokuhon.BokuhonTypography

val BokuhonTypography.header: TextStyle
    get() = bodyMedium.copy(fontWeight = FontWeight.SemiBold)
