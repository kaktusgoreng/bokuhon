package eu.kanade.tachiyomi.ui.reader.viewer.pager

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.math.max
import kotlin.math.min

/**
 * One page peeling off the pager, over the page it is being replaced by.
 *
 * The pager itself does not move while this is on screen - the two pages come in as snapshots -
 * so the sheet can fold back on itself about a crease that follows the finger: the flat part
 * stays put, the part past the crease is drawn mirrored as the back of the paper, and the gap it
 * leaves behind shows the next page already lying there.
 *
 * [anchor] is where the sheet leaves the page, as a fraction of the width: 1 for a left-to-right
 * paged reader (the right edge), 0 for a right-to-left one, and 0.5 for a two-page spread, which
 * curls out of its gutter.
 */
class PageCurlView(context: Context) : View(context) {

    private var over: Bitmap? = null
    private var under: Bitmap? = null
    private var anchor = 1f
    private var travelsLeft = true

    private var animator: ValueAnimator? = null

    /** How far the sheet has been folded over, from 0 (flat) to 1 (gone). */
    var progress = 0f
        private set

    private val pagePaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val shadePaint = Paint()
    private val mirror = Matrix()

    private val density = resources.displayMetrics.density
    private val shadowWidth = 20f * density

    /**
     * Starts a curl. [over] is the page being taken away, [under] the one already in its place.
     */
    fun begin(over: Bitmap, under: Bitmap?, anchor: Float, travelsLeft: Boolean) {
        clear()
        this.over = over
        this.under = under
        this.anchor = anchor
        this.travelsLeft = travelsLeft
        progress = 0f
        invalidate()
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Runs the rest of the fold, either finishing it or letting the page fall back.
     */
    fun animateTo(target: Float, durationMillis: Long, onEnd: () -> Unit) {
        // The token is taken first: a callback from any earlier curl must not run.
        animateTo(target, durationMillis, ++generation, onEnd)
    }

    private fun animateTo(target: Float, durationMillis: Long, token: Int, onEnd: () -> Unit) {
        animator?.cancel()
        val from = progress
        val animation = ValueAnimator.ofFloat(from, target).apply {
            duration = durationMillis
            addUpdateListener {
                if (token != generation) return@addUpdateListener
                progress = it.animatedValue as Float
                invalidate()
            }
            doOnEnd {
                if (token == generation) onEnd()
            }
        }
        animator = animation
        animation.start()
    }

    /** A curl replaced by another one; the older animation must not run its callback. */
    private var generation = 0

    fun clear() {
        generation++
        animator?.cancel()
        animator = null
        over?.recycle()
        under?.recycle()
        over = null
        under = null
        progress = 0f
        invalidate()
    }

    override fun onDetachedFromWindow() {
        clear()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val over = over ?: return
        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0f || height <= 0f) return

        val anchorX = anchor * width
        val travel = if (travelsLeft) anchorX else width - anchorX
        val peel = progress * travel
        val fold = if (travelsLeft) anchorX - peel else anchorX + peel

        // The page that is already in place, through the gap the sheet has left.
        under?.let { page ->
            canvas.save()
            canvas.clipRect(min(fold, anchorX), 0f, max(fold, anchorX), height)
            canvas.drawBitmap(page, 0f, 0f, pagePaint)
            canvas.restore()
        }

        // The part of the sheet still lying flat.
        canvas.save()
        if (travelsLeft) {
            canvas.clipRect(0f, 0f, fold, height)
        } else {
            canvas.clipRect(fold, 0f, width, height)
        }
        canvas.drawBitmap(over, 0f, 0f, pagePaint)
        canvas.restore()

        if (peel <= 0.5f) return

        // The back of the sheet, folded over about the crease.
        val backLeft = if (travelsLeft) fold - peel else fold
        val backRight = backLeft + peel
        mirror.setScale(-1f, 1f)
        mirror.postTranslate(2f * fold, 0f)

        canvas.save()
        canvas.clipRect(backLeft, 0f, backRight, height)
        canvas.drawBitmap(over, mirror, pagePaint)
        // The paper bends away from the reader, so the back darkens towards the crease.
        shadePaint.shader = LinearGradient(
            if (travelsLeft) backLeft else backRight,
            0f,
            if (travelsLeft) backRight else backLeft,
            0f,
            SHADE_LIGHT,
            SHADE_DARK,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(backLeft, 0f, backRight, height, shadePaint)
        canvas.restore()

        // And the shadow the raised edge throws on the page below.
        val shadowFrom = fold
        val shadowTo = if (travelsLeft) fold + shadowWidth else fold - shadowWidth
        canvas.save()
        shadePaint.shader = LinearGradient(
            shadowFrom,
            0f,
            shadowTo,
            0f,
            SHADE_DARK,
            SHADE_CLEAR,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(
            min(shadowFrom, shadowTo),
            0f,
            max(shadowFrom, shadowTo),
            height,
            shadePaint,
        )
        canvas.restore()
    }

    private companion object {
        /** Darkest shade of the folded back, and the one at the crease. */
        const val SHADE_LIGHT = 0x00_00_00_00
        const val SHADE_DARK = 0x55_00_00_00.toInt()
        const val SHADE_CLEAR = 0x00_00_00_00
    }
}
