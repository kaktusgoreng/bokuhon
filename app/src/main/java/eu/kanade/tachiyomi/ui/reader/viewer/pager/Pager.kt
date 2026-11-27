package eu.kanade.tachiyomi.ui.reader.viewer.pager

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import androidx.viewpager.widget.DirectionalViewPager
import eu.kanade.tachiyomi.ui.reader.viewer.GestureDetectorWithLongTap
import kotlin.math.abs

/**
 * Pager implementation that listens for tap and long tap and allows temporarily disabling touch
 * events in order to work with child views that need to disable touch events on this parent. The
 * pager can also be declared to be vertical by creating it with [isHorizontal] to false.
 */
open class Pager(
    context: Context,
    /**
     * Whether this pager pages sideways. Also tells a [DragHandler] which axis to watch.
     */
    val pagerIsHorizontal: Boolean = true,
) : DirectionalViewPager(context, pagerIsHorizontal) {

    /**
     * Takes a drag away from the pager, so the reader can turn the page its own way: swapping it
     * outright on an e-ink panel, whose every intermediate frame ghosts, or curling it elsewhere.
     */
    interface DragHandler {
        /**
         * The first drag past the touch slop. [towardsStart] is true for a drag towards the edge
         * the pages start from - leftwards on a sideways pager, upwards on a vertical one.
         * [crossPosition] is where the finger is on the other axis, in this view's coordinates.
         *
         * @return false to leave the gesture to the pager.
         */
        fun onDragStart(towardsStart: Boolean, crossPosition: Float): Boolean

        /** How far the finger has come, past the slop, in pixels, and where it is across. */
        fun onDragMove(distance: Float, crossPosition: Float) = Unit

        /** Finger lifted. [velocity] is in pixels per second, positive towards the start edge. */
        fun onDragEnd(velocity: Float) = Unit

        fun onDragCancel() = Unit
    }

    var dragHandler: DragHandler? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var dragTracking = false
    private var dragActive = false
    private var dragStartX = 0f
    private var dragStartY = 0f
    private var dragTowardsStart = false
    private var velocityTracker: VelocityTracker? = null

    /**
     * Tap listener function to execute when a tap is detected.
     */
    var tapListener: ((MotionEvent) -> Unit)? = null

    /**
     * Long tap listener function to execute when a long tap is detected.
     */
    var longTapListener: ((MotionEvent) -> Boolean)? = null

    /**
     * Gesture listener that implements tap and long tap events.
     */
    private val gestureListener = object : GestureDetectorWithLongTap.Listener() {
        override fun onSingleTapConfirmed(ev: MotionEvent): Boolean {
            tapListener?.invoke(ev)
            return true
        }

        override fun onLongTapConfirmed(ev: MotionEvent) {
            val listener = longTapListener
            if (listener != null && listener.invoke(ev)) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    /**
     * Gesture detector which handles motion events.
     */
    private val gestureDetector = GestureDetectorWithLongTap(context, gestureListener)

    /**
     * Whether the gesture detector is currently enabled.
     */
    private var isGestureDetectorEnabled = true

    /**
     * Dispatches a touch event.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val handled = super.dispatchTouchEvent(ev)
        if (isGestureDetectorEnabled) {
            gestureDetector.onTouchEvent(ev)
        }
        return handled
    }

    /**
     * Whether the given [ev] should be intercepted. Only used to prevent crashes when child
     * views manipulate [requestDisallowInterceptTouchEvent].
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (handleDrag(ev)) return true
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * Handles a touch event. Only used to prevent crashes when child views manipulate
     * [requestDisallowInterceptTouchEvent].
     */
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // A drag the handler owns must not reach the pager, or it would drag the page along too.
        if (handleDrag(ev)) return true
        return try {
            super.onTouchEvent(ev)
        } catch (e: NullPointerException) {
            false
        } catch (e: IndexOutOfBoundsException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun primaryDistance(ev: MotionEvent): Float =
        if (pagerIsHorizontal) ev.x - dragStartX else ev.y - dragStartY

    private fun crossDistance(ev: MotionEvent): Float =
        if (pagerIsHorizontal) ev.y - dragStartY else ev.x - dragStartX

    private fun crossPosition(ev: MotionEvent): Float =
        if (pagerIsHorizontal) ev.y else ev.x

    /**
     * Watches the gesture for [dragHandler].
     *
     * @return true when the handler owns this gesture and the pager must keep its hands off it.
     */
    private fun handleDrag(ev: MotionEvent): Boolean {
        val handler = dragHandler ?: return false

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragTracking = true
                dragActive = false
                dragStartX = ev.x
                dragStartY = ev.y
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain().also { it.addMovement(ev) }
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(ev)
                if (!dragTracking) return false

                if (!dragActive) {
                    val along = primaryDistance(ev)
                    // The finger has to be going along the pager rather than across it.
                    if (abs(along) <= touchSlop || abs(along) < abs(crossDistance(ev))) return false
                    dragTowardsStart = along < 0
                    dragActive = handler.onDragStart(dragTowardsStart, crossPosition(ev))
                    if (!dragActive) {
                        stopDrag()
                        return false
                    }
                }

                handler.onDragMove(abs(primaryDistance(ev)) - touchSlop, crossPosition(ev))
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasActive = dragActive
                if (wasActive) {
                    if (ev.actionMasked == MotionEvent.ACTION_UP) {
                        velocityTracker?.computeCurrentVelocity(1000)
                        val speed = abs(
                            if (pagerIsHorizontal) {
                                velocityTracker?.xVelocity ?: 0f
                            } else {
                                velocityTracker?.yVelocity ?: 0f
                            },
                        )
                        handler.onDragEnd(if (dragTowardsStart) -speed else speed)
                    } else {
                        handler.onDragCancel()
                    }
                }
                stopDrag()
                return wasActive
            }
        }
        return dragActive
    }

    private fun stopDrag() {
        dragTracking = false
        dragActive = false
        velocityTracker?.recycle()
        velocityTracker = null
    }

    /**
     * Executes the given key event when this pager has focus. Just do nothing because the reader
     * already dispatches key events to the viewer and has more control than this method.
     */
    override fun executeKeyEvent(event: KeyEvent): Boolean {
        // Disable viewpager's default key event handling
        return false
    }

    /**
     * Enables or disables the gesture detector.
     */
    fun setGestureDetectorEnabled(enabled: Boolean) {
        isGestureDetectorEnabled = enabled
    }
}
