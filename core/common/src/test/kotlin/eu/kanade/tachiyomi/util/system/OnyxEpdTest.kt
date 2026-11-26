package eu.kanade.tachiyomi.util.system

import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OnyxEpdTest {

    @BeforeEach
    fun clearCalls() {
        EpdController.repaints.clear()
    }

    @Test
    fun `asks the panel for a full refresh`() {
        OnyxEpd.isSupported.shouldBeTrue()

        OnyxEpd.repaint().shouldBeTrue()

        EpdController.repaints.shouldContainExactly(UpdateMode.GC)
    }

    @Test
    fun `declines a mode this firmware does not have`() {
        OnyxEpd.repaint(OnyxEpd.Mode.DU_QUALITY).shouldBeFalse()

        EpdController.repaints.shouldContainExactly()
    }
}
