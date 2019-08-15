package kaufland.com.swipelibrary.dragengine

import android.view.View

import kaufland.com.swipelibrary.DragView
import kaufland.com.swipelibrary.SurfaceView
import kaufland.com.swipelibrary.SwipeDirectionDetector
import kaufland.com.swipelibrary.SwipeDirectionDetector.Companion.SWIPE_DIRECTION_RIGHT
import kaufland.com.swipelibrary.SwipeLayout
import kaufland.com.swipelibrary.SwipeResult
import kaufland.com.swipelibrary.SwipeState
import kaufland.com.swipelibrary.SwipeViewLayouter

import kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED
import kaufland.com.swipelibrary.SwipeState.DragViewState.RIGHT_OPEN

/**
 * Created by sbra0902 on 29.03.17.
 */

class RightDragViewEngine(private val mLayouter: SwipeViewLayouter) : DraggingEngine<DragView> {
    override var dragView: DragView? = null

    private var mSurfaceView: SurfaceView? = null

    private var mInitialXPos: Int = 0

    override var dragDistance: Int = 0

    override var intermmediateDistance: Int = 0

    override val openOffset: Int
        get() = -dragDistance

    override fun moveView(offset: Float, view: SurfaceView, changedView: View) {

        if (dragView != changedView) {
            dragView!!.x = view.x + mSurfaceView!!.width
        }
    }

    override fun initializePosition(orientation: SwipeViewLayouter.DragDirection) {

        dragView = mLayouter.views[SwipeLayout.RIGHT_DRAG_VIEW] as DragView
        mSurfaceView = mLayouter.surfaceView
        dragDistance = dragView!!.width
        mInitialXPos = (mSurfaceView!!.x + mSurfaceView!!.width).toInt()
        intermmediateDistance = if (dragView!!.settlePointResourceId != -1) dragView!!.findViewById(dragView!!.settlePointResourceId).right else dragView!!.width

        moveToInitial()
    }

    private fun moveToInitial() {
        dragView!!.x = mInitialXPos.toFloat()
    }

    override fun clampViewPositionHorizontal(child: View, left: Int): Int {
        return if (left < mSurfaceView!!.width - dragDistance && !dragView!!.isBouncePossible) {
            mSurfaceView!!.width - dragDistance
        } else left

    }

    override fun restoreState(state: SwipeState.DragViewState, view: SurfaceView) {
        when (state) {
            SwipeState.DragViewState.LEFT_OPEN -> dragView!!.x = (mLayouter.leftDragView.width + mSurfaceView!!.width).toFloat()
            RIGHT_OPEN -> dragView!!.x = 0f
            SwipeState.DragViewState.TOP_OPEN -> {
            }
            SwipeState.DragViewState.BOTTOM_OPEN -> {
            }
            else ->

                dragView!!.x = mSurfaceView!!.width.toFloat()
        }//TODO Implementation
        //TODO Implementation
    }

    override fun determineSwipeHorizontalState(velocity: Float, swipeDirectionDetector: SwipeDirectionDetector, swipeState: SwipeState, swipeListener: SwipeLayout.SwipeListener, releasedChild: View): SwipeResult? {
        if (releasedChild == dragView && swipeDirectionDetector.swipeDirection == SWIPE_DIRECTION_RIGHT && Math.abs(swipeDirectionDetector.difX) > dragDistance / 2) {
            swipeState.state = SwipeState.DragViewState.CLOSED
            return SwipeResult(mSurfaceView!!.width, Runnable { swipeListener.onSwipeClosed(CLOSED) })
        } else if (releasedChild == dragView) {
            swipeState.state = RIGHT_OPEN
            return SwipeResult(mSurfaceView!!.width - dragDistance, Runnable { swipeListener.onSwipeClosed(RIGHT_OPEN) })
        }

        return null
    }
}
