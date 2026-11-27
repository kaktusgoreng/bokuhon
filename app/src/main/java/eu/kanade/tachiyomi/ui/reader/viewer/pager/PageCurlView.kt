package eu.kanade.tachiyomi.ui.reader.viewer.pager

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * One page peeling off the pager, over the page it is being replaced by.
 *
 * The pager itself does not move while this is on screen - the two pages come in as snapshots -
 * so the sheet can fold back on itself about a crease that follows the finger: the flat part
 * stays put, the part past the crease becomes the sheet's back, its paper with only a trace of the
 * print showing through, and the gap it leaves behind shows the next page already lying there.
 *
 * [anchor] is where the sheet leaves the page, as a fraction of the width: 1 for a left-to-right
 * paged reader (the right edge), 0 for a right-to-left one, and 0.5 for a two-page spread, which
 * curls out of its gutter. The crease leans towards the corner the finger is at, the way a sheet
 * lifted by one corner folds, and straightens as the sheet lies down.
 */
class PageCurlView(context: Context) : View(context) {

    private var over: Bitmap? = null
    private var under: Bitmap? = null
    private var anchor = 1f
    private var travelsLeft = true

    /** The finger's position down the page, as a fraction of the height. */
    private var apex = 0.5f

    /** The colour of the sheet's back, taken from the page itself. */
    private var paper = Color.WHITE

    private var animator: ValueAnimator? = null

    /** How far the sheet has been folded over, from 0 (flat) to 1 (gone). */
    var progress = 0f
        private set

    private val pagePaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val shadePaint = Paint()

    private val density = resources.displayMetrics.density
    private val shadowWidth = 20f * density

    /**
     * Starts a curl. [over] is the page being taken away, [under] the one already in its place.
     * [apex] is where the finger went down, so the crease leans towards that corner for the whole
     * drag, however far the finger then travels across the page.
     */
    fun begin(over: Bitmap, under: Bitmap?, anchor: Float, travelsLeft: Boolean, apex: Float) {
        clear()
        this.over = over
        this.under = under
        this.anchor = anchor
        this.travelsLeft = travelsLeft
        this.apex = apex.coerceIn(0f, 1f)
        this.paper = paperOf(over)
        progress = 0f
        invalidate()
    }

    /**
     * The paper the page is printed on: the commonest colour of the sheet that is brighter than the
     * sheet as a whole, sampled coarsely so ink never decides the back of the page. Taking the
     * commonest colour outright would turn the back black on a page that is mostly a dark panel.
     */
    private fun paperOf(page: Bitmap): Int {
        val counts = HashMap<Int, LongArray>()
        val stepX = max(1, page.width / 24)
        val stepY = max(1, page.height / 32)
        var y = stepY / 2
        while (y < page.height) {
            var x = stepX / 2
            while (x < page.width) {
                val colour = page.getPixel(x, y)
                val red = (colour shr 16) and 0xFF
                val green = (colour shr 8) and 0xFF
                val blue = colour and 0xFF
                val bucket = (red shr 5 shl 10) or (green shr 5 shl 5) or (blue shr 5)
                val sum = counts.getOrPut(bucket) { LongArray(4) }
                sum[0] += red
                sum[1] += green
                sum[2] += blue
                sum[3]++
                x += stepX
            }
            y += stepY
        }
        val common = paperBucket(counts) ?: return Color.WHITE
        val n = common[3].coerceAtLeast(1)
        return Color.rgb(
            (common[0] / n).toInt(),
            (common[1] / n).toInt(),
            (common[2] / n).toInt(),
        )
    }

    /** The commonest bucket that is at least as bright as the sampled page on average. */
    private fun paperBucket(counts: Map<Int, LongArray>): LongArray? {
        var luma = 0L
        var samples = 0L
        for ((bucket, sum) in counts) {
            luma += bucketLuma(bucket) * sum[3]
            samples += sum[3]
        }
        if (samples == 0L) return null
        val average = luma / samples
        return counts.entries
            .filter { bucketLuma(it.key) >= average }
            .maxByOrNull { it.value[3] }
            ?.value
            ?: counts.values.maxByOrNull { it[3] }
    }

