package eu.kanade.tachiyomi.util.system

import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.device.Device
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat

/**
 * The panel's own full refresh, through BOOX's SDK.
 *
 * Nothing in the firmware is reachable by a third-party app without these classes, and their calls
 * only mean anything on a BOOX device, so the whole thing is gated on the vendor and every call is
 * allowed to fail quietly: the reader keeps its drawn flash wherever the panel won't answer.
 *
 * Apps are meant to ask for transient modes only; the resting mode belongs to EinkWise and to the
 * user's per-app settings, which is why nothing here sets one.
 */
object OnyxEpd {

    private val device by lazy {
        val resolved = if (DeviceUtil.isOnyx) {
            exemptOnyxHiddenApi()
            runCatching { Device.currentDevice() }.getOrNull()
        } else {
            null
        }
        logcat(LogPriority.INFO) {
            "Onyx panel: ${if (resolved != null) "available" else "not available, keeping the app's own refresh"}"
        }
        resolved
    }

    val isSupported: Boolean get() = device != null

    /**
     * Repaints the whole screen with a full, ghost-clearing waveform. It has to come after the
     * content it is meant to clean: marking a view for the next frame asks for the same waveform
     * through the invalidate path, which the panel answers with a partial update.
     */
    fun repaint(): Boolean {
        if (device == null) return false
        return runCatching { EpdController.repaintEveryThing(UpdateMode.GC) }
            .onFailure { logcat(LogPriority.WARN, it) { "Onyx panel: full refresh refused" } }
            .isSuccess
    }
}
