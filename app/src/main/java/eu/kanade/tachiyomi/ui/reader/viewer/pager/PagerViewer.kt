package eu.kanade.tachiyomi.ui.reader.viewer.pager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.reader.model.ChapterTransition
import eu.kanade.tachiyomi.ui.reader.model.InsertPage
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import eu.kanade.tachiyomi.ui.reader.model.ViewerChapters
import eu.kanade.tachiyomi.ui.reader.setting.GeneralSettings
import eu.kanade.tachiyomi.ui.reader.viewer.LocalPageColorFilter
import eu.kanade.tachiyomi.ui.reader.viewer.PagedSettings
import eu.kanade.tachiyomi.ui.reader.viewer.ReaderPageImageView.ZoomStartPosition
import eu.kanade.tachiyomi.ui.reader.viewer.Viewer
import eu.kanade.tachiyomi.ui.reader.viewer.collectChanges
import eu.kanade.tachiyomi.ui.reader.viewer.mouseWheelPaging
import eu.kanade.tachiyomi.ui.reader.viewer.position
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.widget.ViewPagerAdapter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import mihon.app.di.appGraph
import tachiyomi.core.common.util.system.logcat
import kotlin.math.abs
import kotlin.math.min

/**
 * Implementation of a [Viewer] to display pages with a [ViewPager].
 */
@Suppress("LeakingThis")
abstract class PagerViewer(val activity: ReaderActivity) : Viewer {

    val graph by lazy { activity.appGraph }
    val downloadManager by lazy { graph.downloadManager }

    private val scope = MainScope()

    /**
     * View pager used by this viewer. It's abstract to implement L2R, R2L and vertical pagers on
     * top of this class.
     */
    val pager = createPager()

    private val settingsViewModel = activity.settingsViewModel

    /**
     * Settings every paged viewer uses, like scale mode on images or tap zones.
     */
    val paged: PagedSettings
        get() = settingsViewModel.paged.value

    /**
     * Settings only the pager viewers use, like splitting double pages.
     */
    val simplePaged: SimplePagedSettings
        get() = settingsViewModel.simplePaged.value

    /**
     * Settings shared by every viewer, like allow taps or page transitions.
     */
    val general: GeneralSettings
        get() = settingsViewModel.general.value

    val imageZoomType: ZoomStartPosition
        get() = paged.page.zoomStart.position(
            auto = when (this) {
                is L2RPagerViewer -> ZoomStartPosition.LEFT
                is R2LPagerViewer -> ZoomStartPosition.RIGHT
                else -> ZoomStartPosition.CENTER
            },
        )

    /** The curl drawn over the pager while a swipe turns the page. */
    private val curl = PageCurlView(activity)
    private var curlActive = false
    private var curlForward = true

    private val pagerItems = PagerItems(isReversed = this is R2LPagerViewer)

    /**
     * Adapter of the pager.
     */
    private val adapter = PagerViewerAdapter(this, pagerItems)

    private val tracker = PagerPageTracker(activity, pagerItems, ::setChaptersInternal) { page, forward ->
        getPageHolder(page)?.onPageSelected(forward)
    }