    /** Brightness of a bucket, on the same 0-31 scale as its channels. */
    private fun bucketLuma(bucket: Int): Long {
        val red = (bucket shr 10) and 0x1F
        val green = (bucket shr 5) and 0x1F
        val blue = bucket and 0x1F
        return (299L * red + 587L * green + 114L * blue) / 1000L
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
        if (peel <= 0.5f) {
            canvas.drawBitmap(over, 0f, 0f, pagePaint)
            return
        }

        // The crease runs through the finger and leans towards the corner it is at, then straightens
        // as the sheet lies down at either end of the fold.
        val lean = (apex - 0.5f) * 2f
        val slope = lean * CREASE_LEAN * (min(progress, 1f - progress) * 2f) *
            (if (travelsLeft) 1f else -1f)
        val ascent = apex * height
        val creaseX = if (travelsLeft) anchorX - peel else anchorX + peel
        fun creaseAt(y: Float) = creaseX + (ascent - y) * slope

        val top = creaseAt(0f)
        val bottom = creaseAt(height)
        val page = Path().apply { addRect(0f, 0f, width, height, Path.Direction.CW) }
        val peeled = Path().apply {
            val far = if (travelsLeft) width * 2f else -width
            moveTo(top, 0f)
            lineTo(bottom, height)
            lineTo(far, height)
            lineTo(far, 0f)
            close()
            op(page, Path.Op.INTERSECT)
        }
        val flat = Path().apply { op(page, peeled, Path.Op.DIFFERENCE) }

        // A unit vector straight into the half being folded away, and the mirror about the crease.
        val span = hypot(bottom - top, height)
        val ux = (bottom - top) / span
        val uy = height / span
        val nx = uy * (if (travelsLeft) 1f else -1f)
        val ny = -ux * (if (travelsLeft) 1f else -1f)
        val mirror = Matrix().apply {
            val from = floatArrayOf(0f, 0f, width, 0f, 0f, height)
            val to = FloatArray(6)
            for (i in 0 until 3) {
                val x = from[i * 2]
                val y = from[i * 2 + 1]
                val along = (x - top) * ux + y * uy
                to[i * 2] = 2f * (top + along * ux) - x
                to[i * 2 + 1] = 2f * along * uy - y
            }
            setPolyToPoly(from, 0, to, 0, 3)
        }
        val flap = Path().apply { peeled.transform(mirror, this) }

        // The page that is already in place, through the gap the sheet has left.
        under?.let { beneath ->
            canvas.save()
            canvas.clipPath(peeled)
            canvas.drawBitmap(beneath, 0f, 0f, pagePaint)
            canvas.restore()
        }

        // The shadow the raised edge throws on the page below.
        canvas.save()
        canvas.clipPath(peeled)
        shadePaint.shader = LinearGradient(
            creaseX,
            ascent,
            creaseX + nx * shadowWidth,
            ascent + ny * shadowWidth,
            SHADE_DARK,
            SHADE_CLEAR,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width, height, shadePaint)
        canvas.restore()

        // The part of the sheet still lying flat.
        canvas.save()
        canvas.clipPath(flat)
        canvas.drawBitmap(over, 0f, 0f, pagePaint)
        canvas.restore()

        // The back of the sheet, folded over about the crease: paper, with only a hint of the print
        // carried through it, the way light picks out the ink on the other side.
        canvas.save()
        canvas.clipPath(flap)
        shadePaint.shader = null
        shadePaint.color = paper
        canvas.drawRect(0f, 0f, width, height, shadePaint)
        pagePaint.alpha = PAPER_SHOW_THROUGH
        canvas.drawBitmap(over, mirror, pagePaint)
        pagePaint.alpha = 255
        canvas.restore()

        // The paper bends away from the reader, so the back darkens towards the crease.
        canvas.save()
        canvas.clipPath(flap)
        shadePaint.shader = LinearGradient(
            creaseX - nx * peel,
            ascent - ny * peel,
            creaseX,
            ascent,
            SHADE_LIGHT,
            SHADE_DARK,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width, height, shadePaint)
        canvas.restore()
    }

    private companion object {
        /** Darkest shade of the folded back, and the one at the crease. */
        const val SHADE_LIGHT = 0x00_00_00_00
        const val SHADE_DARK = 0x55_00_00_00.toInt()
        const val SHADE_CLEAR = 0x00_00_00_00

        /** How much of the page's own print reaches the back of the sheet, 0-255. */
        const val PAPER_SHOW_THROUGH = 0x26

        /** How far the crease leans towards a corner, across per down, at most. */
        const val CREASE_LEAN = 0.55f
    }
}
