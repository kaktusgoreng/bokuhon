package eu.kanade.tachiyomi.util.system

import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * The firmware keeps the members the vendor SDK reaches through behind the hidden API, and Android
 * denies those to apps by default, so nothing answers until they are exempt. Only exists from
 * Android 9 on; calling it more than once is harmless.
 */
internal fun exemptOnyxHiddenApi() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        runCatching { HiddenApiBypass.addHiddenApiExemptions("") }
    }
}
