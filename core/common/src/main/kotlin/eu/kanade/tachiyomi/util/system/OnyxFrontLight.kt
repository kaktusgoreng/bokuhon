package eu.kanade.tachiyomi.util.system

import android.content.Context
import com.onyx.android.sdk.api.device.FrontLightController
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import java.lang.reflect.Method

/**
 * The front light of a BOOX panel, switched through the firmware's own toggle.
 *
 * That toggle is what the system's light control calls: it drives the brightness and temperature
 * channels together and keeps the tint the panel was set to. The SDK's per-channel calls do not -
 * opening the light resets its temperature to cold, which is why a light switched from the app came
 * back white. The firmware entry point is hidden from apps, so it is reached by reflection and the
 * exemption is applied first; off BOOX, or where it cannot be reached, everything answers false.
 */
object OnyxFrontLight {

    private class Control(private val getLightState: Method, private val toggleLight: Method) {
        fun isOn(context: Context): Boolean = getLightState.invoke(null, context) as? Boolean ?: false

        fun toggle(context: Context) {
            toggleLight.invoke(null, context)
        }

        companion object {
            private const val CONTROLLER = "android.onyx.hardware.DeviceController"

            fun resolve(): Control? = runCatching {
                val controller = Class.forName(CONTROLLER)
                Control(
                    getLightState = controller.getMethod("getLightState", Context::class.java),
                    toggleLight = controller.getMethod("toggleLight", Context::class.java),
                )
            }.getOrNull()
        }
    }

    private val readable: Boolean by lazy {
        if (!DeviceUtil.isOnyx) return@lazy false
        exemptOnyxHiddenApi()
        true
    }

    private val control: Control? by lazy { if (readable) Control.resolve() else null }

    fun isAvailable(context: Context): Boolean {
        if (control == null) return false
        return runCatching {
            FrontLightController.hasFLBrightness(context) || FrontLightController.hasCTMBrightness(context)
        }.getOrDefault(false)
    }

    fun isOn(context: Context): Boolean {
        val controller = control ?: return false
        return runCatching { controller.isOn(context) }.getOrDefault(false)
    }

    /** Turns the light on or off and answers with the state it ended in. */
    fun toggle(context: Context): Boolean {
        val controller = control ?: return false
        val wasOn = isOn(context)
        runCatching { controller.toggle(context) }
            .onFailure { logcat(LogPriority.WARN, it) { "Onyx front light: refused" } }
        logcat(LogPriority.DEBUG) { "Onyx front light: wasOn=$wasOn now=${!wasOn} read=${isOn(context)}" }
        // The panel lags a write, so this answers what was asked for; the reader re-reads the light
        // when its menu opens and corrects the icon.
        return !wasOn
    }
}
