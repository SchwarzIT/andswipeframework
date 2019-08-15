package kaufland.com.swipelibrary.dragengine

import android.view.View

import kaufland.com.swipelibrary.DragView
import kaufland.com.swipelibrary.SurfaceView
import kaufland.com.swipelibrary.SwipeDirectionDetector
import kaufland.com.swipelibrary.SwipeDirectionDetector.Companion.SWIPE_DIRECTION_LEFT
import kaufland.com.swipelibrary.SwipeLayout
import kaufland.com.swipelibrary.SwipeResult
import kaufland.com.swipelibrary.SwipeState
import kaufland.com.swipelibrary.SwipeViewLayouter

import kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED

/**
 * Created by sbra0902 on 29.03.17.
 */

class LeftDragViewEngine(private val mLayouter: SwipeViewLayouter) : DraggingEngine<DragView> {
    override val openOffset: Int
        get() = dragDistance

    private var mSurfaceView: SurfaceView? = null

    override var dragView: DragView? = null

    private var mInitialXPos: Int = 0

    override var dragDistance: Int = 0

    override var intermmediateDistance: Int = 0

    override fun moveView(offset: Float, view: SurfaceView, changedView: View) {
        if (dragView != changedView) {
            dragView!!.x = view.x - dragView!!.width
        }
    }

    override fun initializePosition(orientation: SwipeViewLayouter.DragDirection) {

        mSurfaceView = mLayouter.surfaceView
        dragView = mLayouter.leftDragView

        mInitialXPos = (mSurfaceView!!.x - dragView!!.width).toInt()
        dragDistance = dragView!!.width
        intermmediateDistance = if (dragView!!.settlePointResourceId != -1) dragView!!.findViewById(dragView!!.settlePointResourceId).right else dragView!!.width

        moveToInitial()
    }

    private fun moveToInitial() {
        dragView!!.x = mInitialXPos.toFloat()
    }

    override fun clampViewPositionHorizontal(child: View, left: Int): Int {

        return if (dragView != null && child == dragView) {


            if (left > 0 && !dragView!!.isBouncePossible) {
                0
            } else left

        } else 0

    }

    override fun restoreState(state: SwipeState.DragViewState, view: SurfaceView) {
        when (state) {
            SwipeState.DragViewState.LEFT_OPEN ->

                dragView!!.x = 0f
            SwipeState.DragViewState.RIGHT_OPEN -> dragView!!.x = (mLayouter.rightDragView.x - mSurfaceView!!.width.toFloat() - dragView!!.width.toFloat()).toInt().toFloat()
            SwipeState.DragViewState.TOP_OPEN -> {
            }
            SwipeState.DragViewState.BOTTOM_OPEN -> {
            }
            else -> dragView!!.x = (-dragDistance).toFloat()
        }//TODO Implementation
        //TODO Implementation
    }

    override fun determineSwipeHorizontalState(velocity: Float, swipeDirectionDetector: SwipeDirectionDetector, swipeState: SwipeState, swipeListener: SwipeLayout.SwipeListener, releasedChild: View): SwipeResult? {
        if (dragView == releasedChild && swipeDirectionDetector.swipeDirection == SWIPE_DIRECTION_LEFT && Math.abs(swipeDirectionDetector.difX) > dragDistance / 2) {
            swipeState.state = SwipeState.DragViewState.CLOSED
            return SwipeResult(-dragView!!.width, Runnable { swipeListener.onSwipeClosed(CLOSED) })
        }

        return null
    }
}
