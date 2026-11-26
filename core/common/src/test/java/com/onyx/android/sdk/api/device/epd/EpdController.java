package com.onyx.android.sdk.api.device.epd;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-in for the vendor controller that {@link eu.kanade.tachiyomi.util.system.OnyxEpd} reaches
 * by reflection. The signatures are the ones the bridge resolves, and every call is recorded so a
 * test can see what the app asked the panel for.
 */
public final class EpdController {

    public static final List<UpdateMode> repaints = new ArrayList<>();
    public static final List<UpdateMode> viewModes = new ArrayList<>();

    private EpdController() {
    }

    public static void repaintEveryThing(UpdateMode mode) {
        repaints.add(mode);
    }

    public static void setViewDefaultUpdateMode(View view, UpdateMode mode) {
        viewModes.add(mode);
    }
}
