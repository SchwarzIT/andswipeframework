package kaufland.com.swipelibrary

import android.view.View
import android.view.ViewGroup
import kaufland.com.swipelibrary.SwipeLayout.Companion.LEFT_DRAG_VIEW
import kaufland.com.swipelibrary.SwipeLayout.Companion.RIGHT_DRAG_VIEW
import kaufland.com.swipelibrary.SwipeLayout.Companion.SURFACE_VIEW


import kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL
import kotlin.math.abs

/**
 * Created by sbra0902 on 30.03.17.
 */
class DraggingProxy {

    private var mSwipeViewLayouter: SwipeViewLayouter? = null

    private var mLayoutCache: LayoutCache? = null

    var isInitialized: Boolean = false
        private set

    val dragDirection: SwipeViewLayouter.DragDirection
        get() = mSwipeViewLayouter!!.dragDirection

    val surfaceView: View
        get() = mSwipeViewLayouter!!.surfaceView


    fun init(parent: ViewGroup) {
        isInitialized = true
        mSwipeViewLayouter!!.init(parent)
        initInitialPosition()
    }


    fun requestLayout() {
        for (dragView in mSwipeViewLayouter!!.views.values) {
            dragView.forceLayout()
        }
    }

    fun clampViewPositionHorizontal(child: View, left: Int): Int {

        for (dragView in mSwipeViewLayouter!!.viewEngines.values) {
            if (dragView.dragView == child) {
                return dragView.clampViewPositionHorizontal(child, left)
            }
        }
        return left
    }

    fun determineSwipeHorizontalState(velocity: Float, swipeDirectionDetector: SwipeDirectionDetector, swipeState: SwipeState, swipeListener: SwipeLayout.SwipeListener, child: View): SwipeResult {
        for (dragView in mSwipeViewLayouter!!.viewEngines.values) {

            val result = dragView.determineSwipeHorizontalState(velocity, swipeDirectionDetector, swipeState, swipeListener, child)
            if (result != null) {
                return result
            }

        }
        return SwipeResult(0)
    }

    private fun initInitialPosition() {

        for (dragView in mSwipeViewLayouter!!.viewEngines.values) {
            dragView.initializePosition(mSwipeViewLayouter!!.dragDirection)
        }
    }

    fun moveView(changedView: View, positionChanges: Int) {

        for (engine in mSwipeViewLayouter!!.viewEngines.values) {
            engine.moveView(positionChanges.toFloat(), mSwipeViewLayouter!!.views[SURFACE_VIEW] as SurfaceView, changedView)
        }

    }

    fun restoreState(state: SwipeState.DragViewState) {

        for (view in mSwipeViewLayouter!!.viewEngines.values) {
            view.restoreState(state, mSwipeViewLayouter!!.views[SURFACE_VIEW] as SurfaceView)
            view.dragView?.forceLayout()
        }
    }

    fun isCapturedViewDraggable(child: View): Boolean {

        var draggable = false

        for (view in mSwipeViewLayouter!!.views.values) {

            draggable = draggable || child.id == view.id
        }

        return draggable
    }

    fun canSwipe(swipeDirectionDetector: SwipeDirectionDetector): Boolean {

        var canSwipe = false

        val absDiffX = abs(swipeDirectionDetector.difX).toFloat()
        val absDiffY = abs(swipeDirectionDetector.difY).toFloat()
        val diffX = swipeDirectionDetector.difX.toFloat()
        val isLeftDraggable = (mSwipeViewLayouter!!.views[LEFT_DRAG_VIEW] as DragView).isDraggable
        val isRightDraggable = (mSwipeViewLayouter!!.views[RIGHT_DRAG_VIEW] as DragView).isDraggable

        if (mSwipeViewLayouter!!.dragDirection == HORIZONTAL) {
            canSwipe = if (diffX > 0) {
                absDiffX > absDiffY && (isLeftDraggable || mSwipeViewLayouter!!.surfaceView.x < 0)
            } else {
                absDiffX > absDiffY && (isRightDraggable || mSwipeViewLayouter!!.surfaceView.x > 0)
            }
        }

        return canSwipe
    }

    fun getSurfaceOpenOffsetByDragView(dragView: Int): Int {
        return mSwipeViewLayouter!!.viewEngines.getValue(dragView).openOffset
    }

    fun captureChildrenBound() {
        mLayoutCache!!.captureChildrenBound(mSwipeViewLayouter!!.views.values)
    }

    fun restoreChildrenBound() {
        mLayoutCache!!.restoreOnLayout()
    }
}
