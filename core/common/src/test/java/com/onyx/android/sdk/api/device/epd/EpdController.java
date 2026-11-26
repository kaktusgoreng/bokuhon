package com.onyx.android.sdk.api.device.epd;

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-in for the vendor controller that {@link eu.kanade.tachiyomi.util.system.OnyxEpd} reaches
 * by reflection. The signature is the one the bridge resolves, and every call is recorded so a test
 * can see what the app asked the panel for.
 */
public final class EpdController {

    public static final List<UpdateMode> repaints = new ArrayList<>();

    private EpdController() {
    }

    public static void repaintEveryThing(UpdateMode mode) {
        repaints.add(mode);
    }
}
