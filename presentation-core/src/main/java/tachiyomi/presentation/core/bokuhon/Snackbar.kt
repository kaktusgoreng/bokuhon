package tachiyomi.presentation.core.bokuhon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

enum class SnackbarResult {
    Dismissed,
    ActionPerformed,
}

enum class SnackbarDuration {
    Short,
    Long,
    Indefinite,
}

class SnackbarHostState {
    private val mutex = Mutex()

    var currentSnackbarData: SnackbarData? by mutableStateOf(null)
        private set

    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    ): SnackbarResult = mutex.withLock {
        try {
            val timeout = when (duration) {
                SnackbarDuration.Short -> 4_000L
                SnackbarDuration.Long -> 10_000L
                SnackbarDuration.Indefinite -> Long.MAX_VALUE
            }
            withTimeoutOrNull(timeout) {
                suspendCancellableCoroutine { continuation ->
                    currentSnackbarData = SnackbarData(
                        message = message,
                        actionLabel = actionLabel,
                        withDismissAction = withDismissAction,
                        duration = duration,
                        continuation = continuation,
                    )
                    continuation.invokeOnCancellation { currentSnackbarData = null }
                }
            } ?: SnackbarResult.Dismissed
        } finally {
            currentSnackbarData = null
        }
    }

    fun dismiss() {
        currentSnackbarData?.let { data ->
            currentSnackbarData = null
            data.continuation?.takeIf { it.isActive }?.resume(SnackbarResult.Dismissed)
        }
    }

    fun performAction() {
        currentSnackbarData?.let { data ->
            currentSnackbarData = null
            data.continuation?.takeIf { it.isActive }?.resume(SnackbarResult.ActionPerformed)
        }
    }
}

class SnackbarData internal constructor(
    val message: String,
    val actionLabel: String?,
    val withDismissAction: Boolean,
    val duration: SnackbarDuration,
    internal val continuation: CancellableContinuation<SnackbarResult>?,
) {
    fun dismiss() {
        continuation?.takeIf { it.isActive }?.resume(SnackbarResult.Dismissed)
    }

    fun performAction() {
        continuation?.takeIf { it.isActive }?.resume(SnackbarResult.ActionPerformed)
    }
}

@Composable
fun SnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { Snackbar(it) },
) {
    val data = hostState.currentSnackbarData
    if (data != null) {
        Box(modifier = modifier.padding(16.dp)) {
            snackbar(data)
        }
    }
}

@Composable
fun Snackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = RectangleShape,
    containerColor: Color = LocalBokuhonColors.current.inverseSurface,
    contentColor: Color = LocalBokuhonColors.current.inverseOnSurface,
    actionColor: Color = LocalBokuhonColors.current.mist,
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = shape, color = containerColor) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = snackbarData.message,
                modifier = Modifier.weight(1f),
                color = contentColor,
                style = LocalBokuhonTypography.current.bodyMedium,
            )
            snackbarData.actionLabel?.let { label ->
                Text(
                    text = label,
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            snackbarData.continuation?.takeIf { it.isActive }?.resume(SnackbarResult.ActionPerformed)
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    color = actionColor,
                    style = LocalBokuhonTypography.current.labelLarge,
                )
            }
            if (snackbarData.withDismissAction) {
                Text(
                    text = "✕",
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            snackbarData.continuation?.takeIf { it.isActive }?.resume(SnackbarResult.Dismissed)
                        }
                        .padding(8.dp),
                    color = contentColor,
                    style = LocalBokuhonTypography.current.bodyMedium,
                )
            }
        }
    }
}
