package eu.kanade.tachiyomi.util.system

import android.content.Context
import android.provider.Settings
import com.onyx.android.sdk.api.device.brightness.BaseBrightnessProvider
import com.onyx.android.sdk.api.device.brightness.BrightnessController
import com.onyx.android.sdk.api.device.brightness.CTMBrightnessProvider
import com.onyx.android.sdk.api.device.brightness.CTMTemperatureProvider
import com.onyx.android.sdk.api.device.brightness.FLBrightnessProvider
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat

/**
 * The front light of a BOOX panel.
 *
 * A warm and cold panel lights through a channel each for brightness and temperature, so both have
 * to be driven: switching only the brightness one, as the SDK's single-brightness calls do, brings
 * the light up cold. The channels are the truth here - the firmware settings mirror them, but only
 * the firmware's own light control keeps the two in step, and it does not restore a level it never
 * wrote. The app therefore remembers what each channel was lit at, restores both, and writes the
 * same numbers back to the settings so the system's light controls show what the panel is doing.
 */
object OnyxFrontLight {

    private const val BRIGHTNESS_KEY = "screen_ctm_brightness"
    private const val TEMPERATURE_KEY = "screen_ctm_temperature"

    private val readable: Boolean by lazy {
        if (!DeviceUtil.isOnyx) return@lazy false
        exemptOnyxHiddenApi()
        true
    }

    /** Last lit level per channel type. */
    private val levels = mutableMapOf<Int, Int>()

    private fun channels(context: Context): List<BaseBrightnessProvider> = runCatching {
        BrightnessController.getBrightnessType(context)
        BrightnessController.providerMap.values.filter { it is FLBrightnessProvider || it is CTMBrightnessProvider }
    }.getOrDefault(emptyList())

    private fun tint(context: Context): BaseBrightnessProvider? = runCatching {
        BrightnessController.providerMap.values.firstOrNull { it is CTMTemperatureProvider }
    }.getOrNull()

    private fun putSetting(context: Context, key: String, value: Int) {
        runCatching { Settings.System.putInt(context.contentResolver, key, value) }
    }

    fun isAvailable(context: Context): Boolean = readable && channels(context).isNotEmpty()

    fun isOn(context: Context): Boolean = channels(context).any { channel ->
        runCatching { channel.isLightOn() }.getOrDefault(false)
    }

    /**
     * Learns the levels the panel is lit at, so a light switched on by the system - with whatever
     * brightness and warmth the user chose there - can be restored by this after being switched off.
     */
    private fun learn(context: Context) {
        (channels(context) + listOfNotNull(tint(context))).forEach { channel ->
            runCatching { channel.index }.getOrNull()?.takeIf { it > 0 }?.let { levels[channel.type] = it }
        }
    }

    /** Turns the light on or off and answers with the state it ended in. */
    fun toggle(context: Context): Boolean {
        if (!readable) return false
        val lights = channels(context)
        if (lights.isEmpty()) return false

        val colour = tint(context)
        learn(context)
        val wasOn = isOn(context)

        runCatching {
            if (wasOn) {
                lights.forEach { light ->
                    (light.index ?: 0).takeIf { it > 0 }?.let { levels[light.type] = it }
                    light.setIndex(0)
                    if (isOn(context)) light.close()
                    putSetting(context, BRIGHTNESS_KEY, 0)
                }
                colour?.let { channel -> (channel.index ?: 0).takeIf { it > 0 }?.let { levels[channel.type] = it } }
            } else {
                lights.forEach { light ->
                    light.open()
                    val level = levels[light.type] ?: (light.maxIndex / 2).coerceAtLeast(1)
                    light.setIndex(level)
                    // The first call after a cold start only initialises the controller.
                    if ((light.index ?: 0) == 0) light.setIndex(level)
                    putSetting(context, BRIGHTNESS_KEY, light.index ?: level)
                }
                colour?.let { channel ->
                    levels[channel.type]?.let { channel.setIndex(it) }
                        ?: putSetting(context, TEMPERATURE_KEY, 0)
                }
            }
        }.onFailure {
            logcat(LogPriority.WARN, it) { "Onyx front light: refused" }
        }

        logcat(LogPriority.DEBUG) {
            "Onyx front light: wasOn=$wasOn channels=${lights.map { "${it.type}:${it.index}/${it.maxIndex}" }} " +
                "tint=${colour?.index}"
        }
        // The panel lags a write, so this answers what was asked for; the reader re-reads the light
        // when its menu opens and corrects the icon.
        return !wasOn
    }
}
