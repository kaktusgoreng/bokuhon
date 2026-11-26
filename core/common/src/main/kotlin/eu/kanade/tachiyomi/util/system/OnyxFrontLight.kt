package eu.kanade.tachiyomi.util.system

import android.content.Context
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider
import com.onyx.android.sdk.api.device.brightness.BrightnessController
import com.onyx.android.sdk.api.device.brightness.CTMBrightnessProvider
import com.onyx.android.sdk.api.device.brightness.FLBrightnessProvider
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat

/**
 * The front light of a BOOX panel, through the vendor SDK. Onyx is the only e-paper vendor that
 * exposes one to apps, and its calls go through hidden firmware members, so this is gated on the
 * vendor and answers false rather than throwing anywhere it cannot reach the hardware.
 *
 * The light is the brightness provider of a warm and cold panel, which registers a temperature
 * provider beside it; the temperature one is left alone, since it tints the light rather than
 * switching it. The single-brightness [FrontLightController] calls report -1 on these panels.
 */
object OnyxFrontLight {

    private val readable: Boolean by lazy {
        if (!DeviceUtil.isOnyx) return@lazy false
        exemptOnyxHiddenApi()
        true
    }

    /** Remembered level per provider type, so switching the light back on returns to where it was. */
    private val levels = mutableMapOf<Int, Int>()

    private fun provider(context: Context): BaseBrightnessProvider? = runCatching {
        // Initialises the provider map for this device, then hands back what it registered.
        BrightnessController.getBrightnessType(context)
        BrightnessController.providerMap.values.firstOrNull {
            (it is FLBrightnessProvider || it is CTMBrightnessProvider) && it.maxIndex > 0
        }
    }.getOrNull()

    fun isAvailable(context: Context): Boolean = readable && provider(context) != null

    fun isOn(context: Context): Boolean {
        val light = provider(context) ?: return false
        // The level stays where it was set while the light is off, so it cannot answer this.
        return runCatching { light.isLightOn() }.getOrDefault(false)
    }

    /** Turns the light on or off and answers with the state it ended in. */
    fun toggle(context: Context): Boolean {
        if (!readable) return false
        val light = provider(context) ?: return false

        val wasOn = isOn(context)
        runCatching {
            if (wasOn) {
                levels[light.type] = light.index ?: 0
                light.setIndex(0)
                // Some panels keep the light on at level zero and only stop on close.
                if (isOn(context)) light.close()
            } else {
                // The first call only initialises the controller; the level lands on the next one.
                val level = levels[light.type] ?: (light.maxIndex / 2).coerceAtLeast(1)
                light.open()
                light.setIndex(level)
                if (!isOn(context)) light.setIndex(level)
            }
        }.onFailure {
            logcat(LogPriority.WARN, it) { "Onyx front light: refused" }
        }

        // The panel can still report the old state for a moment after a write, so this answers what
        // was asked for; the reader re-reads the light when its menu opens and corrects the icon.
        logcat(LogPriority.DEBUG) {
            "Onyx front light: wasOn=$wasOn now=${!wasOn} read=${isOn(context)} index=${light.index}"
        }
        return !wasOn
    }
}
