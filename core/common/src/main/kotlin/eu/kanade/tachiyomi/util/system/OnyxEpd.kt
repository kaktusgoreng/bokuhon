package eu.kanade.tachiyomi.util.system

import android.view.View
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import java.lang.reflect.Method

/**
 * Bridge to the refresh-mode API that BOOX firmware (EinkWise on 4.x) hands to apps.
 *
 * The vendor classes live on the device rather than with the app, so they are reached by
 * reflection: there is nothing to bundle or redistribute, and on any other device, or on firmware
 * whose classes are hidden, every call answers false so the caller's own fallback stays in charge.
 *
 * Apps are meant to ask for transient modes only; the resting mode belongs to EinkWise and to the
 * user's per-app settings, which is why nothing here sets a display mode for a view.
 */
object OnyxEpd {

    /** The panel update modes worth asking for, under the names the vendor API knows them by. */
    enum class Mode(val vendorName: String) {
        GU("GU"),
        GC("GC"),
        DU("DU"),
        DU_QUALITY("DU_QUALITY"),
        ANIMATION("ANIMATION"),
    }

    private val vendor: Vendor? by lazy {
        Vendor.resolve().also { resolved ->
            logcat(LogPriority.INFO) {
                "Onyx panel bridge: ${resolved?.describe() ?: "not available, keeping the app's own refresh"}"
            }
        }
    }

    val isSupported: Boolean get() = vendor != null

    /**
     * Repaints the screen with [mode]. A full refresh through [Mode.GC] is what the app's drawn
     * flash is imitating, so it is the default. [view] is only needed for firmware that can repaint
     * a view's window instead of the whole screen.
     */
    fun repaint(mode: Mode = Mode.GC, view: View? = null): Boolean = vendor?.repaint(mode, view) == true

    private class Vendor(
        private val modes: Map<String, Any>,
        private val repaintAll: Method?,
        private val invalidate: Method?,
    ) {
        /** Once the firmware refuses a call, it is not asked again for the rest of the session. */
        private var failing = false

        fun describe(): String = buildString {
            append("available (")
            append("repaint=").append(repaintAll?.name ?: "-")
            append(", invalidate=").append(invalidate?.name ?: "-")
            append(", modes=").append(modes.size)
            append(", resting=").append(appScopeMode() ?: "-")
            append(")")
        }

        fun repaint(mode: Mode, view: View?): Boolean {
            if (failing) return false
            val constant = modes[mode.vendorName] ?: return false
            repaintAll?.let { method -> if (call(method, null, constant)) return true }
            val method = invalidate ?: return false
            if (view == null) return false
            return call(method, null, view, constant)
        }

        private fun call(method: Method, receiver: Any?, vararg args: Any): Boolean = try {
            method.invoke(receiver, *args)
            true
        } catch (e: Throwable) {
            // Hidden, stubbed or rejecting firmware: stop asking rather than throw on every page turn.
            failing = true
            logcat(LogPriority.WARN, e.cause ?: e) {
                "Onyx panel bridge: ${method.name} refused, keeping the app's own refresh"
            }
            false
        }

        companion object {
            private const val CONTROLLER = "com.onyx.android.sdk.api.device.epd.EpdController"
            private const val CONTROLLER_LEGACY = "com.onyx.android.sdk.device.EpdController"
            private const val UPDATE_MODE = "com.onyx.android.sdk.api.device.epd.UpdateMode"
            private const val UPDATE_MODE_NESTED = "$CONTROLLER\$UpdateMode"

            fun resolve(): Vendor? {
                val controller = classOrNull(CONTROLLER) ?: classOrNull(CONTROLLER_LEGACY) ?: return null
                val updateMode = classOrNull(UPDATE_MODE) ?: classOrNull(UPDATE_MODE_NESTED) ?: return null
                val modes = updateMode.enumConstants
                    ?.associateBy { (it as Enum<*>).name }
                    ?: return null

                val repaintAll = controller.methodOrNull("repaintEveryThing", updateMode)
                val invalidate = controller.methodOrNull("invalidate", View::class.java, updateMode)
                    ?: controller.methodOrNull("postInvalidate", View::class.java, updateMode)

                if (repaintAll == null && invalidate == null) return null
                return Vendor(modes, repaintAll, invalidate)
            }

            /** The refresh mode EinkWise holds the app in, read only, so a report can name it. */
            private fun appScopeMode(): String? = try {
                val device = classOrNull("com.onyx.android.sdk.device.Device")
                    ?.getMethod("currentDevice")
                    ?.invoke(null)
                device?.javaClass?.getMethod("getAppScopeRefreshMode")?.invoke(device)?.toString()
            } catch (e: Throwable) {
                null
            }

            private fun classOrNull(name: String): Class<*>? = try {
                Class.forName(name)
            } catch (e: Throwable) {
                null
            }

            private fun Class<*>.methodOrNull(name: String, vararg parameterTypes: Class<*>): Method? = try {
                getMethod(name, *parameterTypes)
            } catch (e: Throwable) {
                null
            }
        }
    }
}
