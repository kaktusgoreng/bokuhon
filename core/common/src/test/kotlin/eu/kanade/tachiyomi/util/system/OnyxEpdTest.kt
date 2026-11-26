package eu.kanade.tachiyomi.util.system

import android.view.View
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OnyxEpdTest {

    @BeforeEach
    fun clearCalls() {
        EpdController.repaints.clear()
        EpdController.viewModes.clear()
    }

    @Test
    fun `asks the panel for a full refresh in the requested mode`() {
        OnyxEpd.isSupported.shouldBeTrue()

        OnyxEpd.repaint(OnyxEpd.Mode.GC).shouldBeTrue()

        EpdController.repaints.shouldContainExactly(UpdateMode.GC)
    }

    @Test
    fun `asks the panel to read a view in regal`() {
        OnyxEpd.setViewMode(mockk<View>(), OnyxEpd.Mode.REGAL).shouldBeTrue()

        EpdController.viewModes.shouldContainExactly(UpdateMode.REGAL)
    }

    @Test
    fun `declines a mode this firmware does not have`() {
        OnyxEpd.repaint(OnyxEpd.Mode.DU_QUALITY).shouldBeFalse()

        EpdController.repaints.shouldContainExactly()
    }
}