    private val pagerListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            if (!activity.isScrollingThroughPages) {
                activity.hideMenu()
            }
            tracker.onPageChange(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            tracker.isIdle = state == ViewPager.SCROLL_STATE_IDLE
        }
    }

    init {
        pager.isVisible = false // Don't layout the pager yet
        pager.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        pager.isFocusable = false
        pager.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        pager.offscreenPageLimit = 1
        pager.id = R.id.reader_pager
        pager.adapter = adapter
        pager.addOnPageChangeListener(pagerListener)
        pager.tapListener = { event ->
            val viewPosition = IntArray(2)
            pager.getLocationOnScreen(viewPosition)
            val viewPositionRelativeToWindow = IntArray(2)
            pager.getLocationInWindow(viewPositionRelativeToWindow)
            val pos = Offset(
                (event.rawX - viewPosition[0] + viewPositionRelativeToWindow[0]) / pager.width,
                (event.rawY - viewPosition[1] + viewPositionRelativeToWindow[1]) / pager.height,
            )
            activity.onViewerTap(pos)
        }
        pager.dragHandler = object : Pager.DragHandler {
            override fun onDragStart(towardsStart: Boolean): Boolean {
                // A zoomed page keeps its drag: the image view and the pager pan it themselves.
                if (isPagePannable()) return false

                val forward = isForwardSwipe(towardsStart)
                if (DeviceUtil.isEink) {
                    // Nothing between one page and the next is worth showing on a panel this slow.
                    turnPage(forward, animate = false)
                    return true
                }
                return pager.pagerIsHorizontal && startCurl(forward)
            }

            override fun onDragMove(distance: Float) {
                if (curlActive) curl.setProgress(distance / curlTravel())
            }

            override fun onDragEnd(velocity: Float) {
                if (!curlActive) return
                curlActive = false

                val forward = curlForward
                val finished = curl.progress > CURL_COMMIT_PROGRESS || abs(velocity) > CURL_FLING_VELOCITY
                if (finished) {
                    curl.animateTo(1f, CURL_SETTLE_MILLIS) {
                        turnPage(forward, animate = false)
                        curl.clear()
                    }
                } else {
                    curl.animateTo(0f, CURL_SETTLE_MILLIS) { curl.clear() }
                }
            }

            override fun onDragCancel() {
                if (!curlActive) return
                curlActive = false
                curl.animateTo(0f, CURL_SETTLE_MILLIS) { curl.clear() }
            }
        }

        pager.longTapListener = f@{
            if (activity.viewModel.state.value.menuVisible || general.longTapEnabled) {
                val item = adapter.items.getOrNull(pager.currentItem)
                if (item is ReaderPage) {
                    activity.onPageLongTap(item)
                    return@f true
                }
            }
            false
        }

        settingsViewModel.general.collectChanges(scope) { old, new ->
            if (old != null && old.theme != new.theme) {
                refreshAdapter()
            }
        }
        settingsViewModel.simplePaged.collectChanges(scope) { old, new ->
            if (old != null) {
                refreshAdapter()
            }
            if (old?.dualPageSplit != new.dualPageSplit && !new.dualPageSplit) {
                cleanupPageSplit()
            }
        }
        settingsViewModel.paged.collectChanges(scope) { old, new ->
            if (old != null && old.page != new.page) {
                refreshAdapter()
            }
        }
    }

    override fun destroy() {
        super.destroy()
        curl.clear()
        scope.cancel()
    }

    /**
     * Creates a new ViewPager.
     */
    abstract fun createPager(): Pager

    /** The page images' colours, for holders made later - see [LocalPageColorFilter]. */
    var pageColorFilter: ColorFilter? = null
        private set

    @Composable
    override fun Content(modifier: Modifier) {
        val filter = LocalPageColorFilter.current
        val pageFilter = remember(filter) { filter?.asAndroidColorFilter() }
        SideEffect {
            pageColorFilter = pageFilter
            // The pager keeps every page it holds as a child.
            pager.children.filterIsInstance<PagerPageHolder>().forEach { it.pageColorFilter = pageFilter }
        }
        AndroidView(
            factory = { context ->
                DisallowInterceptBarrier(context).apply {
                    addView(pager)
                    // Above the pages and never in the way: it takes its gestures through the pager.
                    addView(curl, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                }
            },
            modifier = modifier.mouseWheelPaging(this),
        )
    }

    /**
     * Returns the PagerPageHolder for the provided page
     */
    private fun getPageHolder(page: ReaderPage): PagerPageHolder? =
        pager.children
            .filterIsInstance(PagerPageHolder::class.java)
            .firstOrNull { it.item == page }

    /** The holder showing [item], page or chapter transition alike. */
    private fun holderFor(item: Any): View? =
        pager.children.firstOrNull { (it as? ViewPagerAdapter.PositionableView)?.item == item }

    /** Whether the current page is zoomed in far enough to move under the finger. */
    private fun isPagePannable(): Boolean {
        val page = tracker.currentPage as? ReaderPage ?: return false
        val holder = getPageHolder(page) ?: return false
        return paged.navigateToPan && (holder.canPanLeft() || holder.canPanRight())
    }

    /**
     * A drag towards the edge the pages start from goes forward in a left-to-right reader and in
     * a vertical one, and backwards in a right-to-left one.
     */
    private fun isForwardSwipe(towardsStart: Boolean): Boolean =
        if (pager.pagerIsHorizontal) towardsStart != isReversed else towardsStart

    private fun turnPage(forward: Boolean, animate: Boolean) {
        val target = pager.currentItem + if (forward) 1 else -1
        if (target in 0 until adapter.count) {
            pager.setCurrentItem(target, animate)
        }
    }

    /**
     * Where the sheet leaves the page: the edge a single page is read towards. A two-page spread
     * would hand its gutter here instead, which is the middle of the screen.
     */
    private fun curlAnchor(): Float = if (isReversed) 0f else 1f

    /** How far the finger has to travel for the sheet to come all the way over. */
    private fun curlTravel(): Float {
        val width = pager.width.toFloat()
        val anchor = curlAnchor()
        return if (isReversed) width - anchor * width else anchor * width
    }

    /**
     * Photographs both pages and puts the curl between them. The pager stays where it is until the
     * sheet has finished folding, so letting go of a curl never turns the page behind your back.
     */
    private fun startCurl(forward: Boolean): Boolean {
        val target = pager.currentItem + if (forward) 1 else -1
        if (target !in 0 until adapter.count) return true

        val current = adapter.items.getOrNull(pager.currentItem) ?: return false
        val next = adapter.items.getOrNull(target) ?: return false
        val over = snapshot(holderFor(current)) ?: return false
        val under = snapshot(holderFor(next))

        curlForward = forward
        curlActive = true
        curl.begin(over, under, curlAnchor(), !isReversed)
        return true
    }

    /** Draws [view] straight into a bitmap the size of the pager. */
    private fun snapshot(view: View?): Bitmap? {
        val width = pager.width
        val height = pager.height
        if (view == null || width <= 0 || height <= 0) return null
        return runCatching {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                .also { view.draw(Canvas(it)) }
        }.getOrNull()
    }

    /**
     * Tells this viewer to set the given [chapters] as active. If the pager is currently idle,
     * it sets the chapters immediately, otherwise they are saved and set when it becomes idle.
     */
    override fun setChapters(chapters: ViewerChapters) {
        tracker.setChapters(chapters)
    }

    /**
     * Sets the active [chapters] on this pager.
     */
    private fun setChaptersInternal(chapters: ViewerChapters) {
        // Remove listener so the change in item doesn't trigger it
        pager.removeOnPageChangeListener(pagerListener)

        val forceTransition = general.alwaysShowChapterTransition ||
            adapter.items.getOrNull(pager.currentItem) is ChapterTransition
        adapter.setChapters(chapters, forceTransition)

        // Layout the pager once a chapter is being set
        if (pager.isGone) {
            logcat { "Pager first layout" }
            val pages = chapters.currChapter.pages ?: return
            moveToPage(pages[min(chapters.currChapter.requestedPage, pages.lastIndex)])
            pager.isVisible = true
        }

        pager.addOnPageChangeListener(pagerListener)
        // Manually call onPageChange to update the UI
        tracker.onPageChange(pager.currentItem)
    }

    /**
     * Tells this viewer to move to the given [page].
     */
    override fun moveToPage(page: ReaderPage) {
        val position = adapter.items.indexOf(page)
        if (position != -1) {
            val currentPosition = pager.currentItem
            pager.setCurrentItem(position, true)
            // manually call onPageChange since ViewPager listener is not triggered in this case
            if (currentPosition == position) {
                tracker.onPageChange(position)
            }
        } else {
            logcat { "Page $page not found in adapter" }
        }
    }

    /**
     * Whether pages run right to left, so moving forward goes left.
     */
    protected open val isReversed = false

    /**
     * Whether a page turn is drawn at all. An e-paper panel cannot show a slide without leaving
     * every frame of it behind as ghosting, so there it always jumps.
     */
    private val animateTurns: Boolean
        get() = general.usePageTransitions && !DeviceUtil.isEink

    override fun moveForward() = if (isReversed) moveLeft() else moveRight()

    override fun moveBackward() = if (isReversed) moveRight() else moveLeft()

    /**
     * Moves to the page at the right.
     */
    private fun moveRight() {
        if (pager.currentItem != adapter.count - 1) {
            val holder = (tracker.currentPage as? ReaderPage)?.let(::getPageHolder)
            if (holder != null && paged.navigateToPan && holder.canPanRight()) {
                holder.panRight()
            } else {
                pager.setCurrentItem(pager.currentItem + 1, animateTurns)
            }
        }
    }

    /**
     * Moves to the page at the left.
     */
    private fun moveLeft() {
        if (pager.currentItem != 0) {
            val holder = (tracker.currentPage as? ReaderPage)?.let(::getPageHolder)
            if (holder != null && paged.navigateToPan && holder.canPanLeft()) {
                holder.panLeft()
            } else {
                pager.setCurrentItem(pager.currentItem - 1, animateTurns)
            }
        }
    }

    /**
     * Resets the adapter in order to recreate all the views. Used when a image configuration is
     * changed.
     */
    private fun refreshAdapter() {
        val currentItem = pager.currentItem
        adapter.refresh()
        pager.adapter = adapter
        pager.setCurrentItem(currentItem, false)
    }

    fun onPageSplit(currentPage: ReaderPage, newPage: InsertPage) {
        activity.runOnUiThread {
            // Need to insert on UI thread else images will go blank
            adapter.onPageSplit(currentPage, newPage)
        }
    }

    private fun cleanupPageSplit() {
        adapter.cleanupPageSplit()
    }
}

/** How far a page has to be folded before letting go finishes the turn. */
private const val CURL_COMMIT_PROGRESS = 0.3f

/** ... or how fast it has to be thrown. */
private const val CURL_FLING_VELOCITY = 400f

/** How long the sheet takes to finish folding, or to fall back. */
private const val CURL_SETTLE_MILLIS = 140L

/**
 * Keeps [requestDisallowInterceptTouchEvent] from reaching Compose. Its interop filter cancels the
 * gesture when a child re-allows intercepting mid-drag, which stops a zoomed page from handing the
 * drag to the pager at its edge.
 */
private class DisallowInterceptBarrier(context: Context) : FrameLayout(context) {
    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
}
