package eu.kanade.tachiyomi.util.system

import android.content.Context
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider
import com.onyx.android.sdk.api.device.brightness.BrightnessController
import com.onyx.android.sdk.api.device.brightness.CTMBrightnessProvider
import com.onyx.android.sdk.api.device.brightness.CTMTemperatureProvider
import com.onyx.android.sdk.api.device.brightness.FLBrightnessProvider
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat

/**
 * The front light of a BOOX panel, through the vendor SDK. Onyx is the only e-paper vendor that
 * exposes one to apps, and its calls go through hidden firmware members, so this is gated on the
 * vendor and answers false rather than throwing anywhere it cannot reach the hardware.
 *
 * A warm and cold panel lights through two channels, brightness and temperature, which the SDK
 * models as two providers; the single-brightness [FrontLightController] calls report -1 on them.
 * Only the brightness channel is switched, and the temperature one is written back to the level it
 * was on, so a light turned back on keeps the tint the user chose instead of coming up cold.
 */
object OnyxFrontLight {

    private val readable: Boolean by lazy {
        if (!DeviceUtil.isOnyx) return@lazy false
        exemptOnyxHiddenApi()
        true
    }

    /** Last lit level per channel type, so switching back on returns to it. */
    private val levels = mutableMapOf<Int, Int>()

    private fun provider(context: Context): BaseBrightnessProvider? = runCatching {
        // Initialises the provider map for this device, then hands back what it registered.
        BrightnessController.getBrightnessType(context)
        BrightnessController.providerMap.values.firstOrNull {
            (it is FLBrightnessProvider || it is CTMBrightnessProvider) && it.maxIndex > 0
        }
    }.getOrNull()

    private fun tint(context: Context): BaseBrightnessProvider? = runCatching {
        BrightnessController.providerMap.values.firstOrNull { it is CTMTemperatureProvider }
    }.getOrNull()

    /**
     * The tint the framework holds, which survives the light being off; the provider's own index does
     * not, and turning the light on resets it to cold.
     */
    private fun systemTint(context: Context): Int? = runCatching {
        android.provider.Settings.System.getInt(context.contentResolver, "screen_ctm_temperature")
    }.getOrNull()

    private fun remember(channel: BaseBrightnessProvider) {
        runCatching { channel.index }.getOrNull()?.let { levels[channel.type] = it }
    }

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
        val colour = tint(context)
        val wasOn = isOn(context)

        runCatching {
            if (wasOn) {
                remember(light)
                colour?.let { channel ->
                    (systemTint(context) ?: runCatching { channel.index }.getOrNull())
                        ?.let { levels[channel.type] = it }
                }
                light.close()
                // Some panels keep the light on after close and only stop at level zero.
                if (isOn(context)) light.setIndex(0)
            } else {
                light.open()
                val level = levels[light.type] ?: (light.maxIndex / 2).coerceAtLeast(1)
                light.setIndex(level)
                // The first call after a cold start only initialises the controller.
                if (!isOn(context)) light.setIndex(level)
                // Opening resets the tint to cold, so put back the one the user is used to.
                colour?.let { channel ->
                    (levels[channel.type] ?: systemTint(context))?.let(channel::setIndex)
                }
            }
        }.onFailure {
            logcat(LogPriority.WARN, it) { "Onyx front light: refused" }
        }

        logcat(LogPriority.DEBUG) {
            "Onyx front light: wasOn=$wasOn now=${!wasOn} brightness=${light.index} tint=${colour?.index}"
        }
        // The panel can still report the old state for a moment after a write, so this answers what
        // was asked for; the reader re-reads the light when its menu opens and corrects the icon.
        return !wasOn
    }
}
