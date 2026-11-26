package eu.kanade.tachiyomi.util.system

import android.os.Build
import android.view.View
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.device.Device
import logcat.LogPriority
import org.lsposed.hiddenapibypass.HiddenApiBypass
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
            // The firmware keeps its EPD members behind the hidden API, and Android denies those to
            // apps by default; the SDK can only reach the panel once they are exempt. The hidden API
            // only exists from Android 9 on.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                runCatching { HiddenApiBypass.addHiddenApiExemptions("") }
            }
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
     * Has the next frame, the one carrying [view]'s current content, drawn with a full
     * ghost-clearing waveform. Refreshing before that frame would clean the previous content and
     * leave the new content to a partial waveform, so callers invalidate rather than repaint.
     */
    fun refreshWithFullWaveform(view: View): Boolean {
        if (device == null) return false
        return runCatching { EpdController.invalidate(view, UpdateMode.GC) }
            .onFailure { logcat(LogPriority.WARN, it) { "Onyx panel: full refresh refused" } }
            .isSuccess
    }
}
