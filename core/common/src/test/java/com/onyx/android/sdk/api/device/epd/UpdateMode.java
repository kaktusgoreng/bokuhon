package com.onyx.android.sdk.api.device.epd;

/**
 * Stand-in for the vendor enum that {@link eu.kanade.tachiyomi.util.system.OnyxEpd} reaches by
 * reflection. Only the names the bridge may ask for are here, and two of them are left out on
 * purpose so a missing constant can be tested.
 */
public enum UpdateMode {
    None,
    DU,
    GU,
    GC,
    ANIMATION,
    REGAL,
}
