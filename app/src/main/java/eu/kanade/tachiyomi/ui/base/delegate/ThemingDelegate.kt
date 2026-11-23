package eu.kanade.tachiyomi.ui.base.delegate

import android.app.Activity
import eu.kanade.tachiyomi.R

interface ThemingDelegate {
    fun applyAppTheme(activity: Activity)

    companion object {
        val themeResId: Int = R.style.Theme_Tachiyomi
    }
}

class ThemingDelegateImpl : ThemingDelegate {
    override fun applyAppTheme(activity: Activity) {
        activity.setTheme(ThemingDelegate.themeResId)
    }
}
